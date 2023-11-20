use rocket::serde::Serialize;
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

#[derive(Serialize)]
pub struct UserCourse
{
    id: Option<i32>,
    user_id: i32,
    course_id: i32,
    categories: Option<Vec<CourseCategory>>,
    is_active: bool
}

#[derive(Serialize)]
pub struct CourseCategory
{
    id: Option<i32>,
    user_course_id: i32,
    category_id: i32,
    points: i32,
    subcategories: Option<Vec<CategorySubcategory>>
}

#[derive(Serialize)]
pub struct CategorySubcategory
{
    user_course_category_id: i32,
    subcategory_id: i32,
    points: i32
}

pub async fn get_all_course_for_user(session_token: session_token::SessionToken) -> Result<UserCourse, String>
{
    let mut connection = match database::establish_connection_to_database().await
        {
            Ok(con) => con,
            Err(error) => return Err(format!("{}", error))
        };
    // check if the session token is valid
    match session_token.validate_token(&mut connection).await
    {
        Ok(_) => {},
        Err(error) => return Err(format!("{}", error))
    };
    // get all active user course data
    // get all course data relevant to use
    Ok(UserCourse { id: None, user_id: 1, course_id: 1, categories: None, is_active: true })
}
