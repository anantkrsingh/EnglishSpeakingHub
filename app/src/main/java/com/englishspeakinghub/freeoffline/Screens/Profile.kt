package com.englishspeakinghub.freeoffline.Screens

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.englishspeakinghub.freeoffline.Helpers.CONST
import com.englishspeakinghub.freeoffline.Helpers.PremiumData
import com.englishspeakinghub.freeoffline.Helpers.RegisterService
import com.englishspeakinghub.freeoffline.Helpers.Response
import com.englishspeakinghub.freeoffline.R
import com.englishspeakinghub.freeoffline.databinding.ActivityProfileBinding
import com.englishspeakinghub.freeoffline.databinding.DialogLoadingBinding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity(), PaymentResultListener {
    lateinit var binding:ActivityProfileBinding
    var name  = ""
    var uid = ""
    var email = ""
    var isPremium = false;
    lateinit var dialogLoading:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
        binding.userName.text = sharedPreferences.getString("name", "")
        binding.userEmail.text = sharedPreferences.getString("email", "")
        name = sharedPreferences.getString("name", "")!!
        uid = sharedPreferences.getString("uid", "")!!
        email = sharedPreferences.getString("email", "")!!
        isPremium = sharedPreferences.getBoolean("isPremium",false)
        if (isPremium) {
            val dtobj = SimpleDateFormat("dd-MM-yyyy")
            val current: Calendar = Calendar.getInstance()
            val df = SimpleDateFormat("dd-MM-yyyy")
            val resultdate = Date(current.getTimeInMillis())
            val today: String = df.format(resultdate)
            val userDate = sharedPreferences.getString("validTill","")
            val a = dtobj.parse(userDate)
            val b = dtobj.parse(today)
            if (a.equals(b) ||a.after(b) ){
                //Premium Valid
                binding.premiumSection.visibility = View.VISIBLE
                binding.subscribePan.visibility = View.GONE
                binding.premiumValidity.text = "Your Premium is Valid Till " + sharedPreferences.getString("validTill","")
            }else if(a.before(b)){
                //User Premium Expired
                binding.premiumSection.visibility = View.GONE
                binding.subscribePan.visibility = View.VISIBLE
            }
        }
        binding.logout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("user", false)
        }
        binding.logout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("user", false)
            editor.apply()
            editor.commit()
            val intent = Intent(this@Profile, Login::class.java)
            val pairs: Array<Pair<View, String>?> = arrayOfNulls(2)
            pairs[0] = Pair<View, String>(binding.splashLogo, "logo")
            pairs[1] = Pair<View, String>(binding.root, "bg")
            var options: ActivityOptions? = null
            options = ActivityOptions.makeSceneTransitionAnimation(
                this@Profile,
                *pairs
            )
            finishAffinity()
            startActivity(intent, options!!.toBundle())
        }
        binding.subscribe.setOnClickListener {
            initPayment()
        }
    }
    fun initPayment(){
        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_live_j34ghDLqqNtjX8")
//        co.setKeyID("rzp_test_lyVg4EbhAATCjE")
        try {
            val options = JSONObject()
            options.put("name","English SpeakingHub Premium")
            options.put("description","Premium For English Speaking Hub")
            options.put("theme.color", "#FDC50C")
            options.put("currency", "INR")
            options.put("amount", "4900")

            val retryOBJ = JSONObject()
            retryOBJ.put("enabled", true)
            retryOBJ.put("max_count", 4)

            options.put("retry", retryOBJ)
            co.open(this@Profile, options)
        }catch (e:java.lang.Exception){
            Toast.makeText(this@Profile, "Error in Payment : " + e.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this@Profile, "Payment Success", Toast.LENGTH_LONG)
            .show()
        loadingDialog()
        val premiumService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val userData: PremiumData
            val sharedPreferences = getSharedPreferences("USER-DATA", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val current: Calendar = Calendar.getInstance()
            current.add(Calendar.DATE, 30)
            val df = SimpleDateFormat("dd-MM-yyyy")
            val resultdate = Date(current.getTimeInMillis())
            val dueudate: String = df.format(resultdate)
            userData = PremiumData(uid = sharedPreferences.getString("uid","")!!, validTill = dueudate.toString())
            val data = withContext(Dispatchers.IO){premiumService.addPremium(userData)}
            data.enqueue(object : Callback<Response> {
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    dialogLoading.dismiss()
                    binding.premiumSection.visibility  = View.VISIBLE
                    binding.premiumValidity.text = "Your Premium is Valid till " + dueudate
                    binding.subscribePan.visibility = View.GONE
                    editor.putBoolean("isPremium", true)
                    editor.putString("validTill",dueudate)
                    editor.commit()
                }
                override fun onFailure(call: Call<Response>, t: Throwable) {
                    dialogLoading.dismiss()
                }
            })
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.e("Payment Failure",p1.toString())
        Toast.makeText(this@Profile, "Payment Failure" + p1.toString(), Toast.LENGTH_LONG).show()
    }
    fun loadingDialog(){
        val dialogBinding: DialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        dialogLoading = Dialog(this@Profile)
        dialogLoading.setContentView(dialogBinding.root)
        dialogLoading.setCanceledOnTouchOutside(false)
        dialogLoading.window?.setGravity(Gravity.CENTER)
        dialogLoading.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogLoading.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogLoading.window?.setDimAmount(0.9f)
        dialogLoading.window?.attributes?.windowAnimations = R.style.DialogAnimFade
        dialogLoading.show()
    }
}

