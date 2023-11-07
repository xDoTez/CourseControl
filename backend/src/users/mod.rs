use serde::{Serialize, Deserialize};
use sqlx::{FromRow, PgConnection};
use chrono::NaiveDateTime;

use crate::database;
use crate::regex_checks;

#[derive(FromRow, Debug, Serialize, Deserialize)]
pub struct User
{
    id: Option<i32>,
    username: String,
    password: String,
    email: String,
    datetime_of_creation: Option<NaiveDateTime>
}

#[derive(Serialize, Deserialize)]
pub struct UserCredentials
{
    username: String,
    password: String,
    email: String
}

#[derive(Serialize)]
pub enum UserRegistrationResult
{
    SuccessfulRegistration,
    UsernameInvalid,
    EmailInvalid,
    UsernameUnique,
    UsernameDuplicate,
    EmailUnique,
    EmailDuplicate,
    PasswordInvalid,
    CredentialsValid,
    DatabaseError(String),
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

    pub async fn register_user(user_credentials: UserCredentials) -> UserRegistrationResult
    {
        let mut connection =  match database::establish_connection_to_database().await
        {
            Ok(database_url) => database_url,
            Err(error) => return UserRegistrationResult::DatabaseError(error)
        };

        match User::check_validity_of_user_credentials(&user_credentials)
        {
            UserRegistrationResult::CredentialsValid => (),
            other_result => return other_result
        };

        match User::check_username_duplicate(&user_credentials, &mut connection).await
        {
            UserRegistrationResult::UsernameUnique => (),
            other_result => return other_result
        };

        match User::check_email_duplicate(&user_credentials, &mut connection).await
        {
            UserRegistrationResult::EmailUnique => (),
            other_result => return other_result
        }
        
        UserRegistrationResult::SuccessfulRegistration
    }

    fn check_validity_of_user_credentials(user_credentials: &UserCredentials) -> UserRegistrationResult
    {
        match regex_checks::perform_regex_check(r"([A-z,0-9]{6,})\w+", &user_credentials.username)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::UsernameInvalid
            }
            Err(error) => return UserRegistrationResult::RegexInitializationError(format!("{}", error))
        };
        match regex_checks::perform_regex_check(r"^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$", &user_credentials.password)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::PasswordInvalid
            }
            Err(error) => return UserRegistrationResult::RegexInitializationError(format!("{}", error))
        };
        match regex_checks::perform_regex_check(r"^[^@]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$", &user_credentials.email)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::EmailInvalid
            }
            Err(error) => return UserRegistrationResult::RegexInitializationError(format!("{}", error))
        };

        UserRegistrationResult::CredentialsValid
    }

    async fn check_username_duplicate(user_credentials: &UserCredentials, connection: &mut PgConnection) -> UserRegistrationResult
    {
        #[derive(FromRow)]
        struct Username
        {
            username: String
        }
        
        let username_email_rows: Vec<Username> = match sqlx::query_as("SELECT username FROM users WHERE username = $1")
            .bind(user_credentials.username.clone())
            .fetch_all(connection)
            .await
        {
            Ok(results) => results,
            Err(error) => return UserRegistrationResult::DatabaseError(format!("{}", error))
        };

        match username_email_rows.iter().count()
        {
            0 => UserRegistrationResult::UsernameUnique,
            _ => UserRegistrationResult::UsernameDuplicate
        }
    }

    async fn check_email_duplicate(user_credentials: &UserCredentials, connection: &mut PgConnection) -> UserRegistrationResult
    {
        #[derive(FromRow)]
        struct Email
        {
            email: String
        }
        
        let username_email_rows: Vec<Email> = match sqlx::query_as("SELECT email FROM users WHERE email = $1")
            .bind(user_credentials.email.clone())
            .fetch_all(connection)
            .await
        {
            Ok(results) => results,
            Err(error) => return UserRegistrationResult::DatabaseError(format!("{}", error))
        };

        match username_email_rows.iter().count()
        {
            0 => UserRegistrationResult::EmailUnique,
            _ => UserRegistrationResult::EmailDuplicate
        }
    }
}