#[macro_use] extern crate rocket;

mod test;

mod users;
mod database;
mod regex_checks;

use sqlx::Row;

use rocket::serde::json::Json;

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

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![status])
        .mount("/users/", routes![get_user_by_id, register_user])
}
