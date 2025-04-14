package com.example.rentauto.network

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class LoginResponse(
    val success: Boolean,
    val user_id: Int?,
    val name: String?,
    val status: String?,
    val message: String? = null
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String,
)

data class AdminLoginResponse(
    val success: Boolean,
    val admin_id: Int?,
    val username: String?,
    val status: String?,
    val message: String? = null
)

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("register.php")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): LoginResponse

    @FormUrlEncoded
    @POST("adminLogin.php")
    suspend fun loginAdmin(
        @Field("username") username: String,
        @Field("password") password: String
    ): AdminLoginResponse
}