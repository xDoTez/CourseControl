#[macro_use] extern crate rocket;

mod test;

use std::env;
use dotenv::dotenv;
use sqlx::{Connection, Row};

use rocket::serde::json::Json;

#[get("/")]
async fn status() -> Json<Result<String, String>>
{
    dotenv().ok();
    let database_url = match env::var("DATABASE_URL")
    {
        Ok(url) => url,
        Err(error) => return Json(Err(format!("{}", error)))
    };

    let mut connection = match sqlx::postgres::PgConnection::connect(&database_url).await
    {
        Ok(con) => con,
        Err(error) => return Json(Err(format!("{}", error)))
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

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![status])
}
