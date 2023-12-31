use serde::Serialize;
use sqlx::{FromRow, PgConnection};

#[derive(Serialize, FromRow)]
pub struct Subcategory
{
    id: Option<i32>,
    pub category_id: i32,
    name: String,
    points: i32,
    requirements: i32
}

#[derive(Serialize, FromRow)]
pub struct CategorySubcategory
{
    user_course_category_id: i32,
    pub subcategory_id: i32,
    points: i32
}

pub async fn get_user_subcategory(user_category_id: i32, connection: &mut PgConnection) -> Result<Vec<CategorySubcategory>, String>
{
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

pub async fn get_subcategory(subcategory_id: i32, connection: &mut PgConnection) -> Result<Subcategory, String>
{
    let subcategory: Subcategory = match sqlx::query_as("SELECT id, category_id, name, points, requirements FROM subcategories WHERE id = $1")
        .bind(&subcategory_id)
        .fetch_one(connection)
        .await
    {
        Ok(subcat) => subcat,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(subcategory)
}

pub async fn get_subcategories(category_id: i32, connection: &mut PgConnection) -> Result<Vec<Subcategory>, String>
{
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

impl Subcategory
{
    pub async fn add_subcategory_to_category_data(&self, category_id: i32, connection: &mut PgConnection) -> Result<i32, String>
    {
        let subcategory_id = match self.id
        {
            Some(id) => id,
            None => return Err(String::from("Missing category id"))
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
