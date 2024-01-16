use serde::{Deserialize, Serialize};
use sqlx::{FromRow, PgConnection, Postgres, Transaction};

#[derive(Serialize, Deserialize, FromRow, Clone)]
pub struct Subcategory {
    id: Option<i32>,
    pub category_id: i32,
    name: String,
    pub points: i32,
    pub requirements: i32,
}

#[derive(Serialize, Deserialize, FromRow, Clone)]
pub struct CategorySubcategory {
    user_course_category_id: i32,
    pub subcategory_id: i32,
    pub points: i32,
}

pub async fn get_user_subcategory(
    user_category_id: i32,
    connection: &mut PgConnection,
) -> Result<Vec<CategorySubcategory>, String> {
    let subcategories: Vec<CategorySubcategory> = match sqlx::query_as("SELECT user_course_category_id, subcategory_id, points FROM category_subcategories WHERE user_course_category_id = $1")
        .bind(&user_category_id)
        .fetch_all(connection)
        .await
    {
        Ok(subcats) => subcats,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(subcategories)
}

pub async fn get_subcategory(
    subcategory_id: i32,
    connection: &mut PgConnection,
) -> Result<Subcategory, String> {
    let subcategory: Subcategory = match sqlx::query_as(
        "SELECT id, category_id, name, points, requirements FROM subcategories WHERE id = $1",
    )
    .bind(&subcategory_id)
    .fetch_one(connection)
    .await
    {
        Ok(subcat) => subcat,
        Err(error) => return Err(format!("{}", error)),
    };

    Ok(subcategory)
}

pub async fn get_subcategories(
    category_id: i32,
    connection: &mut PgConnection,
) -> Result<Vec<Subcategory>, String> {
    let subcategories: Vec<Subcategory> = match sqlx::query_as("SELECT id, category_id, name, points, requirements FROM subcategories WHERE category_id = $1")
        .bind(&category_id)
        .fetch_all(connection)
        .await
    {
        Ok(subcats) => subcats,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(subcategories)
}

impl Subcategory {
    pub async fn add_subcategory_to_category_data(
        &self,
        category_id: i32,
        connection: &mut PgConnection,
    ) -> Result<i32, String> {
        let subcategory_id = match self.id {
            Some(id) => id,
            None => return Err(String::from("Missing category id")),
        };

        match sqlx::query("INSERT INTO category_subcategories (user_course_category_id, subcategory_id, points) VALUES ($1, $2, 0)")
            .bind(&category_id)
            .bind(&subcategory_id)
            .execute(connection).await
        {
            Ok(_) => Ok(1),
            Err(error) => Err(format!("{}", error))
        }
    }
}

pub enum ModifyingDataResult {
    Success,
    DatabaseError(String),
}

impl CategorySubcategory {
    pub async fn modify_existing_data(&self, connection: &mut PgConnection) -> ModifyingDataResult {
        let query = format!("UPDATE category_subcategories SET points = {} WHERE user_course_category_id = {} AND subcategory_id = {}", self.points, self.user_course_category_id, self.subcategory_id);
        match sqlx::query(&query).execute(connection).await {
            Ok(_) => {}
            Err(error) => return ModifyingDataResult::DatabaseError(format!("{}", error)),
        };

        ModifyingDataResult::Success
    }
}

#[derive(Clone, Deserialize)]
pub struct NewSubcategory {
    pub category_id: Option<i32>,
    pub name: String,
    pub points: i32,
    pub requirements: i32,
}

impl NewSubcategory // impl block for adding new courses
{
    pub async fn insert_new_subcategory(
        &self,
        connection: &mut PgConnection,
    ) -> Result<(), String> {
        match &self.category_id {
            None => Err(String::from("Missing category id")),
            Some(id) => {
                match sqlx::query("INSERT INTO subcategories(category_id, name, points, requirements) VALUES ($1, $2, $3, $4)")
                    .bind(&id)
                    .bind(&self.name)
                    .bind(&self.points)
                    .bind(&self.requirements)
                    .execute(connection)
                    .await {
                        Ok(_) => Ok(()),
                        Err(error) => Err(format!("{}", error))
                    }
            }
        }
    }

    pub async fn transaction_insert_new_subcategory(
        &self,
        connection: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        match &self.category_id {
            None => Err(String::from("Missing category id")),
            Some(id) => {
                match sqlx::query("INSERT INTO subcategories(category_id, name, points, requirements) VALUES ($1, $2, $3, $4)")
                    .bind(&id)
                    .bind(&self.name)
                    .bind(&self.points)
                    .bind(&self.requirements)
                    .execute(&mut **connection)
                    .await {
                        Ok(_) => Ok(()),
                        Err(error) => Err(format!("{}", error))
                    }
            }
        }
    }
}

pub struct ModifiedSubcategory {
    pub id: i32,
    pub name: String,
    pub points: i32,
    pub requirements: i32,
}

impl ModifiedSubcategory {
    pub async fn transaction_modify_subcategory(
        &self,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        match sqlx::query(
            "UPDATE subcategories SET name = $1, points = $2, requirements = $3 WHERE id = $4",
        )
        .bind(&self.name)
        .bind(&self.points)
        .bind(&self.requirements)
        .bind(&self.id)
        .execute(&mut **transaction)
        .await
        {
            Ok(_) => Ok(()),
            Err(error) => Err(format!("{}", error)),
        }
    }
}
