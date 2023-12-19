mod categories;
pub mod courses;
mod subcategories;

use std::fmt::Display;

use crate::database;
use crate::session_token;
use itertools::iproduct;
use rocket::serde::Serialize;
use sqlx::prelude::FromRow;
use sqlx::PgConnection;

#[derive(Serialize, FromRow)]
pub struct Program {
    id: Option<i32>,
    name: String,
}

#[derive(Serialize)]
pub struct CourseData {
    course: courses::Course,
    course_user_data: courses::UserCourse,
    categories: Option<Vec<CategoryData>>,
}

#[derive(Serialize)]
pub struct CategoryData {
    category: categories::Category,
    category_user_data: categories::CourseCategory,
    subcategories: Option<Vec<SubcategoryData>>,
}

#[derive(Serialize)]
pub struct SubcategoryData {
    subcategory: subcategories::Subcategory,
    subcategory_user_data: subcategories::CategorySubcategory,
}

pub enum UserCourseResult {
    DatabaseConnectionError,
    InvalidSessionToken,
    DatabaseError(String),
    Success(Vec<CourseData>),
}

impl Display for UserCourseResult {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            UserCourseResult::DatabaseError(_) => write!(f, "DatabaseError"),
            UserCourseResult::Success(_) => write!(f, "Success"),
            UserCourseResult::DatabaseConnectionError => write!(f, "DatabaseConnectionError"),
            UserCourseResult::InvalidSessionToken => write!(f, "InvalidSessionToken"),
        }
    }
}

pub enum GettingProgramsResult {
    Success(Vec<Program>),
    DatabaseError(String),
}

impl Program {
    pub async fn get_all_programs() -> GettingProgramsResult {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => return GettingProgramsResult::DatabaseError(error),
        };

        let programs: Vec<Program> = match sqlx::query_as("SELECT id, name FROM programs")
            .fetch_all(&mut connection)
            .await
        {
            Ok(programs) => programs,
            Err(error) => return GettingProgramsResult::DatabaseError(format!("{}", error)),
        };

        GettingProgramsResult::Success(programs)
    }
}

#[derive(FromFormField)]
pub enum CourseDataSortingOptions {
    NameAlphabeticAsc,
    NameAlphabeticDesc,
    SemesterAsc,
    SemesterDesc,
}

impl CourseDataSortingOptions {
    pub fn from_string(string: String) -> Self {
        match string.as_str() {
            "NameAlphabeticAsc" => CourseDataSortingOptions::NameAlphabeticAsc,
            "NameAlphabeticDesc" => CourseDataSortingOptions::NameAlphabeticDesc,
            "SemesterAsc" => CourseDataSortingOptions::SemesterAsc,
            "SemesterDesc" => CourseDataSortingOptions::SemesterDesc,
            &_ => CourseDataSortingOptions::NameAlphabeticDesc,
        }
    }

    pub fn to_query_sorting_clause(&self) -> String {
        match self {
            CourseDataSortingOptions::NameAlphabeticAsc => {
                String::from(" ORDER BY courses.name ASC")
            }
            CourseDataSortingOptions::NameAlphabeticDesc => {
                String::from(" ORDER BY courses.name DESC")
            }
            CourseDataSortingOptions::SemesterAsc => String::from(" ORDER BY courses.semester ASC"),
            CourseDataSortingOptions::SemesterDesc => {
                String::from(" ORDER BY courses.semester DESC")
            }
        }
    }
}

