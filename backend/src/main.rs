#[macro_use] extern crate rocket;

use std::env;
use std::fmt::Display;
use dotenv::dotenv;
use sqlx::{FromRow, Connection};

use rocket::serde::Serialize;
use rocket::serde::json::Json;


#[derive(FromRow, Serialize)]
struct User
{
    id: Option<i32>,
    #[sqlx(rename = "userName")]
    username: String,
    password: String,
    email: String
}

impl Display for User
{
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self.id
        {
            Some(id) => write!(f, "Username: {}\nEmail: {}\nPassword: {}\nId: {}", self.username, self.email, self.password, id),
            None => write!(f, "Username: {}\nEmail: {}\nPassword: {}", self.username, self.email, self.password)
        }
    }
}

#[get("/")]
async fn read() -> Json<Result<User, String>>
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

    let user: User = match sqlx::query_as("SELECT * FROM Public.\"Users\"")
        .fetch_one(&mut connection)
        .await
    {
        Ok(results) => results,
        Err(error) => return Json(Err(format!("{}", error)))
    };

    Json(Ok(user))
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![read])
}
