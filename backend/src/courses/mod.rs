use rocket::serde::Serialize;
use sqlx::{FromRow, PgConnection};
use crate::database;
use crate::session_token;

struct Program
{
    id: Option<i32>,
    name: String,
}

struct Course
{
    id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

struct Category
{
    id: Option<i32>,
    course_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

struct Subcategory
{
    id: Option<i32>,
    category_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

#[derive(Serialize, FromRow)]
pub struct UserCourse
{
    id: Option<i32>,
    user_id: i32,
    course_id: i32,
    is_active: bool
}

#[derive(Serialize)]
pub struct CourseCategory
{
    id: Option<i32>,
    user_course_id: i32,
    category_id: i32,
    points: i32,
}

#[derive(Serialize)]
pub struct CategorySubcategory
{
    user_course_category_id: i32,
    subcategory_id: i32,
    points: i32
}

pub async fn get_all_course_for_user(session_token: session_token::SessionToken, is_active: bool) -> Result<Vec<UserCourse>, String>
{
    let mut connection = match database::establish_connection_to_database().await
        {
            Ok(con) => con,
            Err(_) => return Err(format!("Session token invalid"))
        };
    // check if the session token is valid
    match session_token.validate_token(&mut connection).await
        {
            Ok(_) => {},
            Err(error) => return Err(format!("{}", error))
        };

    // get all active user course data

    // get all course data relevant to use
    get_user_course_data(session_token, is_active, &mut connection).await
}

async fn get_user_course_data(session_token: session_token::SessionToken, is_active: bool, connection: &mut PgConnection) -> Result<Vec<UserCourse>, String>
{
    let user_course: Vec<UserCourse> = match sqlx::query_as("SELECT * FROM user_courses WHERE user_id = $1 AND is_active = $2")
        .bind(&session_token.user)
        .bind(&is_active)
        .fetch_all(connection)
        .await
    {
        Ok(data) => data,
        Err(error) => return Err(format!("{}", error)) 
    };

    Ok(user_course)
}
