struct Program
{
    id: Option(i32),
    name: String,
}

struct Course
{
    id: Option(i32),
    program_id: i32,
    name: String,
    semester: i32,
    ects: i32,
}

struct Category
{
    id: Option(i32),
    course_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

struct Subcategory
{
    id: Option(i32),
    category_id: i32,
    name: String,
    points: i32,
    requirement: i32
}

struct UserCourse
{
    id: Option(i32),
    user_id: i32,
    course_id: i32,
}

struct UserCourseCategory
{
    id: Option(i32),
    user_course_id: i32,
    category_id: i32,
    points: Option(i32),
}

struct UserCourseCategorySubcategory
{
    user_course_category_id: i32,
    subcategory_id: i32,
    points: i32
}