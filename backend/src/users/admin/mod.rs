use crate::{database, session_token};
use chrono::{Local, NaiveDateTime};
use sqlx::{prelude::FromRow, PgConnection};

use super::User;

#[derive(FromRow)]
pub struct Admin {
    pub id: i32,
    _user_id: i32,
    _time_added: NaiveDateTime,
}

pub enum AddingNewAdminResult {
    Success,
    InvalidSessionToken,
    RequestMadeByNotAdmin,
    DatabaseError(String),
    UserAlreadyAdmin,
}

impl ToString for AddingNewAdminResult {
    fn to_string(&self) -> String {
        match self {
            AddingNewAdminResult::Success => String::from("Success"),
            AddingNewAdminResult::InvalidSessionToken => String::from("InvalidSessionToken"),
            AddingNewAdminResult::RequestMadeByNotAdmin => String::from("RequestMadeByNotAdmin"),
            AddingNewAdminResult::DatabaseError(_) => String::from("DatabaseError"),
            AddingNewAdminResult::UserAlreadyAdmin => String::from("UserAlreadyAdmin"),
        }
    }
}

impl Admin // impl block for adding new adminss
{
    pub async fn add_new_admin(
        user_id: i32,
        session_token: session_token::SessionToken,
    ) -> AddingNewAdminResult {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => return AddingNewAdminResult::DatabaseError(error),
        };

        match session_token.validate_token(&mut connection).await {
            Ok(valid) => match valid {
                true => {}
                false => return AddingNewAdminResult::InvalidSessionToken,
            },
            Err(error) => {
                println!("{}", error);
                return AddingNewAdminResult::InvalidSessionToken;
            }
        };

        match Admin::check_if_session_token_belongs_to_admin(&session_token, &mut connection).await
        {
            Ok(valid) => match valid {
                true => {}
                false => return AddingNewAdminResult::RequestMadeByNotAdmin,
            },
            Err(error) => return AddingNewAdminResult::DatabaseError(error),
        }

        match Admin::check_if_user_is_admin(user_id, &mut connection).await {
            Ok(does_not_exist) => match does_not_exist {
                true => {}
                false => return AddingNewAdminResult::UserAlreadyAdmin,
            },
            Err(error) => return AddingNewAdminResult::DatabaseError(error),
        }

        // call function to add user to
        match Admin::add_user_to_admin_table(user_id, &mut connection).await {
            Ok(_) => AddingNewAdminResult::Success,
            Err(error) => AddingNewAdminResult::DatabaseError(error),
        }
    }

    pub async fn check_if_session_token_belongs_to_admin(
        session_token: &session_token::SessionToken,
        connection: &mut PgConnection,
    ) -> Result<bool, String> {
        match sqlx::query("SELECT * FROM admins WHERE user_id = $1")
            .bind(&session_token.user)
            .fetch_all(connection)
            .await
        {
            Ok(admins) => match admins.len() {
                0 => Ok(false),
                _ => Ok(true),
            },
            Err(error) => Err(format!("{}", error)),
        }
    }

    async fn check_if_user_is_admin(
        user_id: i32,
        connection: &mut PgConnection,
    ) -> Result<bool, String> {
        match sqlx::query("SELECT * FROM admins WHERE user_id = $1")
            .bind(&user_id)
            .fetch_all(connection)
            .await
        {
            Ok(admins) => match admins.len() {
                0 => Ok(true),
                _ => Ok(false),
            },
            Err(error) => Err(format!("{}", error)),
        }
    }

    async fn add_user_to_admin_table(
        user_id: i32,
        connection: &mut PgConnection,
    ) -> Result<(), String> {
        match sqlx::query("INSERT INTO admins (user_id, time_added) VALUES ($1, $2)")
            .bind(&user_id)
            .bind(&Local::now().naive_local())
            .execute(connection)
            .await
        {
            Ok(_) => Ok(()),
            Err(error) => Err(format!("{}", error)),
        }
    }
}

pub enum GettingAllNonAdminsResult {
    Success(Vec<User>),
    DatabaseError(String),
    InvalidSessionToken,
    RequestMadeByNonAdmin,
}

impl ToString for GettingAllNonAdminsResult {
    fn to_string(&self) -> String {
        match self {
            GettingAllNonAdminsResult::Success(_) => String::from("Success"),
            GettingAllNonAdminsResult::DatabaseError(_) => String::from("DatabaseError"),
            GettingAllNonAdminsResult::InvalidSessionToken => String::from("InvalidSessionToken"),
            GettingAllNonAdminsResult::RequestMadeByNonAdmin => {
                String::from("RequestMadeByNonAdmin")
            }
        }
    }
}

impl Admin // impl block for returning a list of all non-admin users
{
    pub async fn get_all_non_admins(
        session_token: session_token::SessionToken,
    ) -> GettingAllNonAdminsResult {
        let mut connection = match database::establish_connection_to_database().await {
            Ok(database_url) => database_url,
            Err(error) => return GettingAllNonAdminsResult::DatabaseError(error),
        };

        match session_token.validate_token(&mut connection).await {
            Ok(valid) => match valid {
                true => {}
                false => return GettingAllNonAdminsResult::InvalidSessionToken,
            },
            Err(_) => return GettingAllNonAdminsResult::InvalidSessionToken,
        };

        match Admin::check_if_session_token_belongs_to_admin(&session_token, &mut connection).await
        {
            Ok(valid) => match valid {
                true => {}
                false => return GettingAllNonAdminsResult::RequestMadeByNonAdmin,
            },
            Err(error) => return GettingAllNonAdminsResult::DatabaseError(error),
        };

        match Admin::get_all_non_admin_users(&mut connection).await {
            Ok(users) => GettingAllNonAdminsResult::Success(users),
            Err(error) => GettingAllNonAdminsResult::DatabaseError(error),
        }
    }

    async fn get_all_non_admin_users(connection: &mut PgConnection) -> Result<Vec<User>, String> {
        let _users: Vec<User> = match sqlx::query_as("SELECT users.id, username, password, email, datetime_of_creation FROM users LEFT JOIN admins ON users.id = admins.user_id WHERE admins.id IS NULL")
            .fetch_all(connection)
            .await
            {
                Ok(users) => return Ok(users),
                Err(error) => return Err(format!("{}", error))
            };
    }
}
