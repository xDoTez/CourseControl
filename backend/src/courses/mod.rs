struct Program
{
    id: Option<i32>,
    name: String,
}

struct Course
{
    id: Option<i32>,
    name: String,
    semester: i32,
    ects: i32,
}

struct Category
{
    id: Option<i32>,
    course_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

struct Subcategory
{
    id: Option<i32>,
    category_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

pub struct UserCourse
{
    id: Option<i32>,
    user_id: i32,
    course_id: i32,
    categories: Option<Vec<CourseCategory>>,
    is_active: bool
}

pub struct CourseCategory
{
    id: Option<i32>,
    user_course_id: i32,
    category_id: i32,
    points: i32,
    subcategories: Option<Vec<CategorySubcategory>>
}

pub struct CategorySubcategory
{
    user_course_category_id: i32,
    subcategory_id: i32,
    points: i32
}

pub fn get_all_course_for_user(user_id: i32) -> UserCourse
{
    let user_course_data: UserCourse;
    todo!();
}
