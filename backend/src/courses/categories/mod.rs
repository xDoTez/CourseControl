use rocket::serde::Serialize;
use sqlx::{PgConnection, FromRow};

#[derive(Serialize)]
pub struct Category
{
    id: Option<i32>,
    course_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

#[derive(Serialize, FromRow)]
pub struct CourseCategory
{
    id: Option<i32>,
    user_course_id: i32,
    category_id: i32,
    points: i32,
}

async fn get_user_categories(user_course_id: i32, connectio: &mut PgConnection) -> Result<Vec<CourseCategory>, String>
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