pub async fn get_all_course_for_user(
    session_token: session_token::SessionToken,
    is_active: bool,
    sorting_option: CourseDataSortingOptions,
) -> UserCourseResult {
    let mut connection = match database::establish_connection_to_database().await {
        Ok(con) => con,
        Err(_) => return UserCourseResult::DatabaseConnectionError,
    };

    match session_token.validate_token(&mut connection).await {
        Ok(_) => {}
        Err(_) => return UserCourseResult::InvalidSessionToken,
    };

    let courses_data = match courses::get_user_course_data(
        session_token,
        is_active,
        &mut connection,
        &sorting_option,
    )
    .await
    {
        Ok(data) => data,
        Err(error) => return UserCourseResult::DatabaseError(error),
    };

    let course_ids: Vec<i32> = courses_data.iter().map(|x| x.course_id).collect();
    let courses = match courses::get_courses(course_ids, &mut connection, &sorting_option).await {
        Ok(courses) => courses,
        Err(error) => return UserCourseResult::DatabaseError(error),
    };

    let mut results: Vec<CourseData> = Vec::new();
    for (course_data, course) in iproduct!(courses_data, courses).filter(|(x, y)| match y.id {
        Some(id) => id == x.course_id,
        None => false,
    }) {
        results.push(CourseData {
            course: course,
            course_user_data: course_data,
            categories: match course_data.id {
                Some(id) => Some(
                    match get_all_categories_for_user(id, &mut connection).await {
                        Ok(cats) => cats,
                        Err(error) => return UserCourseResult::DatabaseError(error),
                    },
                ),
                None => None,
            },
        });
    }

    UserCourseResult::Success(results)
}

async fn get_all_categories_for_user(
    parent_course_data_id: i32,
    connection: &mut PgConnection,
) -> Result<Vec<CategoryData>, String> {
    let categoriy_data =
        match categories::get_user_categories(parent_course_data_id, connection).await {
            Ok(cat_data) => cat_data,
            Err(error) => return Err(format!("{}", error)),
        };

    let mut results: Vec<CategoryData> = Vec::new();
    for cat_data in categoriy_data {
        results.push(CategoryData {
            category: match categories::get_category(cat_data.category_id, connection).await {
                Ok(cat) => cat,
                Err(error) => return Err(format!("{}", error)),
            },
            category_user_data: cat_data,
            subcategories: match cat_data.id {
                Some(id) => {
                    match get_all_subcategories_for_category(id.clone(), connection).await {
                        Ok(subcats) => Some(subcats),
                        Err(error) => return Err(format!("{}", error)),
                    }
                }
                None => return Err(format!("Category data id missing")),
            },
        });
    }

    Ok(results)
}

async fn get_all_subcategories_for_category(
    parent_category_data_id: i32,
    connection: &mut PgConnection,
) -> Result<Vec<SubcategoryData>, String> {
    let subcategory_data =
        match subcategories::get_user_subcategory(parent_category_data_id, connection).await {
            Ok(subcats) => subcats,
            Err(error) => return Err(format!("{}", error)),
        };

    let mut results: Vec<SubcategoryData> = Vec::new();
    for subcat_data in subcategory_data {
        results.push(SubcategoryData {
            subcategory: match subcategories::get_subcategory(
                subcat_data.subcategory_id,
                connection,
            )
            .await
            {
                Ok(subcat) => subcat,
                Err(error) => return Err(error),
            },
            subcategory_user_data: subcat_data,
        });
    }

    Ok(results)
}

struct CourseSkeleton {
    course: courses::Course,
    category_skeletons: Vec<CategorySkeleton>,
}

struct CategorySkeleton {
    category: categories::Category,
    subcategories: Vec<subcategories::Subcategory>,
}

impl CourseSkeleton {
    async fn get_course_skeleton(
        course_id: i32,
        connection: &mut PgConnection,
    ) -> Result<CourseSkeleton, String> {
        let course = match courses::get_course(course_id, connection).await {
            Ok(course) => course,
            Err(error) => return Err(error),
        };

        Ok(CourseSkeleton {
            course: course,
            category_skeletons: match CategorySkeleton::get_category_skeletons(
                course_id, connection,
            )
            .await
            {
                Ok(cat_skeletons) => cat_skeletons,
                Err(error) => return Err(error),
            },
        })
    }
}

