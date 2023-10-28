#[cfg(test)]
mod tests {
    use std::env;
    // Put unit test for the simplex method here
    #[test]
    fn test_env_variables() 
    {
        let database_url = match env::var("DATABASE_URL")
        {
            Some(url) => assert(true),
            None => assert(false) 
        };
    }
}