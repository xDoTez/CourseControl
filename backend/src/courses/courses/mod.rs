use super::{session_token, CourseDataSortingOptions};
use crate::database;
use rocket::serde::Serialize;
use sqlx::{FromRow, PgConnection, Row};

#[derive(Serialize, FromRow, Clone)]
pub struct Course {
    pub id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

#[derive(Serialize, FromRow, Clone, Copy)]
pub struct UserCourse {
    pub id: Option<i32>,
    user_id: i32,
    pub course_id: i32,
    is_active: bool,
}

pub async fn get_user_course_data(
    session_token: session_token::SessionToken,
    is_active: bool,
    connection: &mut PgConnection,
    sorting_option: &CourseDataSortingOptions,
) -> Result<Vec<UserCourse>, String> {
    let query = format!("SELECT user_courses.id, user_id, course_id, is_active FROM user_courses, courses WHERE user_id = $1 AND is_active = $2 AND user_courses.course_id = courses.id {}", sorting_option.to_query_sorting_clause());
    let user_course: Vec<UserCourse> = match sqlx::query_as(&query)
        .bind(&session_token.user)
        .bind(&is_active)
        .bind(&sorting_option.to_query_sorting_clause())
        .fetch_all(connection)
        .await
    {
        Ok(data) => data,
        Err(error) => return Err(format!("{}", error)),
    };

    Ok(user_course)
}

pub async fn get_courses(
    course_ids: Vec<i32>,
    connection: &mut PgConnection,
    sorting_option: &CourseDataSortingOptions,
) -> Result<Vec<Course>, String> {
    let query = format!(
        "SELECT id, name, semester, ects FROM courses WHERE id = ANY($1) {}",
        sorting_option.to_query_sorting_clause()
    );
    let courses: Vec<Course> = match sqlx::query_as(&query)
        .bind(&course_ids)
        .fetch_all(connection)
        .await
    {
        Ok(courses) => courses,
        Err(error) => return Err(format!("{}", error)),
    };

    Ok(courses)
}

pub async fn get_course(course_id: i32, connection: &mut PgConnection) -> Result<Course, String> {
    let course: Course =
        match sqlx::query_as("SELECT id, name, semester, ects FROM courses WHERE id = $1")
            .bind(course_id)
            .fetch_one(connection)
            .await
        {
            Ok(course) => course,
            Err(error) => return Err(format!("{}", error)),
        };

    Ok(course)
}

pub enum GettingAllAddableCourses {
    Sucess(Vec<Course>),
    DatabaseError(String),
}

impl Course {
    pub async fn get_all_addable_courses(
        session_token: session_token::SessionToken,
        program_id: i32,
    ) -> GettingAllAddableCourses {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => return GettingAllAddableCourses::DatabaseError(error),
        };

        let courses: Vec<Course> = match sqlx::query_as("SELECT c.id, c.name, c.semester, c.ects FROM courses c LEFT JOIN user_courses u_c ON c.id = u_c.course_id AND u_c.user_id = $1 AND u_c.is_active = true LEFT JOIN course_progam c_p ON c.id = c_p.course_id AND c_p.program_id = $2 WHERE u_c.id IS NULL AND c_p.course_id IS NOT NULL")
            .bind(&session_token.user)
            .bind(&program_id)
            .fetch_all(&mut connection)
            .await
        {
            Ok(courses) => courses,
            Err(error) => return GettingAllAddableCourses::DatabaseError(format!("{}", error))
        };

        GettingAllAddableCourses::Sucess(courses)
    }

    pub async fn add_course_to_user(
        &self,
        user_id: i32,
        connection: &mut PgConnection,
    ) -> Result<i32, String> {
        match sqlx::query("INSERT INTO user_courses (user_id, course_id, is_active) VALUES ($1, $2, true) RETURNING id")
            .bind(&user_id)
            .bind(&self.id)
            .fetch_one(connection).await
        {
            Ok(result) => Ok(result.get("id")),
            Err(error) => Err(format!("{}", error))
        }
    }
}
