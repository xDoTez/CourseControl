use crate::{database, session_token};
use chrono::{Local, NaiveDateTime};
use sqlx::{prelude::FromRow, PgConnection};

#[derive(FromRow)]
pub struct Admin {
    id: i32,
    user_id: i32,
    time_added: NaiveDateTime,
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

impl Admin // impl block for adding new admins
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

        match Admin::check_if_session_token_belongs_to_admin(session_token, &mut connection).await {
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

    async fn check_if_session_token_belongs_to_admin(
        session_token: session_token::SessionToken,
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
