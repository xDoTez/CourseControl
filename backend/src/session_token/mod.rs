use crate::users::{User, UserLoginResult};
use chrono::{Duration, Local, NaiveDateTime};
use rand::Rng;
use rocket::serde::{Deserialize, Serialize};
use sqlx::{FromRow, PgConnection};

#[derive(FromRow, Serialize, Deserialize)]
pub struct SessionToken {
    pub user: i32,
    pub session_token: String,
    pub expiration: NaiveDateTime,
}

impl SessionToken {
    pub fn new(user_id: i32) -> Self {
        let token: u64 = rand::thread_rng().gen();
        SessionToken {
            user: user_id,
            session_token: token.to_string(),
            expiration: Local::now().naive_local() + Duration::hours(1),
        }
    }

    pub async fn validate_token(&self, connecting: &mut PgConnection) -> Result<bool, String> {
        match sqlx::query(
            "SELECT * FROM sessions WHERE \"user\" = $1 AND session_token = $2 AND expiration > $3",
        )
        .bind(&self.user)
        .bind(&self.session_token)
        .bind(Local::now().naive_local())
        .fetch_one(connecting)
        .await
        {
            Ok(_) => Ok(true),
            Err(error) => Err(format!("{}", error)),
        }
    }

    pub async fn create_session_token(
        user: User,
        connection: &mut PgConnection,
    ) -> UserLoginResult {
        let session_token = match user.id {
            Some(id) => SessionToken::new(id),
            None => return UserLoginResult::MissingData,
        };

        match sqlx::query(
            "INSERT INTO sessions (\"user\", session_token, expiration) VALUES ($1, $2, $3)",
        )
        .bind(&session_token.user)
        .bind(&session_token.session_token)
        .bind(&session_token.expiration)
        .execute(connection)
        .await
        {
            Ok(_) => UserLoginResult::SuccessfulLogin(session_token),
            Err(error) => UserLoginResult::DataBaseError(format!("{}", error)),
        }
    }
}
