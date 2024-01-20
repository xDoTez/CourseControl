use super::categories;
use std::fmt::Display;

use super::{session_token, users, CourseDataSortingOptions};
use crate::{courses::subcategories, database};
use chrono::Local;
use rocket::serde::{Deserialize, Serialize};
use sqlx::{Connection, FromRow, PgConnection, Postgres, Row, Transaction};

#[derive(Serialize, Deserialize, FromRow, Clone)]
pub struct Course {
    pub id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

#[derive(Serialize, Deserialize, FromRow, Clone, Copy)]
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

pub async fn get_single_user_course_data(
    session_token: session_token::SessionToken,
    course_id: i32,
    connection: &mut PgConnection,
) -> Result<UserCourse, String> {
    let user_course: UserCourse = match sqlx::query_as("SELECT id, user_id, course_id, is_active FROM user_courses WHERE user_id = $1 AND course_id = $2 AND is_active = true")
        .bind(&session_token.user)
        .bind(&course_id)
        .fetch_one(connection)
        .await
    {
        Ok(user_course) => user_course,
        Err(error) => return Err(format!("{}", error))
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
    InvalidSessionToken,
}

impl Display for GettingAllAddableCourses {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match self {
                GettingAllAddableCourses::Sucess(_) => String::from("Success"),
                GettingAllAddableCourses::DatabaseError(_) => String::from("DatabaseError"),
                GettingAllAddableCourses::InvalidSessionToken =>
                    String::from("InvalidSessionToken"),
            }
        )
    }
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

        match session_token.validate_token(&mut connection).await {
            Ok(_) => {}
            Err(_) => return GettingAllAddableCourses::InvalidSessionToken,
        }

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

#[derive(Deserialize)]
pub struct NewCourse {
    name: String,
    semester: i32,
    ects: i32,
    categories: Vec<categories::NewCategory>,
}

pub enum AddingNewCourseResult {
    Success,
    DatabaseError(String),
    RequestMadeByNonAdmin,
    InvalidSessionToken,
    InsertDatabaseError((Option<i32>, String)),
}

impl ToString for AddingNewCourseResult {
    fn to_string(&self) -> String {
        match self {
            AddingNewCourseResult::Success => String::from("Success"),
            AddingNewCourseResult::DatabaseError(_) => String::from("DatabaseError"),
            AddingNewCourseResult::RequestMadeByNonAdmin => String::from("RequestMadeByNonAdmin"),
            AddingNewCourseResult::InvalidSessionToken => String::from("InvalidSessionToken"),
            AddingNewCourseResult::InsertDatabaseError(_) => String::from("InsertDatabaseError"),
        }
    }
}

impl NewCourse {
    pub async fn add_new_course(
        &mut self,
        session_token: session_token::SessionToken,
        program_ids: Vec<i32>,
    ) -> AddingNewCourseResult {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(con) => con,
            Err(error) => return AddingNewCourseResult::DatabaseError(format!("{}", error)),
        };

        match session_token.validate_token(&mut connection).await {
            Ok(con) => con,
            Err(_) => return AddingNewCourseResult::InvalidSessionToken,
        };

        match users::admin::Admin::check_if_session_token_belongs_to_admin(
            &session_token,
            &mut connection,
        )
        .await
        {
            Ok(valid) => match valid {
                true => {}
                false => return AddingNewCourseResult::RequestMadeByNonAdmin,
            },
            Err(error) => return AddingNewCourseResult::DatabaseError(error),
        };

        let admin: users::admin::Admin =
            match sqlx::query_as("SELECT * FROM admins WHERE user_id = $1")
                .bind(&session_token.user)
                .fetch_one(&mut connection)
                .await
            {
                Ok(admin) => admin,
                Err(error) => return AddingNewCourseResult::DatabaseError(format!("{}", error)),
            };

        let course_id: Option<i32>;
        match self.insert_new_course(&mut connection).await {
            Err(error) => return AddingNewCourseResult::DatabaseError(error),
            Ok(id) => {
                for category in &mut self.categories {
                    category.course_id = Some(id);
                }
                course_id = Some(id);
            }
        }

        let mut subcategories: Vec<subcategories::NewSubcategory> = Vec::new();
        for category in &mut self.categories {
            match category.insert_new_category(&mut connection).await {
                Ok(id) => match &mut category.subcategories {
                    None => {}
                    Some(subcats) => {
                        for subcategory in subcats {
                            subcategory.category_id = Some(id);
                            subcategories.push(subcategory.clone());
                        }
                    }
                },
                Err(error) => {
                    return AddingNewCourseResult::InsertDatabaseError((
                        category.course_id.clone(),
                        error,
                    ))
                }
            }
        }

