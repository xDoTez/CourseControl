use rocket::serde::{Serialize, Deserialize};

#[derive(Serialize, Deserialize)]
struct User
{
    id: i32,
    username: String,
    password: u64,
    email: String
}