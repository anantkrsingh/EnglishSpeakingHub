package com.englishspeakinghub.freeoffline

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.englishspeakinghub.freeoffline.Adapters.RoomAdapter
import com.englishspeakinghub.freeoffline.Helpers.*
import com.englishspeakinghub.freeoffline.Screens.NewMeeting
import com.englishspeakinghub.freeoffline.Screens.Profile
import com.englishspeakinghub.freeoffline.databinding.ActivityMainBinding
import com.englishspeakinghub.freeoffline.databinding.PremiumDialogBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    var rooms:ArrayList<Room> = ArrayList()
    lateinit var dialogPremium:Dialog

    lateinit var appUpdateManager:AppUpdateManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getRooms()
        val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
        val isPremium = sharedPreferences.getBoolean("isPremium",false)
        if (!isPremium) premiumDialog()
         appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager?.registerListener(listener)
        checkUpdate()
        getValidity(sharedPreferences.getString("uid",""))
        window.statusBarColor = ContextCompat.getColor(this,R.color.primary_bg)
        binding.newMeeting.setOnClickListener {
            val intent = Intent(this, NewMeeting::class.java)
            startActivity(intent)
        }
        binding.homeLogo.setOnClickListener{
            openProfile()
        }
        binding.roomsRV.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.searchRoom.doOnTextChanged { text, start, before, count ->
            val searchQuery = text.toString()
            val newList = ArrayList<Room>()
            for (room in rooms) {
                if (room.roomId.contains(searchQuery)) {
                    newList.add(room)
                }
            }
            binding.roomsRV.adapter = RoomAdapter(this@MainActivity, newList)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.roomsRV.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if(scrollY > oldScrollY){
                    binding.newMeeting.hide()
                } else binding.newMeeting.show()
            }
        }
        MobileAds.initialize(
            this
        ) { }
    }


    fun getRooms(){
        var api = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoomService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val roomData = withContext(Dispatchers.IO){api.getRooms()}
            roomData.enqueue(object: Callback<Rooms>{
                override fun onResponse(call: Call<Rooms>, response: Response<Rooms>) {
                    if (response.isSuccessful) {
                        var room: List<Room> = response.body()?.rooms ?: emptyList()
                        val roomList = ArrayList(room)
                        rooms = roomList
                        binding.roomsRV.layoutManager = LinearLayoutManager(this@MainActivity)
                        binding.roomsRV.adapter= RoomAdapter(this@MainActivity,roomList)
                        if (roomList.size == 0){
                            binding.noRooms.visibility = View.VISIBLE
                            binding.roomsRV.visibility = View.GONE
                        } else{
                            binding.noRooms.visibility = View.GONE
                            binding.roomsRV.visibility = View.VISIBLE

                        }
                        for (i in 0 until roomList.size) {
                            Log.e("Rooms", roomList[i].roomId)
                        }

                    }
                }
                override fun onFailure(call: Call<Rooms>, t: Throwable) {
                    Log.e("Room Data", "Error")

                }

            })
        }
    }
    fun getValidity( uid: String?){
        val apiService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO){apiService.getPremium(uid)}
            data.enqueue(object : Callback<PremiumData>{
                override fun onResponse(
                    call: Call<PremiumData>,
                    response: retrofit2.Response<PremiumData>
                ) {
                    val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("validTill", response.body()!!.validTill)
                    editor.apply()
                    editor.commit()
                }

                override fun onFailure(call: Call<PremiumData>, t: Throwable) {

                }

            })
        }
    }
    fun premiumDialog(){
        val dialogBinding: PremiumDialogBinding = PremiumDialogBinding.inflate(layoutInflater)
        dialogPremium = Dialog(this@MainActivity)
        dialogPremium.setContentView(dialogBinding.root)
        dialogPremium.setCanceledOnTouchOutside(false)
        dialogPremium.window?.setGravity(Gravity.CENTER)
        dialogPremium.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogPremium.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPremium.window?.attributes?.windowAnimations = R.style.DialogAnimFade
        dialogPremium.show()
        dialogBinding.closeDialog.setOnClickListener { dialogPremium.dismiss() }
        dialogBinding.subscribe.setOnClickListener { openProfile() }
    }
    fun openProfile(){
        val intent = Intent(this@MainActivity, Profile::class.java)
        val pairs: Array<Pair<View, String>?> = arrayOfNulls(2)
        pairs[0] = Pair<View, String>(binding.homeLogo, "logo")
        pairs[1] = Pair<View, String>(binding.root, "bg")
        var options: ActivityOptions? = null
        options = ActivityOptions.makeSceneTransitionAnimation(
            this@MainActivity,
            *pairs
        )
        startActivity(intent, options!!.toBundle())
    }
    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update.
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    0)
            }
        }
    }

    private val listener: InstallStateUpdatedListener? = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager!!.completeUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        getRooms()
    }
}