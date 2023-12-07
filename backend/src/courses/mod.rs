mod courses;
mod categories;
mod subcategories;

use std::fmt::Display;

use rocket::serde::Serialize;
use sqlx::PgConnection;
use crate::database;
use crate::session_token;
use itertools::iproduct;

struct Program
{
    id: Option<i32>,
    name: String,
}

#[derive(Serialize)]
pub struct CourseData
{
    course: courses::Course,
    course_user_data: courses::UserCourse,
    catagories: Option<Vec<CategoryData>>
}

// comment careogories

#[derive(Serialize)]
pub struct CategoryData
{
    category: categories::Category,
    category_user_data: categories::CourseCategory,
    subcategories: Option<Vec<SubcategoryData>>
}

#[derive(Serialize)]
pub struct SubcategoryData
{
    subcategory: subcategories::Subcategory,
    subcategory_user_data: subcategories::CategorySubcategory
}

pub enum UserCourseResult
    {
        DatabaseConnectionError,
        InvalidSessionToken,
        DatabaseError(String),
        Success(Vec<CourseData>)
    }

impl Display for UserCourseResult
{
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result 
    {
        match self
        {
                UserCourseResult::DatabaseError(_) => write!(f, "DatabaseError"),
                UserCourseResult::Success(_) => write!(f, "Success"),
                UserCourseResult::DatabaseConnectionError => write!(f, "DatabaseConnectionError"),
                UserCourseResult::InvalidSessionToken => write!(f, "InvalidSessionToken")
        }
    }
}


#[derive(FromFormField, Debug)]
pub enum CourseDataSortingOptions
    {
        NameAlphabeticAsc,
        NameAlphabeticDesc,
        SemesterAsc,
        SemesterDesc
    }

impl CourseDataSortingOptions
{
    pub fn from_string(string: String) -> Self
    {
        match string.as_str()
            {
                "NameAlphabeticAsc" => CourseDataSortingOptions::NameAlphabeticAsc,
                "NameAlphabeticDesc" => CourseDataSortingOptions::NameAlphabeticDesc,
                "SemesterAsc" => CourseDataSortingOptions::SemesterAsc,
                "SemesterDesc" => CourseDataSortingOptions::SemesterDesc,
                &_ => CourseDataSortingOptions::NameAlphabeticDesc
            }
    }

    pub fn to_query_sorting_clause(&self) -> String
    {
        match self
            {
                CourseDataSortingOptions::NameAlphabeticAsc => String::from(" ORDER BY courses.name ASC"),
                CourseDataSortingOptions::NameAlphabeticDesc => String::from(" ORDER BY courses.name DESC"),
                CourseDataSortingOptions::SemesterAsc => String::from(" ORDER BY courses.semester ASC"),
                CourseDataSortingOptions::SemesterDesc => String::from(" ORDER BY courses.semester DESC")
            }
    }
}

pub async fn get_all_course_for_user(session_token: session_token::SessionToken, is_active: bool, sorting_option: CourseDataSortingOptions) -> UserCourseResult
{
    let mut connection = match database::establish_connection_to_database().await
        {
            Ok(con) => con,
            Err(_) => return UserCourseResult::DatabaseConnectionError
        };

    match session_token.validate_token(&mut connection).await
        {
            Ok(_) => {},
            Err(_) => return UserCourseResult::InvalidSessionToken
        };

    let courses_data = match courses::get_user_course_data(session_token, is_active, &mut connection, &sorting_option)
        .await
    {
        Ok(data) => data,
        Err(error) => return UserCourseResult::DatabaseError(error)
    };

    let course_ids: Vec<i32> = courses_data.iter().map(|x| x.course_id).collect();
    let courses = match courses::get_courses(course_ids, &mut connection, &sorting_option).await
    {
        Ok(courses) => courses,
        Err(error) => return UserCourseResult::DatabaseError(error)
    };

    let mut results: Vec<CourseData> = Vec::new();
    for (course_data, course) in iproduct!(courses_data, courses).filter(|(x, y)| match y.id { Some(id) => id == x.course_id, None => false })
    {
        results.push(CourseData 
            { course: course, course_user_data: course_data, catagories: match course_data.id 
                { 
                    Some(id) => Some(match get_all_categories_for_user(id, &mut connection).await
                            {
                                Ok(cats) => cats,
                                Err(error) => return UserCourseResult::DatabaseError(error)
                            }), 
                    None => None
                }
            }
        );
    }

    UserCourseResult::Success(results)
}

async fn get_all_categories_for_user(parent_course_data_id: i32, connection: &mut PgConnection) -> Result<Vec<CategoryData>, String>
{
    let categoriy_data = match categories::get_user_categories(parent_course_data_id, connection).await
        {
            Ok(cat_data) => cat_data,
            Err(error) => return Err(format!("{}", error))
        };

    let mut results: Vec<CategoryData> = Vec::new();
    for cat_data in categoriy_data
        {
            results.push(CategoryData 
                { 
                    category: match categories::get_categories(cat_data.category_id, connection).await
                        {
                            Ok(cat) => cat,
                            Err(error) => return Err(format!("{}", error))
                        }, 
                    category_user_data: cat_data, 
                    subcategories: match cat_data.id
                    {
                        Some(id) => {match get_all_subcategories_for_category(id.clone(), connection).await
                        {
                            Ok(subcats) => Some(subcats),
                            Err(error) => return Err(format!("{}", error))
                        }},
                        None => return Err(format!("Category data id missing"))
                    },
                }
            );
        }

    Ok(results)
}

async fn get_all_subcategories_for_category(parent_category_data_id: i32, connection: &mut PgConnection) -> Result<Vec<SubcategoryData>, String>
{
    let subcategory_data = match subcategories::get_user_subcategory(parent_category_data_id, connection).await
    {
        Ok(subcats) => subcats,
        Err(error) => return Err(format!("{}", error))
    };

    let mut results: Vec<SubcategoryData> = Vec::new();
    for subcat_data in subcategory_data
    {
        results.push(SubcategoryData 
            {
                subcategory: match subcategories::get_subcategory(subcat_data.subcategory_id, connection).await
                    {
                        Ok(subcat) => subcat,
                        Err(error) => return Err(error)
                    }, 
                subcategory_user_data: subcat_data
            });
    }

    Ok(results)
}
