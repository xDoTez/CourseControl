use sqlx::Connection;

use std::env;
use dotenv::dotenv; // Used for development, not needed in production

fn get_database_url() -> Result<String, env::VarError>
{
    dotenv().ok();
    
    env::var("DATABASE_URL")
}

pub async fn establish_connection_to_database() -> Result<sqlx::PgConnection, String>
{
    let database_url = match get_database_url()
    {
        Ok(database_url) => database_url,
        Err(error) => return Err(format!("Error while fetching database URL from environment: {}", error))
    };

    match sqlx::postgres::PgConnection::connect(&database_url).await
    {
        Ok(connection) => Ok(connection),
        Err(error) => return Err(format!("Failed to establish database connection: {}", error))
    }
}

// pub async fn fetch_one_row(query: &str) -> Result<PgRow, String>
// {
//     let mut connection =  match establish_connection_to_database(match get_database_url()
//         {
//             Ok(database_url) => database_url,
//             Err(error) => return Err(format!("Error while fetching database URL from environment: {}", error))
//         }).await
//     {
//         Ok(con) => con,
//         Err(error) => return Err(format!("Failed to establish database connection: {}", error))
//     };

//     match sqlx::query(query)
//         .fetch_one(&mut connection)
//         .await
//     {
//         Ok(results) => return Ok(results),
//         Err(error) => return Err(format!("Error while querying the database: {}", error))
//     };
// }

