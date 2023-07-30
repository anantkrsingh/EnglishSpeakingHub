package com.englishspeakinghub.freeoffline.Helpers

data class Response(val status:String,val message : String , val user:User)
data class User(val name:String , val email: String, val isPremium:Boolean , val avatar: String, val _id: String)
data class Rooms(val rooms: List<Room>)
data class Room(
    val roomName: String,
    val roomId: String,
    val host: String,
    val avatar: String,
    val roomToken: String,
    val maxParticipant:String,
    val enrolledUsers:String
)
data class PremiumData(val uid:String,val validTill:String?)
class CreateRoomRequest internal constructor(val host: String, val roomId: String, val maxParticipant:String , val audioEnabled:Boolean, val videoEnabled:Boolean)
data class LoginReq(val email: String ,val password:String)