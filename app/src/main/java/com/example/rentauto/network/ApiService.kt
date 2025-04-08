package com.example.rentauto.network

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

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse
}
