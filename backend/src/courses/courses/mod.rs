use rocket::serde::Serialize;
use sqlx::{FromRow, PgConnection};
use super::session_token;

#[derive(Serialize, FromRow, Clone)]
pub struct Course
{
    pub id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

#[derive(Serialize, FromRow, Clone, Copy)]
pub struct UserCourse
{
    pub id: Option<i32>,
    user_id: i32,
    pub course_id: i32,
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

pub async fn get_courses(course_ids: Vec<i32>, connection: &mut PgConnection) -> Result<Vec<Course>, String>
{
    let courses: Vec<Course> = match sqlx::query_as("SELECT id, name, semester, ects FROM courses WHERE id = ANY($1)")
        .bind(&course_ids)
        .fetch_all(connection)
        .await
    {
        Ok(courses) => courses,
        Err(error) => return Err(format!("{}", error))
    };

    Ok(courses)
}
