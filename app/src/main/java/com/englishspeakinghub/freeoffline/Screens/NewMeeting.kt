package com.englishspeakinghub.freeoffline.Screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.englishspeakinghub.freeoffline.Helpers.CONST
import com.englishspeakinghub.freeoffline.Helpers.CreateRoomRequest
import com.englishspeakinghub.freeoffline.Helpers.Response
import com.englishspeakinghub.freeoffline.Helpers.RoomService
import com.englishspeakinghub.freeoffline.R
import com.englishspeakinghub.freeoffline.databinding.ActivityNewMeetingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NewMeeting : AppCompatActivity() {
    lateinit var binding:ActivityNewMeetingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.secondary)
        binding.backHome.setOnClickListener { finish() }
        binding.createRoom.setOnClickListener {
            if(binding.roomName.text.toString().toString().isEmpty()){
                binding.roomName.error = "Please enter room name"
            }else{
                if (binding.maxUsers.text.toString().trim().isEmpty()){
                    binding.maxUsers.error =
                        "Please Specify"
                } else{

                    createRoom(binding.roomName.text.toString().trim(), binding.maxUsers.text.toString().trim() )
                    binding.createText.visibility = View.GONE
                    binding.progressNewRoom.visibility = View.VISIBLE
                }
            }

        }
    }
    fun createRoom(roomId:String,maxUsers:String){
        val roomService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoomService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val createRoomData: CreateRoomRequest
            var audioEnabled = binding.audioEnabled.isChecked
            var videoEnabled = binding.videoEnabled.isChecked
            val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
            createRoomData = CreateRoomRequest(host = sharedPreferences.getString("name","")!!, roomId = roomId, maxParticipant = maxUsers, videoEnabled = videoEnabled, audioEnabled = audioEnabled)
            val data = withContext(Dispatchers.IO){roomService.createRoom(createRoomData)}
            data.enqueue(object : Callback<Response>{
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    if (response.body()!!.status == "0"){
                        Toast.makeText(applicationContext, "Error While Creating Room", Toast.LENGTH_SHORT).show()
                    }else{
                    Toast.makeText(applicationContext, "Room Created Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@NewMeeting,Meeting::class.java)
                        intent.putExtra("room",roomId)
                        startActivity(intent)
                        finish()
                    }
                    binding.createText.visibility = View.VISIBLE
                    binding.progressNewRoom.visibility = View.GONE
                }
                override fun onFailure(call: Call<Response>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error While Creating Room", Toast.LENGTH_SHORT).show()
                    binding.createText.visibility = View.VISIBLE
                    binding.progressNewRoom.visibility = View.GONE
                }
            })
        }
    }
}