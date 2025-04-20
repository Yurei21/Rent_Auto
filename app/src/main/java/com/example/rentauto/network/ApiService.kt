package com.example.rentauto.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

data class LoginResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: Int?,
    val name: String?,
    val status: String?,
    val message: String? = null
)

data class AdminLoginResponse(
    val success: Boolean,
    val adminId: Int?,
    val username: String?,
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

data class AdminRegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class Vehicle(
    @SerializedName("vehicle_id") val vehicleId: Int,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("year") val year: Int,
    @SerializedName("rent_price") val rentPrice: Double,
    @SerializedName("availability_status") val availabilityStatus: String,
    @SerializedName("car_url") val carUrl: String
)

data class VehicleResponse(
    val success: Boolean,
    val vehicles: List<Vehicle>
)

data class RentalRecord(
    @SerializedName("user_name") val username: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("rental_start_date") val rentalStart: String,
    @SerializedName("rental_end_date") val rentalEnd: String,
    @SerializedName("total_cost") val totalCost: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("carstatus") val carStatus: String
)

data class RentalResponse(
    val success: Boolean,
    val records: List<RentalRecord>?
)

data class PaymentRecord(
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("rental_id") val rentalId: Int,
    @SerializedName("amount_paid") val amountPaid: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_date") val paymentDate: String,
    @SerializedName("pay_status") val payStatus: String,
    @SerializedName("additionalOrLate_fee") val additionalOrLateFee: Double,
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("user_name") val username: String,
    @SerializedName("vehicle_model") val vehicleModel: String,
    @SerializedName("vehicle_brand") val vehicleBrand: String
)

data class PaymentResponse(
    val success: Boolean,
    val payments: List<PaymentRecord>?
)

data class Barcode(
    val success: Boolean,
    val message: String? = null
)

data class ModifyCarResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class SingleVehicleResponse(
    val success: Boolean,
    val vehicle: Vehicle?
)

data class DeleteCarResponse (
    val success: Boolean,
    val message: String? = null
)

data class AddCarResponse (
    val success: Boolean,
    val message: String? = null
)

data class PaymentRentalRecord(
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("amount_paid") val amountPaid: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_date") val paymentDate: String,
    @SerializedName("pay_status") val payStatus: String,
    @SerializedName("additionalOrLate_fee") val additionalOrLateFee: Double,
    @SerializedName("rental_id") val rentalId: Int,
    @SerializedName("rental_start_date") val rentalStartDate: String,
    @SerializedName("rental_end_date") val rentalEndDate: String,
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("rental_payment_status") val rentalPaymentStatus: String,
    @SerializedName("rental_status") val rentalStatus: String,
    @SerializedName("carstatus") val carStatus: String,
    @SerializedName("vehicle_model") val vehicleModel: String,
    @SerializedName("vehicle_brand") val vehicleBrand: String
)

data class UserRecordsResponse(
    val success: Boolean,
    val data: List<PaymentRentalRecord>?
)

data class UserDocument(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("document_url") val documentUrl: String?,
    @SerializedName("local_image_path") val localImagePath: String?
)

data class UserProfile(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String?,
    @SerializedName("status") val status: String,
    val documents: List<UserDocument>? = null
)

data class ProfileResponse (
    val success: Boolean,
    val message: String?,
    val data: UserProfile
)

data class RentResponse (
    val success: Boolean,
    val message: String,
    val rentalId: Int?,
    val barcode: Int?
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

    @POST("adminRegister.php")
    suspend fun registerAdmin(
        @Body request: AdminRegisterRequest
    ): AdminLoginResponse

    @GET("viewCars.php")
    suspend fun viewCars(): VehicleResponse

    @GET("getAllRecords.php")
    suspend fun viewRentals(): RentalResponse

    @GET("getAllPayments.php")
    suspend fun viewPayments(): PaymentResponse

    @FormUrlEncoded
    @POST("returnCar.php")
    suspend fun returnCar(
        @Field("barcode") barcode: String,
        @Field("additionalFee") additionalFee: String
    ): Barcode

    @GET("getCarById.php")
    suspend fun getCarById(@Query("id") id: Int): SingleVehicleResponse

    @POST("modifyCar.php")
    suspend fun updateCar(@Body vehicle: Vehicle): ModifyCarResponse

    @POST("deleteCar.php")
    suspend fun deleteCar(@Body vehicleId: Map<String, Int>): DeleteCarResponse

    @Multipart
    @POST("addCar.php")
    suspend fun addCar(
        @Part image: MultipartBody.Part,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): AddCarResponse

    @GET("getPaymentUser.php")
    suspend fun getPaymentAndRentalRecords(
        @Query("user_id") userId: Int
    ): UserRecordsResponse

    @GET("getUserWithDocument.php")
    suspend fun getProfile (
        @Query("user_id") userId: Int
    ): ProfileResponse

    @FormUrlEncoded
    @POST("rentCar.php")
    suspend fun rentCar(
        @Field("user_id") userId: Int?,
        @Field("vehicle_id") vehicleId: Int?,
        @Field("rental_start_date") startDate: String,
        @Field("rental_end_date") endDate: String,
        @Field("total_cost") totalCost: Double,
        @Field("payment_method") paymentMethod: String
    ): RentResponse
}