        for subcategory in &subcategories {
            match subcategory.insert_new_subcategory(&mut connection).await {
                Ok(_) => {}
                Err(error) => {
                    return AddingNewCourseResult::InsertDatabaseError((course_id, error))
                }
            };
        }

        for program_id in program_ids {
            match sqlx::query("INSERT INTO course_progam(course_id, program_id) VALUES ($1, $2)")
                .bind(&course_id)
                .bind(&program_id)
                .execute(&mut connection)
                .await
            {
                Ok(_) => {}
                Err(error) => {
                    return AddingNewCourseResult::InsertDatabaseError((
                        course_id,
                        format!("{}", error),
                    ))
                }
            }
        }

        match NewCourse::associate_course_to_admin(course_id, admin.id, &mut connection).await {
            Ok(_) => {}
            Err(error) => return AddingNewCourseResult::InsertDatabaseError((course_id, error)),
        }

        AddingNewCourseResult::Success
    }

    async fn insert_new_course(&self, connection: &mut PgConnection) -> Result<i32, String> {
        match sqlx::query(
            "INSERT INTO courses(name, semester, ects) VALUES ($1, $2, $3) RETURNING id",
        )
        .bind(&self.name)
        .bind(&self.semester)
        .bind(&self.ects)
        .fetch_one(connection)
        .await
        {
            Ok(insert) => match insert.try_get("id") {
                Ok(id) => Ok(id),
                Err(error) => Err(format!("{}", error)),
            },
            Err(error) => Err(format!("{}", error)),
        }
    }

    pub async fn revert_insert_of_new_course(
        course_id: i32,
        connection: &mut PgConnection,
    ) -> Result<(), String> {
        match sqlx::query("DELETE FROM courses WHERE id = $1")
            .bind(&course_id)
            .execute(connection)
            .await
        {
            Ok(_) => Ok(()),
            Err(error) => Err(format!("{}", error)),
        }
    }

    async fn associate_course_to_admin(
        course_id: Option<i32>,
        admin_id: i32,
        connection: &mut PgConnection,
    ) -> Result<(), String> {
        match course_id {
            Some(course_id) => {
                match sqlx::query(
                    "INSERT INTO admin_course(admin, course, date_added) VALUES ($1, $2, $3)",
                )
                .bind(&admin_id)
                .bind(&course_id)
                .bind(&Local::now().naive_local())
                .execute(connection)
                .await
                {
                    Ok(_) => Ok(()),
                    Err(error) => Err(format!("{}", error)),
                }
            }
            None => Err(String::from("Course_id is null")),
        }
    }
}

pub enum TogglingCourseDataActivityResult {
    Success,
    DatabaseError(String),
    InvalidSessionToken,
    CourseDataNotAssociatedToUser,
    StrangeDatabaseError,
    NoUserCourseIdPassed,
}

impl ToString for TogglingCourseDataActivityResult {
    fn to_string(&self) -> String {
        match &self {
            TogglingCourseDataActivityResult::Success => String::from("Success"),
            TogglingCourseDataActivityResult::DatabaseError(_) => String::from("DatabaseError"),
            TogglingCourseDataActivityResult::InvalidSessionToken => {
                String::from("InvalidSessionToken")
            }
            TogglingCourseDataActivityResult::CourseDataNotAssociatedToUser => {
                String::from("CourseDataNotAssociatedToUser")
            }
            TogglingCourseDataActivityResult::StrangeDatabaseError => {
                String::from("StrangeDatabaseError")
            }
            TogglingCourseDataActivityResult::NoUserCourseIdPassed => {
                String::from("NoUserCourseIdPassed")
            }
        }
    }
}

impl UserCourse // impl block for toggling activity
{
    pub async fn toggle_activity(
        &self,
        session_token: session_token::SessionToken,
    ) -> TogglingCourseDataActivityResult {
        let user_course_id = match &self.id {
            Some(id) => id,
            None => return TogglingCourseDataActivityResult::NoUserCourseIdPassed,
        };

        let mut connection = match database::establish_connection_to_database().await {
            Ok(con) => con,
            Err(error) => {
                return TogglingCourseDataActivityResult::DatabaseError(format!("{}", error))
            }
        };

        match session_token.validate_token(&mut connection).await {
            Ok(_) => {}
            Err(_) => return TogglingCourseDataActivityResult::InvalidSessionToken,
        };

        match UserCourse::get_course_data_by_id_and_session_token(
            *user_course_id,
            &session_token,
            &mut connection,
        )
        .await
        {
            Ok(user_courses) => match user_courses.len() {
                1 => {}
                0 => return TogglingCourseDataActivityResult::CourseDataNotAssociatedToUser,
                _other => return TogglingCourseDataActivityResult::StrangeDatabaseError,
            },
            Err(error) => return TogglingCourseDataActivityResult::DatabaseError(error),
        };

        let activity = !&self.is_active;
        match sqlx::query("UPDATE user_courses SET is_active = $1 WHERE id = $2 AND user_id = $3")
            .bind(&activity)
            .bind(&user_course_id)
            .bind(&session_token.user)
            .execute(&mut connection)
            .await
        {
            Ok(_) => TogglingCourseDataActivityResult::Success,
            Err(error) => TogglingCourseDataActivityResult::DatabaseError(format!("{}", error)),
        }
    }

