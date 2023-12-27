use rocket::serde::{Deserialize, Serialize};
use sqlx::{FromRow, PgConnection, Row};

use super::{subcategories::CategorySubcategory, ModifyUserCourseDataResult};

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
            Some(id) => match sqlx::query("UPDATE course_categories points = $1 WHERE id = $2")
                .bind(&self.points)
                .bind(&id)
                .execute(connection)
                .await
            {
                Ok(_) => {}
                Err(error) => return ModifyingDataResult::DatabaseError(format!("{}", error)),
            },
            None => return ModifyingDataResult::MissingId,
        }

        ModifyingDataResult::Success
    }
}
