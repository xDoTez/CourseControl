use serde::Serialize;
use sqlx::{FromRow, PgConnection};

#[derive(Serialize)]
pub struct Subcategory
{
    id: Option<i32>,
    category_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

#[derive(Serialize)]
pub struct CategorySubcategory
{
    user_course_category_id: i32,
    subcategory_id: i32,
    points: i32
}


