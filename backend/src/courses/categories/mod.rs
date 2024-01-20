use super::subcategories;
use rocket::serde::{Deserialize, Serialize};
use sqlx::{FromRow, PgConnection, Postgres, Row, Transaction};

#[derive(Serialize, Deserialize, FromRow, Clone)]
pub struct Category {
    pub id: Option<i32>,
    course_id: i32,
    name: String,
    pub points: i32,
    pub requirements: i32,
}

#[derive(Serialize, Deserialize, FromRow, Clone, Copy)]
pub struct CourseCategory {
    pub id: Option<i32>,
    user_course_id: i32,
    pub category_id: i32,
    pub points: i32,
}

pub async fn get_user_categories(
    user_course_id: i32,
    connectio: &mut PgConnection,
) -> Result<Vec<CourseCategory>, String> {
    let course_category: Vec<CourseCategory> =
        match sqlx::query_as("SELECT * FROM course_categories WHERE user_course_id = $1")
            .bind(&user_course_id)
            .fetch_all(connectio)
            .await
        {
            Ok(c_c) => c_c,
            Err(error) => return Err(format!("{}", error)),
        };

    Ok(course_category)
}

pub async fn get_category(
    category_ids: i32,
    connection: &mut PgConnection,
) -> Result<Category, String> {
    let categories: Category = match sqlx::query_as(
        "SELECT id, course_id, name, points, requirements FROM categories WHERE id = $1",
    )
    .bind(&category_ids)
    .fetch_one(connection)
    .await
    {
        Ok(cats) => cats,
        Err(error) => return Err(format!("{}", error)),
    };

    Ok(categories)
}

pub async fn get_categories(
    course_id: i32,
    connection: &mut PgConnection,
) -> Result<Vec<Category>, String> {
    let categories: Vec<Category> = match sqlx::query_as(
        "SELECT id, course_id, name, points, requirements FROM categories WHERE course_id = $1",
    )
    .bind(&course_id)
    .fetch_all(connection)
    .await
    {
        Ok(cats) => cats,
        Err(error) => return Err(format!("{}", error)),
    };

    Ok(categories)
}

impl Category {
    pub async fn add_category_to_course_data(
        &self,
        user_course_id: i32,
        connection: &mut PgConnection,
    ) -> Result<i32, String> {
        let category_id = match self.id {
            Some(id) => id,
            None => return Err(String::from("Missing category id")),
        };

        match sqlx::query("INSERT INTO course_categories (user_course_id, category_id, points) VALUES ($1, $2, 0) RETURNING id")
            .bind(&user_course_id)
            .bind(&category_id)
            .fetch_one(connection).await
        {
            Ok(result) => Ok(result.get("id")),
            Err(error) => Err(format!("{}", error))
        }
    }
}

pub enum ModifyingDataResult {
    Success,
    DatabaseError(String),
    MissingId,
}

impl CourseCategory {
    pub async fn modify_existing_data(&self, connection: &mut PgConnection) -> ModifyingDataResult {
        match self.id {
            Some(id) => {
                let query = format!(
                    "UPDATE course_categories SET points = {} WHERE id = {}",
                    self.points, id
                );
                match sqlx::query(&query).execute(connection).await {
                    Ok(_) => {}
                    Err(error) => return ModifyingDataResult::DatabaseError(format!("{}", error)),
                }
            }
            None => return ModifyingDataResult::MissingId,
        }

        ModifyingDataResult::Success
    }
}

#[derive(Deserialize)]
pub struct NewCategory {
    pub course_id: Option<i32>,
    pub name: String,
    pub points: i32,
    pub requirements: i32,
    pub subcategories: Option<Vec<subcategories::NewSubcategory>>,
}

impl NewCategory // impl block for adding new courses
{
    pub async fn insert_new_category(
        &mut self,
        connectio: &mut PgConnection,
    ) -> Result<i32, String> {
        match &self.course_id {
            None => Err(String::from("Missing course Id")),
            Some(id) => {
                match sqlx::query("INSERT INTO categories(course_id, name, points, requirements) VALUES ($1, $2, $3, $4) RETURNING id")
                    .bind(&id)
                    .bind(&self.name)
                    .bind(&self.points)
                    .bind(&self.requirements)
                    .fetch_one(connectio)
                    .await {
                        Ok(row) => match row.try_get("id") {
                            Ok(id) => Ok(id),
                            Err(error) => Err(format!("{}", error))
                        },
                        Err(error) => Err(format!("{}", error))
                    }
            }
        }
    }

