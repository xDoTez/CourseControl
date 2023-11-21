#[macro_use] extern crate rocket;

mod test;

mod users;
mod courses;
mod database;
mod regex_checks;
mod session_token;

use sqlx::Row;

use rocket::serde::{json::Json, Serialize};

#[get("/")]
async fn status() -> Json<Result<String, String>>
{
    let mut connection = match database::establish_connection_to_database().await
    {
        Ok(con) => con,
        Err(error) => return Json(Err(error))
    };

    let row_result = match sqlx::query("SELECT 1 + 1")
        .fetch_one(&mut connection)
        .await
    {
        Ok(results) => results,
        Err(error) => return Json(Err(format!("{}", error)))
    };

    let _result: i32 = match row_result.try_get("?column?")
    {
        Ok(value) => value,
        Err(error) => return Json(Err(format!("{}", error)))
    };

    Json(Ok(format!("Server is operational")))
}

#[get("/<id>")]
async fn get_user_by_id(id: i32) -> Json<Result<users::User, String>>
{
    Json(users::User::get_user_by_id(id).await)
}

#[post("/registration", format = "json", data = "<user_credentials>")]
async fn register_user(user_credentials: Json<users::UserCredentials>) -> Json<users::UserRegistrationResult>
{
    let user_credentials = user_credentials.into_inner();

    Json(users::User::register_user(user_credentials).await)
}

#[post("/login", format = "json", data = "<user_login_credentials>")]
async fn login_user(user_login_credentials: Json<users::UserLoginCredentials>) -> Json<users::UserLoginResult>
{
    let user_login_credentials = user_login_credentials.into_inner();

    Json(users::User::login_user(user_login_credentials).await)
}

#[derive(Serialize)]
struct ResponseMessage
{
    response: String,
}


#[post("/course_data/", format = "json", data = "<session_token>")]
async fn get_course_data(session_token: Json<session_token::SessionToken>) -> Json<Result<Vec<courses::CourseData>, ResponseMessage>>
{
    let session_token: session_token::SessionToken = session_token.into_inner();

    Json(match courses::get_all_course_for_user(session_token, true).await
        {
            Ok(user_course) => Ok(user_course),
            Err(error) => Err(ResponseMessage{ response: error})
        })
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![status])
        .mount("/users/", routes![get_user_by_id, register_user, login_user])
        .mount("/something", routes![get_course_data])
}