impl CategorySkeleton {
    async fn get_category_skeletons(
        course_id: i32,
        connection: &mut PgConnection,
    ) -> Result<Vec<CategorySkeleton>, String> {
        let categories = match categories::get_categories(course_id, connection).await {
            Ok(cats) => cats,
            Err(error) => return Err(error),
        };

        let mut category_skeletons: Vec<CategorySkeleton> = Vec::new();

        for category in categories {
            let category_id = match category.id {
                Some(id) => id,
                None => return Err(format!("Error missing from category")),
            };

            category_skeletons.push(CategorySkeleton {
                category: category,
                subcategories: match subcategories::get_subcategories(category_id, connection).await
                {
                    Ok(subcats) => subcats,
                    Err(error) => return Err(error),
                },
            });
        }

        Ok(category_skeletons)
    }

    async fn insert_skeleton_data(
        &self,
        user_course_id: i32,
        connection: &mut PgConnection,
    ) -> Result<bool, String> {
        match self
            .category
            .add_category_to_course_data(user_course_id, connection)
            .await
        {
            Ok(category_id) => {
                for subcat in &self.subcategories {
                    match subcat
                        .add_subcategory_to_category_data(category_id, connection)
                        .await
                    {
                        Ok(_) => {}
                        Err(error) => return Err(error),
                    }
                }
            }
            Err(error) => return Err(error),
        };

        Ok(true)
    }
}

pub enum AddingCourseResult {
    Success,
    InvalidSessionToken,
    InvalidCourse,
    CourseGettingError,
    DatabaseError(String),
    DuplicateCourse,
}

impl AddingCourseResult {
    pub fn to_string(&self) -> String {
        match self {
            AddingCourseResult::Success => String::from("Success"),
            AddingCourseResult::InvalidSessionToken => String::from("InvalidSessionToken"),
            AddingCourseResult::InvalidCourse => String::from("InvalidCourse"),
            AddingCourseResult::CourseGettingError => String::from("CourseGettingError"),
            AddingCourseResult::DatabaseError(_) => String::from("DatabaseError"),
            AddingCourseResult::DuplicateCourse => String::from("DuplicateCourse"),
        }
    }
}

pub async fn add_course_to_user(
    session_token: session_token::SessionToken,
    course_id: i32,
) -> AddingCourseResult {
    let mut connection = match database::establish_connection_to_database().await {
        Ok(con) => con,
        Err(error) => return AddingCourseResult::DatabaseError(error),
    };

    match session_token.validate_token(&mut connection).await {
        Ok(con) => con,
        Err(_) => return AddingCourseResult::InvalidSessionToken,
    };

    match sqlx::query("SELECT * FROM courses WHERE id = $1")
        .bind(&course_id)
        .fetch_one(&mut connection)
        .await
    {
        Ok(_) => {}
        Err(_) => return AddingCourseResult::InvalidCourse,
    };

    // Check if an active course with this id already exists for this user
    match sqlx::query(
        "SELECT * FROM user_courses WHERE user_id = $1 AND course_id = $2 AND is_active = true",
    )
    .bind(&session_token.user)
    .bind(&course_id)
    .fetch_one(&mut connection)
    .await
    {
        Ok(_) => return AddingCourseResult::DuplicateCourse,
        Err(_) => {}
    };

    // Get skeleton for course
    let course_skeleton =
        match CourseSkeleton::get_course_skeleton(course_id, &mut connection).await {
            Ok(skeleton) => skeleton,
            Err(_) => return AddingCourseResult::CourseGettingError,
        };

    // Insert blanke course data
    match course_skeleton
        .course
        .add_course_to_user(session_token.user, &mut connection)
        .await
    {
        Ok(course_id) => {
            for category in &course_skeleton.category_skeletons {
                match category
                    .insert_skeleton_data(course_id, &mut connection)
                    .await
                {
                    Ok(_) => {}
                    Err(error) => return AddingCourseResult::DatabaseError(error),
                }
            }
        }
        Err(error) => return AddingCourseResult::DatabaseError(error),
    };

    AddingCourseResult::Success
}
