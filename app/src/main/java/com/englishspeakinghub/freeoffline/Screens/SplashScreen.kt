package com.englishspeakinghub.freeoffline.Screens

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.englishspeakinghub.freeoffline.MainActivity
import com.englishspeakinghub.freeoffline.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    //Binding
    lateinit var binding:ActivitySplashScreenBinding
    val interval : Long = 100
    var totalTime : Long= 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //BindingInit
        Log.e("Package",packageName.toString())

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Timer
        val timer = object : CountDownTimer(totalTime,interval){
            override fun onTick(p0: Long) {

            }
            override fun onFinish() {
                //OPEN NEW ACTIVITY
                val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
                var user = sharedPreferences.getBoolean("user",false)


                if (user){
                    val intent = Intent(this@SplashScreen, MainActivity::class.java)
                    val pairs: Array<Pair<View, String>?> = arrayOfNulls(2)
                    pairs[0] = Pair<View, String>(binding.splashLogo, "logo")
                    pairs[1] = Pair<View, String>(binding.root, "bg")
                    var options: ActivityOptions? = null
                    options = ActivityOptions.makeSceneTransitionAnimation(
                        this@SplashScreen,
                        *pairs
                    )
                    startActivity(intent, options!!.toBundle())
                    finish()
                } else{
                    val intent = Intent(this@SplashScreen, Login::class.java)
                    val pairs: Array<Pair<View, String>?> = arrayOfNulls(2)
                    pairs[0] = Pair<View, String>(binding.splashLogo, "logo")
                    pairs[1] = Pair<View, String>(binding.root, "bg")
                    var options: ActivityOptions? = null
                    options = ActivityOptions.makeSceneTransitionAnimation(
                        this@SplashScreen,
                        *pairs
                    )
                    startActivity(intent, options!!.toBundle())
                    finish()
                }

            }

        }
        timer.start()
    }
}