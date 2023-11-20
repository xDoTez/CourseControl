use rocket::serde::Serialize;
use sqlx::FromRow;
use chrono::{NaiveDateTime, Duration, Local};
use rand::Rng;

#[derive(FromRow, Serialize)]
pub struct SessionToken
{
    pub user: i32,
    pub session_token: String,
    pub expiration: NaiveDateTime
}

impl SessionToken
{
    pub fn new(user_id: i32) -> Self
    {
        let token: u64 = rand::thread_rng().gen();
        SessionToken { user: user_id, session_token: token.to_string(), expiration: Local::now().naive_local() + Duration::hours(1) }
    }
}

