#[macro_use]
extern crate rocket;

mod test;

mod courses;
mod database;
mod regex_checks;
mod session_token;
mod users;

use sqlx::Row;

use rocket::serde::{json::Json, Deserialize, Serialize};

#[get("/")]
async fn status() -> Json<Result<String, String>> {
    let mut connection = match database::establish_connection_to_database().await {
        Ok(con) => con,
        Err(error) => return Json(Err(error)),
    };

    let row_result = match sqlx::query("SELECT 1 + 1").fetch_one(&mut connection).await {
        Ok(results) => results,
        Err(error) => return Json(Err(format!("{}", error))),
    };

    let _result: i32 = match row_result.try_get("?column?") {
        Ok(value) => value,
        Err(error) => return Json(Err(format!("{}", error))),
    };

    Json(Ok(format!("Server is operational")))
}

#[get("/<id>")]
async fn get_user_by_id(id: i32) -> Json<Result<users::User, String>> {
    Json(users::User::get_user_by_id(id).await)
}

#[derive(Serialize)]
struct UserRegistrationResult {
    status: users::UserRegistrationResult,
}

#[post("/registration", format = "json", data = "<user_credentials>")]
async fn register_user(
    user_credentials: Json<users::UserCredentials>,
) -> Json<UserRegistrationResult> {
    let user_credentials = user_credentials.into_inner();

    Json(UserRegistrationResult {
        status: users::User::register_user(user_credentials).await,
    })
}

#[derive(Serialize)]
struct UserLoginResult {
    status: String,
    error_message: Option<String>,
    session_token: Option<session_token::SessionToken>,
}

#[post("/login", format = "json", data = "<user_login_credentials>")]
async fn login_user(
    user_login_credentials: Json<users::UserLoginCredentials>,
) -> Json<UserLoginResult> {
    let user_login_credentials = user_login_credentials.into_inner();

    Json(
        match users::User::login_user(user_login_credentials).await {
            users::UserLoginResult::SuccessfulLogin(token) => UserLoginResult {
                status: String::from("SuccessfullLogin"),
                error_message: None,
                session_token: Some(token),
            },
            users::UserLoginResult::DataBaseError(error) => UserLoginResult {
                status: String::from("DataBaseError"),
                error_message: Some(error),
                session_token: None,
            },
            default => UserLoginResult {
                status: default.to_string(),
                error_message: None,
                session_token: None,
            },
        },
    )
}
#[derive(Serialize)]
struct ResponseMessage {
    response: String,
}

#[derive(Serialize)]
struct UserCourseData {
    status: String,
    message: Option<String>,
    data: Option<Vec<courses::CourseData>>,
}

#[post(
    "/course_data?<sorting_option..>",
    format = "json",
    data = "<session_token>"
)]
async fn get_course_data(
    session_token: Json<session_token::SessionToken>,
    sorting_option: Option<String>,
) -> Json<UserCourseData> {
    let session_token: session_token::SessionToken = session_token.into_inner();

    let sorting_option = match sorting_option {
        Some(sort) => courses::CourseDataSortingOptions::from_string(sort),
        None => courses::CourseDataSortingOptions::NameAlphabeticAsc,
    };

    Json(
        match courses::get_all_course_for_user(session_token, true, sorting_option).await {
            courses::UserCourseResult::Success(result) => UserCourseData {
                status: String::from("Success"),
                message: None,
                data: Some(result),
            },
            courses::UserCourseResult::DatabaseError(error) => UserCourseData {
                status: String::from("DatabaseError"),
                message: Some(error),
                data: None,
            },
            default => UserCourseData {
                status: default.to_string(),
                message: None,
                data: None,
            },
        },
    )
}

#[post(
    "/course_data_old?<sorting_option..>",
    format = "json",
    data = "<session_token>"
)]
async fn get_course_data_old(
    session_token: Json<session_token::SessionToken>,
    sorting_option: Option<String>,
) -> Json<UserCourseData> {
    let session_token: session_token::SessionToken = session_token.into_inner();

    let sorting_option = match sorting_option {
        Some(sort) => courses::CourseDataSortingOptions::from_string(sort),
        None => courses::CourseDataSortingOptions::NameAlphabeticAsc,
    };

    Json(
        match courses::get_all_course_for_user(session_token, false, sorting_option).await {
            courses::UserCourseResult::Success(result) => UserCourseData {
                status: String::from("Success"),
                message: None,
                data: Some(result),
            },
            courses::UserCourseResult::DatabaseError(error) => UserCourseData {
                status: String::from("DatabaseError"),
                message: Some(error),
                data: None,
            },
            default => UserCourseData {
                status: default.to_string(),
                message: None,
                data: None,
            },
        },
    )
}