    async fn get_course_data_by_id_and_session_token(
        user_course_id: i32,
        session_token: &session_token::SessionToken,
        connection: &mut PgConnection,
    ) -> Result<Vec<UserCourse>, String> {
        let user_courses: Vec<UserCourse> =
            match sqlx::query_as("SELECT * FROM user_courses WHERE id = $1 AND user_id = $2")
                .bind(&user_course_id)
                .bind(&session_token.user)
                .fetch_all(connection)
                .await
            {
                Ok(user_courses) => user_courses,
                Err(error) => return Err(format!("{}", error)),
            };

        Ok(user_courses)
    }
}

#[derive(Deserialize)]
pub struct ModifiedCourse {
    id: i32,
    name: String,
    semester: i32,
    ects: i32,
    modified_categories: Vec<categories::ModifiedCategory>,
    new_categories: Vec<categories::NewCategory>,
    deleted_category_ids: Vec<i32>,
}

pub enum ModifyingCourseResult {
    Success,
    DatabaseError(String),
    InvalidSessionToken,
    RequestMadeByNonAdmin,
    TransactionInitializationError,
    TransactionCommitingError,
    NewCategoryInsertionError(String),
    ErrorDeletingCategory(i32),
}

impl ToString for ModifyingCourseResult {
    fn to_string(&self) -> String {
        match self {
            ModifyingCourseResult::Success => String::from("Success"),
            ModifyingCourseResult::DatabaseError(_) => String::from("DatabaseError"),
            ModifyingCourseResult::InvalidSessionToken => String::from("InvalidSessionToken"),
            ModifyingCourseResult::RequestMadeByNonAdmin => String::from("RequestMadeByNonAdmin"),
            ModifyingCourseResult::TransactionInitializationError => {
                String::from("TransactionInitializationError")
            }
            ModifyingCourseResult::TransactionCommitingError => {
                String::from("TransactionCommitingError")
            }
            ModifyingCourseResult::NewCategoryInsertionError(_) => {
                String::from("NewCategoryInsertionError")
            }
            ModifyingCourseResult::ErrorDeletingCategory(_) => {
                String::from("ErrorDeletingCategory")
            }
        }
    }
}

impl ModifiedCourse {
    pub async fn modify_course(
        &mut self,
        session_token: session_token::SessionToken,
    ) -> ModifyingCourseResult {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => return ModifyingCourseResult::DatabaseError(error),
        };

        match session_token.validate_token(&mut connection).await {
            Ok(_) => {}
            Err(_) => return ModifyingCourseResult::InvalidSessionToken,
        };

        match users::admin::Admin::check_if_session_token_belongs_to_admin(
            &session_token,
            &mut connection,
        )
        .await
        {
            Ok(true) => {}
            Ok(false) => return ModifyingCourseResult::RequestMadeByNonAdmin,
            Err(error) => return ModifyingCourseResult::DatabaseError(error),
        };

        let mut transaction = match connection.begin().await {
            Ok(transaction) => transaction,
            Err(_) => return ModifyingCourseResult::TransactionInitializationError,
        };

        match ModifiedCourse::transaction_insert_new_categories(
            &mut self.new_categories,
            self.id,
            &mut transaction,
        )
        .await
        {
            Ok(_) => {}
            Err(error) => return ModifyingCourseResult::NewCategoryInsertionError(error),
        };

        match categories::NewCategory::transaction_delete_categories_by_ids(
            &self.deleted_category_ids,
            &mut transaction,
        )
        .await
        {
            Ok(_) => {}
            Err(error_id) => return ModifyingCourseResult::ErrorDeletingCategory(error_id),
        };

        match ModifiedCourse::transaction_modify_existing_categories(
            &self.modified_categories,
            &mut transaction,
        )
        .await
        {
            Ok(_) => {}
            Err(error) => return ModifyingCourseResult::DatabaseError(error),
        };

        match self.transaction_modify_course_info(&mut transaction).await {
            Ok(_) => {}
            Err(error) => return ModifyingCourseResult::DatabaseError(error),
        };

