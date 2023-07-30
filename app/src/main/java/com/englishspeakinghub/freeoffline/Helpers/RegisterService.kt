package com.englishspeakinghub.freeoffline.Helpers

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RegisterService {
    @GET("/api/signup")
    fun registerUser( @Query("name") name:String,@Query("email") email:String,@Query("password") password:String) : Call<Response>
    @POST("/api/signin")
    fun loginUser( @Body request: LoginReq)  : Call<Response>
    @POST("/api/premium")
    fun addPremium( @Body req:PremiumData) : Call<Response>
    @GET("/api/premium/get")
    fun getPremium (@Query ("uid") uid:String?):Call<PremiumData>
    @GET("/api/google")
    fun registerGoogle(@Query("name") name:String,@Query("email") email:String,@Query("password") password:String) : Call<Response>
    @GET("/api/resend")
    fun resendLink(@Query("email") email: String?) : Call<Response>
}