#[derive(Deserialize)]
struct AddingCourseData {
    session_token: session_token::SessionToken,
    course_id: i32,
}

#[derive(Serialize)]
struct AddingCourseResult {
    status: String,
    message: Option<String>,
}

#[post("/add_course_data", format = "json", data = "<adding_course_data>")]
async fn add_course_to_user(
    adding_course_data: Json<AddingCourseData>,
) -> Json<AddingCourseResult> {
    let adding_course_data = adding_course_data.into_inner();

    Json(
        match courses::add_course_to_user(
            adding_course_data.session_token,
            adding_course_data.course_id,
        )
        .await
        {
            courses::AddingCourseResult::DatabaseError(error) => AddingCourseResult {
                status: String::from("DatabaseError"),
                message: Some(error),
            },
            other => AddingCourseResult {
                status: other.to_string(),
                message: None,
            },
        },
    )
}

#[derive(Serialize)]
struct GettingProgramsResult {
    status: String,
    programs: Option<Vec<courses::Program>>,
    message: Option<String>,
}

#[get("/get_all_programs")]
async fn get_all_programs() -> Json<GettingProgramsResult> {
    Json(match courses::Program::get_all_programs().await {
        courses::GettingProgramsResult::Success(programs) => GettingProgramsResult {
            status: String::from("Sucess"),
            programs: Some(programs),
            message: None,
        },
        courses::GettingProgramsResult::DatabaseError(error) => GettingProgramsResult {
            status: String::from("DatabaseError"),
            programs: None,
            message: Some(error),
        },
    })
}

#[derive(Deserialize)]
struct GetAllAddableCoursesParams {
    session_token: session_token::SessionToken,
    program_id: i32,
}

#[derive(Serialize)]
struct GettingAllAddableProgramsResult {
    status: String,
    programs: Option<Vec<courses::courses::Course>>,
    message: Option<String>,
}

#[post(
    "/get_all_addable_courses",
    format = "json",
    data = "<session_token_and_program_id>"
)]
async fn get_all_addable_courses(
    session_token_and_program_id: Json<GetAllAddableCoursesParams>,
) -> Json<GettingAllAddableProgramsResult> {
    let session_token_and_program_id = session_token_and_program_id.into_inner();

    Json(
        match courses::courses::Course::get_all_addable_courses(
            session_token_and_program_id.session_token,
            session_token_and_program_id.program_id,
        )
        .await
        {
            courses::courses::GettingAllAddableCourses::Sucess(programs) => {
                GettingAllAddableProgramsResult {
                    status: String::from("Success"),
                    programs: Some(programs),
                    message: None,
                }
            }
            courses::courses::GettingAllAddableCourses::DatabaseError(error) => {
                GettingAllAddableProgramsResult {
                    status: String::from("DatabaseError"),
                    programs: None,
                    message: Some(error),
                }
            }
            default => GettingAllAddableProgramsResult {
                status: default.to_string(),
                programs: None,
                message: None,
            },
        },
    )
}

#[derive(Deserialize)]
struct CourseDataAndSessionToken {
    session_token: session_token::SessionToken,
    course_data: courses::CourseData
}

#[derive(Serialize)]
struct CourseDataModificationResult {
    status: String,
    message: String
}

#[post("/modify_existing_course_data", format ="json", data = "<course_data>")]
async fn modify_existing_course_data( course_data: Json<CourseDataAndSessionToken>) -> Json<CourseDataModificationResult> {
    // Added the new function created in courses module here
    todo!();
}

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/", routes![status])
        .mount(
            "/users/",
            routes![
                get_user_by_id,
                register_user,
                login_user,
                add_course_to_user
            ],
        )
        .mount("/something", routes![get_course_data, get_course_data_old])
        .mount("/programs", routes![get_all_programs])
        .mount("/courses", routes![get_all_addable_courses])
}
