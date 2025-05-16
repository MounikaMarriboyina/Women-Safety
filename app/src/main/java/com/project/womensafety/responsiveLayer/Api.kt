package com.project.womensafety.responsiveLayer

import com.project.womensafety.responsiveLayer.models.CommonResponse
import com.project.womensafety.responsiveLayer.responses.LoginResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface Api {

   @FormUrlEncoded
   @POST("users.php")
   suspend fun users(
      @Field("name")name:String,
      @Field("mobile")mobile:String,
      @Field("password")password:String,
      @Field("location")location:String,
      @Field("command")command:String
   ):Response<CommonResponse>

   @FormUrlEncoded
   @POST("getData.php")
   suspend fun login(
      @Query("condition")condition:String,
      @Field("mobile")mobile:String,
      @Field("password")password:String
   ):Response<LoginResponse>

   @GET("getData.php")
   suspend fun getLocation(
      @Query("condition")condition:String
   ):Response<LoginResponse>

   @Multipart
   @POST("addVoice.php")
   suspend fun uploadFile(
      @Part audio:MultipartBody.Part,
      @Part video:MultipartBody.Part,
   ):Response<CommonResponse>

}
