use serde::{Serialize, Deserialize};
use sqlx::{FromRow, PgConnection};
use chrono::{NaiveDateTime, Local, Duration};
use std::collections::hash_map::DefaultHasher;
use std::fmt::{Display, Debug};
use std::hash::{Hash, Hasher};
use rand::Rng;

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

impl User // impl block for misc routes
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
    DatabaseError,
    RegexInitializationError
}

impl User // impl block for user registrations
{
    pub async fn register_user(user_credentials: UserCredentials) -> UserRegistrationResult
    {
        let mut connection =  match database::establish_connection_to_database().await
        {
            Ok(database_url) => database_url,
            Err(_) => return UserRegistrationResult::DatabaseError
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

        let datetime_of_creation = Local::now().naive_local();
        let hashed_password = User::salt_and_hash_string(&user_credentials.password, &datetime_of_creation);

        match sqlx::query("INSERT INTO users (username, password, email, datetime_of_creation) VALUES ($1, $2, $3, $4)")
            .bind(&user_credentials.username)
            .bind(&hashed_password)
            .bind(&user_credentials.email)
            .bind(&datetime_of_creation)
            .execute(&mut connection).await
        {
            Ok(_) => UserRegistrationResult::SuccessfulRegistration,
            Err(_) => UserRegistrationResult::DatabaseError
        }
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
            Err(_) => return UserRegistrationResult::RegexInitializationError
        };
        match regex_checks::perform_regex_check(r"^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$", &user_credentials.password)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::PasswordInvalid
            }
            Err(_) => return UserRegistrationResult::RegexInitializationError
        };
        match regex_checks::perform_regex_check(r"^[^@]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$", &user_credentials.email)
        {
            Ok(outcome) => match outcome
            {
                true => (),
                false => return UserRegistrationResult::EmailInvalid
            }
            Err(_) => return UserRegistrationResult::RegexInitializationError
        };

        UserRegistrationResult::CredentialsValid
    }

    async fn check_username_duplicate(user_credentials: &UserCredentials, connection: &mut PgConnection) -> UserRegistrationResult
    {
        #[derive(FromRow)]
        struct Username
        {
            #[sqlx(rename = "username")]
            _username: String
        }
        
        let username_email_rows: Vec<Username> = match sqlx::query_as("SELECT username FROM users WHERE username = $1")
            .bind(user_credentials.username.clone())
            .fetch_all(connection)
            .await
        {
            Ok(results) => results,
            Err(_) => return UserRegistrationResult::DatabaseError
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
            #[sqlx(rename = "email")]
            _email: String
        }
        
        let username_email_rows: Vec<Email> = match sqlx::query_as("SELECT email FROM users WHERE email = $1")
            .bind(user_credentials.email.clone())
            .fetch_all(connection)
            .await
        {
            Ok(results) => results,
            Err(_) => return UserRegistrationResult::DatabaseError
        };

        match username_email_rows.iter().count()
        {
            0 => UserRegistrationResult::EmailUnique,
            _ => UserRegistrationResult::EmailDuplicate
        }
    }

    fn salt_and_hash_string<T: Display>(text: &str, salt: &T) -> String
    {
        let salted_text = format!("{}{}", text, salt);
        let mut hasher = DefaultHasher::new();

        salted_text.hash(&mut hasher);
        hasher.finish().to_string()
    }
}

#[derive(Serialize, Deserialize)]
pub struct UserLoginCredentials
{
    username: String,
    password: String
}

#[derive(Serialize)]
pub enum UserLoginResult
{
    SuccessfulLogin(SessionToken),
    InvalidCredentials,
    MissingData,
    DataBaseError(String)
}

#[derive(FromRow, Serialize)]
pub struct SessionToken
{
    user: i32,
    session_token: String,
    expiration: NaiveDateTime
}

impl SessionToken
{
    pub fn new(user_id: i32) -> Self
    {
        let token: u64 = rand::thread_rng().gen();
        SessionToken { user: user_id, session_token: token.to_string(), expiration: Local::now().naive_local() + Duration::hours(1) }
    }
}

impl User // impl block for user login
{
    pub async fn login_user(user_login_credentials: UserLoginCredentials) -> UserLoginResult
    {
        let mut connection =  match database::establish_connection_to_database().await
        {
            Ok(database_url) => database_url,
            Err(error) => return UserLoginResult::DataBaseError(format!("Error while fetching database URL from environment: {}", error))
        };

        let mut users = match User::get_user_by_username(&user_login_credentials.username, &mut connection).await
        {
            Ok(result) => result,
            Err(error) => return UserLoginResult::DataBaseError(format!("{}", error))
        };

        let user = match users.iter().count()
        {
            1 => users.remove(0),
            _ => {println!("No or more than one user with this username were found"); return UserLoginResult::InvalidCredentials}
        };

        let password_hashes_match = match user.datetime_of_creation
        {
            Some(creation_datetime) => user.password.chars()
                .zip(User::salt_and_hash_string(&user_login_credentials.password, &creation_datetime).chars())
                .map(|(x, y)| x == y).filter(|x| !x).count() == 0,
            None => return UserLoginResult::MissingData
        };

        match password_hashes_match
        {
            true => User::create_session_token(user, &mut connection).await,
            false => UserLoginResult::InvalidCredentials
        }
    }

    async fn get_user_by_username(username: &str, connection: &mut PgConnection) -> Result<Vec<User>, String>
    {
        let users: Vec<User> = match sqlx::query_as("SELECT * FROM users WHERE username = $1").bind(username)
            .fetch_all(connection)
            .await
        {
            Ok(result) => result,
            Err(error) => return Err(format!("Error while fetching user from database: {}", error))
        };

        Ok(users)
    }

    async fn create_session_token(user: User, connection: &mut PgConnection) -> UserLoginResult
    {
        let session_token = match user.id
        {
            Some(id) => SessionToken::new(id),
            None => return UserLoginResult::MissingData
        };

        match sqlx::query("INSERT INTO sessions (\"user\", session_token, expiration) VALUES ($1, $2, $3)")
            .bind(&session_token.user)
            .bind(&session_token.session_token)
            .bind(&session_token.expiration)
            .execute(connection).await
        {
            Ok(_) => UserLoginResult::SuccessfulLogin(session_token),
            Err(error) => UserLoginResult::DataBaseError(format!("{}", error))
        }
    } 
}
