#[cfg(test)]
mod tests {
    use dotenv::dotenv;
    use sqlx::{Connection, Row};
    use std::env;
    // Put unit test for the simplex method here
    #[test]
    fn test_env_variables() {
        dotenv().ok();
        match env::var("DATABASE_URL") {
            Ok(_) => assert!(true),
            Err(_) => assert!(false),
        };
    }

    #[tokio::test]
    async fn test_database_connection() {
        dotenv().ok();
        let database_url = match env::var("DATABASE_URL") {
            Ok(url) => url,
            Err(_) => {
                assert!(false);
                return ();
            }
        };

        let mut connection = match sqlx::postgres::PgConnection::connect(&database_url).await {
            Ok(con) => con,
            Err(_) => {
                assert!(false);
                return ();
            }
        };

        let query = match sqlx::query("SELECT 1 + 1").fetch_one(&mut connection).await {
            Ok(results) => results,
            Err(_) => {
                assert!(false);
                return ();
            }
        };

        for column in query.columns() {
            println!("{:?}", column)
        }

        let value: i32 = query.get("?column?");

        assert_eq!(2, value);
    }
}