        match transaction.commit().await {
            Ok(_) => ModifyingCourseResult::Success,
            Err(_) => ModifyingCourseResult::TransactionCommitingError,
        }
    }

    async fn transaction_insert_new_categories(
        categories: &mut Vec<categories::NewCategory>,
        course_id: i32,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        for category in categories {
            category.course_id = Some(course_id);
            match category.transaction_insert_new_category(transaction).await {
                Ok(id) => match &mut category.subcategories {
                    Some(subcategories) => {
                        for subcategory in subcategories {
                            subcategory.category_id = Some(id);
                            match subcategory
                                .transaction_insert_new_subcategory(transaction)
                                .await
                            {
                                Ok(_) => {}
                                Err(error) => return Err(format!("{}", error)),
                            }
                        }
                    }
                    None => {}
                },
                Err(error) => return Err(error),
            }
        }
        Ok(())
    }

    async fn transaction_modify_existing_categories(
        categories: &Vec<categories::ModifiedCategory>,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        for category in categories {
            match category.transaction_modifie(transaction).await {
                categories::ModifyingCategoryResult::Success => {}
                categories::ModifyingCategoryResult::DatabaseError(error) => {
                    return Err(format!("{}", error))
                }
                categories::ModifyingCategoryResult::MissmatchingPoints(category_id) => {
                    return Err(format!(
                        "Points between category with id {} and it's subcategories are not equal",
                        category_id
                    ))
                }
            }
        }

        Ok(())
    }

    async fn transaction_modify_course_info(
        &self,
        transaction: &mut Transaction<'_, Postgres>,
    ) -> Result<(), String> {
        match sqlx::query("UPDATE courses SET name = $1, semester = $2, ects = $3 WHERE id = $4")
            .bind(&self.name)
            .bind(&self.semester)
            .bind(&self.ects)
            .bind(&self.id)
            .execute(&mut **transaction)
            .await
        {
            Ok(_) => Ok(()),
            Err(error) => Err(format!("{}", error)),
        }
    }
}

#[derive(Serialize)]
pub struct CourseTemplate {
    course: Course,
    categories: Vec<categories::CategoryTemplate>,
}

// Need to get all of the courses so admins can modify them
pub enum GettingCoursesForModification {
    Success(Vec<CourseTemplate>),
    DatabaseError(String),
    InvalidSessionToken,
    RequestNotMadeByAdmin,
}

impl ToString for GettingCoursesForModification {
    fn to_string(&self) -> String {
        match self {
            GettingCoursesForModification::Success(_) => String::from("Success"),
            GettingCoursesForModification::DatabaseError(_) => String::from("DatabaseError"),
            GettingCoursesForModification::InvalidSessionToken => {
                String::from("InvalidSessionToken")
            }
            GettingCoursesForModification::RequestNotMadeByAdmin => {
                String::from("RequestNotMadeByAdmin")
            }
        }
    }
}

impl CourseTemplate {
    pub async fn get_courses_for_modification(
        program_id: i32,
        session_token: session_token::SessionToken,
    ) -> GettingCoursesForModification {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => {
                return GettingCoursesForModification::DatabaseError(format!("{}", error))
            }
        };

        match session_token.validate_token(&mut connection).await {
            Ok(_) => {}
            Err(_) => return GettingCoursesForModification::InvalidSessionToken,
        };

        match users::admin::Admin::check_if_session_token_belongs_to_admin(
            &session_token,
            &mut connection,
        )
        .await
        {
            Ok(true) => {}
            Ok(false) => return GettingCoursesForModification::RequestNotMadeByAdmin,
            Err(error) => return GettingCoursesForModification::DatabaseError(error),
        };

        let courses: Vec<Course> = match sqlx::query_as("SELECT c.id, c.name, c.semester, c.ects FROM course_progam JOIN courses as c ON course_progam.course_id = c.id WHERE course_progam.program_id = $1")
            .bind(&program_id)
            .fetch_all(&mut connection)
            .await
        {
            Ok(courses) => courses,
            Err(error) => return GettingCoursesForModification::DatabaseError(format!("{}", error))
        };

        let mut course_templates: Vec<CourseTemplate> = Vec::new();
        for course in courses {
            match course.id {
                None => {
                    return GettingCoursesForModification::DatabaseError(String::from(
                        "Course did not have an ID",
                    ))
                }
                Some(course_id) => {
                    match categories::CategoryTemplate::get_categories(course_id, &mut connection)
                        .await
                    {
                        Ok(cats) => course_templates.push(CourseTemplate {
                            course: course,
                            categories: cats,
                        }),
                        Err(error) => return GettingCoursesForModification::DatabaseError(error),
                    }
                }
            }
        }

        GettingCoursesForModification::Success(course_templates)
    }
}
