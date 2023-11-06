use serde::{Serialize, Deserialize};
use sqlx::FromRow;
use chrono::NaiveDateTime;

use crate::database;

#[derive(FromRow, Debug, Serialize, Deserialize)]
pub struct User
{
    id: Option<i32>,
    username: String,
    password: String,
    email: String,
    datetime_of_creation: NaiveDateTime
}

impl User
{
    pub async fn get_user_by_id(id: i32) -> Result<User, String>
    {
        let mut connection =  match database::establish_connection_to_database().await
        {
            Ok(database_url) => database_url,
            Err(error) => return Err(format!("Error while fetching database URL from environment: {}", error))
        };

        let user: User = match sqlx::query_as("SELECT * FROM users WHERE ID = $1").bind(id)
            .fetch_one(&mut connection)
            .await
        {
            Ok(results) => results,
            Err(error) => return Err(format!("Error while fetching user from database: {}", error))
        };

        Ok(user)
    }

    // async fn insert_user_by_id(username: )
}