use super::subcategories;
use rocket::serde::{Deserialize, Serialize};
use sqlx::{FromRow, PgConnection, Row};

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
}

pub struct ModifiedCategory {
    id: i32,
    name: String,
    points: i32,
    requirements: i32,
    modified_subcategories: Vec<subcategories::ModifiedSubcategory>,
    new_subcategories: Vec<subcategories::NewSubcategory>,
    deleted_subcategory_ids: Vec<i32>
}
