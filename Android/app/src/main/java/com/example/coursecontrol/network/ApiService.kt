package com.example.coursecontrol.network

import com.example.coursecontrol.model.AddNewAdmin
import com.example.coursecontrol.model.AddNewCourse
import com.example.coursecontrol.model.Admin
import com.example.coursecontrol.model.ApiResponse
import com.example.coursecontrol.model.ApiResponseAddNewCourse
import com.example.coursecontrol.model.ApiResponseAdminPrivileges
import com.example.coursecontrol.model.CreateNewCourse
import com.example.coursecontrol.model.EditCourseData
import com.example.coursecontrol.model.Email
import com.example.coursecontrol.model.NewCourses
import com.example.coursecontrol.model.ProgramNew
import com.example.coursecontrol.model.ResetCode
import com.example.coursecontrol.model.ResetPassword

import com.example.coursecontrol.model.SetInactive
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class NewCoursesModel(
    val session_token: YourRequestModel,
    val program_id: Int
)
data class UserModel(
    val session_token: YourRequestModel,
    val user_id: Int
)

data class AddNewCourseModel(
    val session_token: YourRequestModel,
    val course_id: Int
)

data class YourRequestModel(
    val user: Int,
    val session_token: String,
    val expiration: String
)

data class NewAdminModel(
    val session_token: YourRequestModel,
    val user_id: Int
)


interface ApiService {
    @POST("http://165.232.76.112:8000/something/course_data")
    suspend fun postUserData(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data_old")
    suspend fun postUserDataHistory(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/admin/add_new_program")
    suspend fun addNewProgram(@Body request: ProgramNew): ApiResponse
    @POST("http://165.232.76.112:8000/courses/toggle_user_course_activity")
    suspend fun setInactive(@Body request: SetInactive): ApiResponse
    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=NameAlphabeticAsc")
    suspend fun sortNameAlphabeticAsc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=NameAlphabeticDesc")
    suspend fun sortNameAlphabeticDesc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=SemesterAsc")
    suspend fun sortSemesterAsc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=SemesterDesc")
    suspend fun sortSemesterDesc(@Body request: YourRequestModel): ApiResponse

    @GET("http://165.232.76.112:8000/programs/get_all_programs")
    suspend fun getAllPrograms(): ApiResponseAddNewCourse

    @POST("http://165.232.76.112:8000/courses/get_all_addable_courses")
    suspend fun getNewCourses(@Body request: NewCoursesModel): NewCourses

    @POST("http://165.232.76.112:8000/users/add_course_data")
    suspend fun addNewCourse(@Body request: AddNewCourseModel): AddNewCourse

    @POST("http://165.232.76.112:8000/users/check_if_user_is_admin")
    suspend fun checkIfAdmin(@Body request: YourRequestModel): Admin

    @POST("http://165.232.76.112:8000/admin/get_all_non_admins")
    suspend fun getAllNonAdmins(@Body request: YourRequestModel): ApiResponseAdminPrivileges

    @POST("http://165.232.76.112:8000/admin/add_new_admin")
    suspend fun addNewAdmin(@Body request: NewAdminModel): AddNewAdmin

    @POST("http://165.232.76.112:8000/courses/modify_existing_course_data")
    suspend fun editCourseData(@Body request: EditCourseData): AddNewCourse

    @POST("http://165.232.76.112:8000/users/send_reset_code")
    suspend fun sendEmail(@Body request: Email): ResetCode

    @POST("http://165.232.76.112:8000/users/reset_password")
    suspend fun resetPassword(@Body request: ResetPassword): AddNewCourse

    @POST("http://165.232.76.112:8000/admin/add_new_course")
    suspend fun createNewCourse(@Body request: CreateNewCourse): AddNewCourse
}