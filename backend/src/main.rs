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
    course_data: courses::CourseData,
}

#[derive(Serialize)]
struct CourseDataModificationResult {
    status: String,
    message: Option<String>,
}

#[post(
    "/modify_existing_course_data",
    format = "json",
    data = "<course_data>"
)]
async fn modify_existing_course_data(
    course_data: Json<CourseDataAndSessionToken>,
) -> Json<CourseDataModificationResult> {
    // Added the new function created in courses module here
    let course_data = course_data.into_inner();

    let result =
        courses::modify_user_course_data(course_data.course_data, course_data.session_token).await;
    Json(match &result {
        courses::ModifyUserCourseDataResult::DatabaseError(error) => CourseDataModificationResult {
            status: result.to_string(),
            message: Some(error.clone()),
        },
        courses::ModifyUserCourseDataResult::CategoryGettingError(error) => {
            CourseDataModificationResult {
                status: result.to_string(),
                message: Some(error.clone()),
            }
        }
        courses::ModifyUserCourseDataResult::UnequalCourseData(error) => {
            CourseDataModificationResult {
                status: result.to_string(),
                message: Some(error.to_string()),
            }
        }
        courses::ModifyUserCourseDataResult::InvalidChangedData(error) => {
            CourseDataModificationResult {
                status: result.to_string(),
                message: Some(error.to_string()),
            }
        }
        courses::ModifyUserCourseDataResult::DataModificationError(error) => {
            CourseDataModificationResult {
                status: result.to_string(),
                message: Some(error.to_string()),
            }
        }
        other => CourseDataModificationResult {
            status: other.to_string(),
            message: None,
        },
    })
}

#[derive(Deserialize)]
struct UserIdSessionToken {
    user_id: i32,
    session_token: session_token::SessionToken,
}

#[derive(Serialize)]
struct AdminAddingResult {
    status: String,
    message: Option<String>,
}

#[post(
    "/add_new_admin",
    format = "json",
    data = "<user_id_and_session_token>"
)]
async fn add_new_admin(
    user_id_and_session_token: Json<UserIdSessionToken>,
) -> Json<AdminAddingResult> {
    let user_id_and_session_token = user_id_and_session_token.into_inner();

    let result = users::admin::Admin::add_new_admin(
        user_id_and_session_token.user_id,
        user_id_and_session_token.session_token,
    )
    .await;

    Json(match &result {
        users::admin::AddingNewAdminResult::DatabaseError(error) => AdminAddingResult {
            status: result.to_string(),
            message: Some(error.clone()),
        },
        other => AdminAddingResult {
            status: other.to_string(),
            message: None,
        },
    })
}

#[derive(Serialize)]
struct GettingAllNonAdminsResult {
    status: String,
    message: Option<String>,
    users: Option<Vec<users::User>>,
}

#[post("/get_all_non_admins", format = "json", data = "<session_token>")]
async fn get_all_non_admins(
    session_token: Json<session_token::SessionToken>,
) -> Json<GettingAllNonAdminsResult> {
    let session_token = session_token.into_inner();

    let result = users::admin::Admin::get_all_non_admins(session_token).await;

    Json(match &result {
        users::admin::GettingAllNonAdminsResult::Success(users) => GettingAllNonAdminsResult {
            status: result.to_string(),
            message: None,
            users: Some(users.to_vec()),
        },
        users::admin::GettingAllNonAdminsResult::DatabaseError(error) => {
            GettingAllNonAdminsResult {
                status: result.to_string(),
                message: Some(error.clone()),
                users: None,
            }
        }
        other => GettingAllNonAdminsResult {
            status: other.to_string(),
            message: None,
            users: None,
        },
    })
}

#[derive(Deserialize)]
struct AddingNewCourseStruct {
    session_token: session_token::SessionToken,
    new_course: courses::courses::NewCourse,
    program_id: Vec<i32>,
}

#[derive(Serialize)]
struct AddingNewCourseResult {
    status: String,
    message: Option<String>,
}

#[post(
    "/add_new_course",
    format = "json",
    data = "<adding_new_course_struct>"
)]
async fn add_new_course(
    adding_new_course_struct: Json<AddingNewCourseStruct>,
) -> Json<AddingNewCourseResult> {
    let mut data = adding_new_course_struct.into_inner();

    let result = data
        .new_course
        .add_new_course(data.session_token, data.program_id)
        .await;

    Json(match &result {
        courses::courses::AddingNewCourseResult::DatabaseError(error) => AddingNewCourseResult {
            status: result.to_string(),
            message: Some(error.clone()),
        },
        courses::courses::AddingNewCourseResult::InsertDatabaseError((id, error)) => match id {
            Some(id) => {
                let mut connection = match database::establish_connection_to_database().await {
                    Ok(database_url) => database_url,
                    Err(error) => {
                        return Json(AddingNewCourseResult {
                            status: String::from("DatabaseError"),
                            message: Some(format!("{}", error)),
                        })
                    }
                };

                match courses::courses::NewCourse::revert_insert_of_new_course(*id, &mut connection)
                    .await
                {
                    Ok(_) => AddingNewCourseResult {
                        status: result.to_string(),
                        message: Some(error.clone()),
                    },
                    Err(error_2) => AddingNewCourseResult {
                        status: String::from("DatabaseError"),
                        message: Some(error_2),
                    },
                }
            }
            None => AddingNewCourseResult {
                status: String::from("DatabaseError"),
                message: Some(String::from("Completly unexpected null")),
            },
        },
        other => AddingNewCourseResult {
            status: other.to_string(),
            message: None,
        },
    })
}

#[derive(Deserialize)]
struct AddingNewProgramStruct {
    program: courses::Program,
    session_token: session_token::SessionToken,
}

#[derive(Serialize)]
struct AddingNewProgramResult {
    status: String,
    message: Option<String>,
}

#[post("/add_new_program", format = "json", data = "<data>")]
async fn add_new_program(data: Json<AddingNewProgramStruct>) -> Json<AddingNewProgramResult> {
    let data = data.into_inner();

    let result = data.program.add_new_program(data.session_token).await;

    Json(match result {
        courses::AddingNewProgramResult::DatabaseError(error) => AddingNewProgramResult {
            status: String::from("DatabaseError"),
            message: Some(error.clone()),
        },
        courses::AddingNewProgramResult::Success => AddingNewProgramResult {
            status: String::from("Success"),
            message: None,
        },
        courses::AddingNewProgramResult::RequestmadeByNonAdminUser => AddingNewProgramResult {
            status: String::from("RequestmadeByNonAdminUser"),
            message: None,
        },
    })
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
        .mount(
            "/courses",
            routes![get_all_addable_courses, modify_existing_course_data],
        )
        .mount(
            "/admin",
            routes![
                add_new_admin,
                get_all_non_admins,
                add_new_course,
                add_new_program
            ],
        )
}
