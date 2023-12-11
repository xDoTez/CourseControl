use rocket::serde::Serialize;
use sqlx::{PgConnection, FromRow};

#[derive(Serialize, FromRow)]
pub struct Category
{
    pub id: Option<i32>,
    course_id: i32,
    name: String,
    points: i32,
    requirements: i32
}

#[derive(Serialize, FromRow, Clone, Copy)]
pub struct CourseCategory
{
    pub id: Option<i32>,
    user_course_id: i32,
    pub category_id: i32,
    points: i32,
}

pub async fn get_user_categories(user_course_id: i32, connectio: &mut PgConnection) -> Result<Vec<CourseCategory>, String>
{
    let course_category: Vec<CourseCategory> = match sqlx::query_as("SELECT * FROM course_categories WHERE user_course_id = $1")
        .bind(&user_course_id)
        .fetch_all(connectio)
        .await
        {
            Ok(c_c) => c_c,
            Err(error) => return Err(format!("{}", error))
        };

    Ok(course_category)
}

pub async fn get_category(category_ids: i32, connection: &mut PgConnection) -> Result<Category, String>
{
    let categories: Category = match sqlx::query_as("SELECT id, course_id, name, points, requirements FROM categories WHERE id = $1")
        .bind(&category_ids)
        .fetch_one(connection)
        .await
    {
        Ok(cats) => cats,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(categories)
}

pub async fn get_categories(course_id: i32, connection: &mut PgConnection) -> Result<Vec<Category>, String>
{
    let categories: Vec<Category> = match sqlx::query_as("SELECT id, course_id, name, points, requirements FROM categories WHERE course_id = $1")
        .bind(&course_id)
        .fetch_all(connection)
        .await
    {
        Ok(cats) => cats,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(categories)

}
