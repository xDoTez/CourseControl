use rocket::serde::Serialize;
use sqlx::{FromRow, PgConnection};
use super::session_token;

pub struct Course
{
    id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

#[derive(Serialize, FromRow)]
pub struct UserCourse
{
    id: Option<i32>,
    user_id: i32,
    course_id: i32,
    is_active: bool
}

pub async fn get_user_course_data(session_token: session_token::SessionToken, is_active: bool, connection: &mut PgConnection) -> Result<Vec<UserCourse>, String>
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


