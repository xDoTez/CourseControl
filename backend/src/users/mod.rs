use rocket::outcome;
use serde::{Serialize, Deserialize};
use sqlx::FromRow;
use chrono::NaiveDateTime;
use regex::Regex;

use crate::database;
use crate::regex_checks;

#[derive(FromRow, Debug, Serialize, Deserialize)]
pub struct User
{
    id: Option<i32>,
    username: String,
    password: String,
    email: String,
    datetime_of_creation: NaiveDateTime
}

pub enum UserRegistrationResult
{
    SuccessfulRegistration,
    UsernameInvalid,
    UsernameDuplicate,
    EmailInvalid,
    EmailDuplicate,
    PasswordInvalid,
    RegexInitializationError(String)
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

    // username regex: "([A-z,0-9]{6,})\w+"
    // password regex: "^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$"
    // email regex: "^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$"
    async fn insert_user_by_id(username: String, password: String, email: String) -> UserRegistrationResult
    {
        match regex_checks::perform_regex_check(r"([A-z,0-9]{6,})\w+", &username)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::UsernameInvalid
            }
            Err(error) => return UserRegistrationResult::RegexInitializationError(format!("{}", error))
        };
        // implement regex checks for user name and password
        todo!();
    }
}