    pub async fn transaction_insert_new_category(
        &mut self,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<i32, String> {
        match &self.course_id {
            None => Err(String::from("Missing course Id")),
            Some(id) => {
                match sqlx::query("INSERT INTO categories(course_id, name, points, requirements) VALUES ($1, $2, $3, $4) RETURNING id")
                    .bind(&id)
                    .bind(&self.name)
                    .bind(&self.points)
                    .bind(&self.requirements)
                    .fetch_one(&mut **transaction)
                    .await {
                        Ok(row) => match row.try_get("id") {
                            Ok(id) => Ok(id),
                            Err(error) => Err(format!("{}", error))
                        },
                        Err(error) => Err(format!("{}", error))
                    }
            }
        }
    }

    pub async fn transaction_delete_categories_by_ids(
        categorie_ids: &Vec<i32>,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), i32> {
        for category_id in categorie_ids {
            match sqlx::query("DELETE FROM categories WHERE id = $1")
                .bind(&category_id)
                .execute(&mut **transaction)
                .await
            {
                Ok(_) => {}
                Err(_) => return Err(*category_id),
            };
        }

        Ok(())
    }
}

#[derive(Deserialize)]
pub struct ModifiedCategory {
    id: i32,
    name: String,
    points: i32,
    requirements: i32,
    modified_subcategories: Vec<subcategories::ModifiedSubcategory>,
    new_subcategories: Vec<subcategories::NewSubcategory>,
    unchanged_subcategories: Vec<subcategories::ModifiedSubcategory>,
    deleted_subcategory_ids: Vec<i32>,
}

pub enum ModifyingCategoryResult {
    Success,
    DatabaseError(String),
    MissmatchingPoints(i32),
}

impl ModifiedCategory {
    // impl block for modifying existing categories
    pub async fn transaction_modifie(
        &self,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> ModifyingCategoryResult {
        let modified_points_sum: i32 = self.modified_subcategories.iter().map(|x| x.points).sum();
        let new_points_sum: i32 = self.new_subcategories.iter().map(|x| x.points).sum();
        let unchanged_points_sum: i32 = self.unchanged_subcategories.iter().map(|x| x.points).sum();
        match self.points == modified_points_sum + new_points_sum + unchanged_points_sum {
            true => {
                // modify the category
                match sqlx::query(
                    "UPDATE categories SET name = $1, points = $2, requirements = $3 WHERE id = $4",
                )
                .bind(&self.name)
                .bind(&self.points)
                .bind(&self.requirements)
                .bind(&self.id)
                .execute(&mut **transaction)
                .await
                {
                    Ok(_) => {}
                    Err(error) => {
                        return ModifyingCategoryResult::DatabaseError(format!("{}", error))
                    }
                };

                // modify the subcategories
                for subcategory in &self.modified_subcategories {
                    match subcategory
                        .transaction_modify_subcategory(transaction)
                        .await
                    {
                        Ok(_) => {}
                        Err(error) => return ModifyingCategoryResult::DatabaseError(error),
                    }
                }

                match self
                    .transaction_delete_subcategories_on_category(transaction)
                    .await
                {
                    Ok(_) => {}
                    Err(error) => return ModifyingCategoryResult::DatabaseError(error),
                };

                ModifyingCategoryResult::Success
            }
            false => ModifyingCategoryResult::MissmatchingPoints(self.id),
        }
    }

    async fn transaction_delete_subcategories_on_category(
        &self,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        match sqlx::query("DELETE FROM subcategories WHERE id = ANY($1)")
            .bind(&self.deleted_subcategory_ids)
            .execute(&mut **transaction)
            .await
        {
            Ok(_) => Ok(()),
            Err(error) => Err(format!("{}", error)),
        }
    }
}

#[derive(Serialize, FromRow)]
pub struct CategoryTemplate {
    category: Category,
    subcategories: Vec<subcategories::Subcategory>,
}

impl CategoryTemplate {
    pub async fn get_categories(
        course_id: i32,
        connectio: &mut PgConnection,
    ) -> Result<Vec<CategoryTemplate>, String> {
        let categories: Vec<Category> = match sqlx::query_as(
            "SELECT id, course_id, name, points, requirements FROM categories WHERE course_id = $1",
        )
        .bind(&course_id)
        .fetch_all(&mut *connectio)
        .await
        {
            Ok(categories) => categories,
            Err(error) => return Err(format!("{}", error)),
        };

        let mut category_templates: Vec<CategoryTemplate> = Vec::new();
        for category in categories {
            match category.id {
                None => return Err(String::from("Category missing ID")),
                Some(cat_id) => {
                    match subcategories::get_subcategories(cat_id, &mut *connectio).await {
                        Ok(subcats) => category_templates.push(CategoryTemplate {
                            category: category,
                            subcategories: subcats,
                        }),
                        Err(error) => return Err(error),
                    }
                }
            }
        }

        Ok(category_templates)
    }
}
