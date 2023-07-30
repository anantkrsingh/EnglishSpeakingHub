package com.englishspeakinghub.freeoffline.Helpers
import androidx.annotation.Keep
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomService {
    @GET("/api/meeting/getRooms")
    fun getRooms() : Call<Rooms>
    @POST("/api/meeting/create")
    fun createRoom(@Body request: CreateRoomRequest) : Call<Response>
    @Keep
    @GET("/api/session/enlist")
    fun enlist(@Query("room") room:String?) :Call<Response>

}