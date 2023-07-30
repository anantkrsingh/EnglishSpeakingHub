package com.englishspeakinghub.freeoffline.Screens

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.englishspeakinghub.freeoffline.Helpers.*
import com.englishspeakinghub.freeoffline.MainActivity
import com.englishspeakinghub.freeoffline.R
import com.englishspeakinghub.freeoffline.databinding.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Login : AppCompatActivity() {
    lateinit var loginBinding: ActivityLoginBinding
    lateinit var auth:FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var dialogLoading:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        //Click Function
        loginBinding.btnRegister.setOnClickListener {
            showRegister()
        }
        loginBinding.btnLogin.setOnClickListener {
            showLogin()
        }
        auth = FirebaseAuth.getInstance()
        loginBinding.btnGoogleLogin.setOnClickListener {
            googleLogin()
            loadingDialog()
        }


    }
    fun showRegister(){
        val dialogBinding:RegisterDialogBinding = RegisterDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this@Login)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!
            .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setWindowAnimations(R.style.DialogAnim)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.setCancelable(false)
        dialog.show()
        dialogBinding.regBack.setOnClickListener { dialog.dismiss() }
        val regName = dialogBinding.regName
        val regEmail = dialogBinding.regEmail
        val regPass = dialogBinding.regPass
        val regFinalPass = dialogBinding.regPassCnf
        dialogBinding.registerSubmit.setOnClickListener {
            if(regName.text?.trim()?.isEmpty()!!){
                regName.error = "Name Required"
            } else {
                if (regEmail.text?.trim()?.isEmpty()!!) {
                    regEmail.error = "Email Required"
                } else {
                    if (regPass.text?.trim()?.isEmpty()!!) {
                        regPass.error = "Password Required"
                    } else {
                        if (regPass.text?.toString()?.trim()!!.equals(regFinalPass.text?.toString()?.trim())) {
                            signUp(dialogBinding.regName.text.toString(),dialogBinding.regPass.text.toString().trim(),dialogBinding.regEmail.text.toString(),dialogBinding.regTitle,dialogBinding.progressReg)
                            dialogBinding.regTitle.visibility = View.GONE
                            dialogBinding.progressReg.visibility = View.VISIBLE

                        } else{
                            regPass.error = "Passwords Doesn't Match"
                            regFinalPass.error = "Passwords Doesn't Match"
                        }
                    }
                }
            }

        }
    }
    fun showLogin(){
        val dialogBinding:LoginDialogBinding = LoginDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this@Login)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!
            .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setWindowAnimations(R.style.DialogAnim)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.setCancelable(false)
        dialog.show()
        dialogBinding.loginBack.setOnClickListener { dialog.dismiss() }
        dialogBinding.loginBtn.setOnClickListener { login(dialogBinding.loginPass.text.toString(),dialogBinding.loginEmail.text.toString(), dialogBinding.textLogin, dialogBinding.progressLogin)
            dialogBinding.textLogin.visibility = View.GONE
            dialogBinding.progressLogin.visibility = View.VISIBLE

        }
    }
    fun signUp(name:String,password:String,email: String, view: View, view2:View){
         val apiService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO){apiService.registerUser(name, email, password)}
            data.enqueue(object : Callback<Response>{
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    try {
                        Log.d("Success",response.body()!!.message)
                        if (response.body()!!.status.equals("0")){
                            showErrorDialog(response.body()!!.message,true,email)
                        } else showSuccessDialog(response.body()!!.message , email)

                    }catch (e:java.lang.Exception){

                    }
                    view.visibility = View.VISIBLE
                    view2.visibility = View.GONE
                }

                override fun onFailure(call: Call<Response>, th: Throwable) {
                    Log.d("Success",th.message.toString() )
                    showErrorDialog("")
                    view.visibility = View.VISIBLE
                    view2.visibility = View.GONE
                }
            })
        }

    }
    fun login(password:String , email:String, view: View, view2:View){
        val apiService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val transData: LoginReq
            transData = LoginReq(email = email, password = password)
            val data = withContext(Dispatchers.IO){apiService.loginUser(transData)}
            data.enqueue(object : Callback<Response>{
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                        if (response.body()!!.status.trim().equals("1")){
                            if (response.body()!!.user.isPremium){
                                getValidity(uid = response.body()!!.user._id, name = response.body()!!.user.name ,email = response.body()!!.user.email, login ="Email & Password" )
                            }else{
                                saveUser(response.body()!!.user.name,response.body()!!.user._id,response.body()!!.user.email,"Email & Password", avatar =  response.body()!!.user.avatar ,  isPremium = response.body()!!.user.isPremium)

                            }
                        }
                        else showErrorDialog(response.body()!!.message)
                    view.visibility = View.VISIBLE
                    view2.visibility = View.GONE

                }

                override fun onFailure(call: Call<Response>, th: Throwable) {
                    Log.d("Success",th.message.toString() )
                    showErrorDialog("")
                    view.visibility = View.VISIBLE
                    view2.visibility = View.GONE

                }
            })
        }
    }
    fun googleLogin(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if(account != null){
                updateUI(account)
            }
        }else{
            dialogLoading.dismiss()
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUI(account: GoogleSignInAccount) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                googleRegister(account.displayName.toString(),account.email.toString())

            }else Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    fun showErrorDialog(error:String, resend:Boolean = false , email: String? = ""){
        val dialogBinding:ErrorDialogBinding = ErrorDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this@Login)
        dialog.setContentView(dialogBinding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.9f)
        dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
        dialog.show()
        dialogBinding.errorDetails.text = error
        dialogBinding.dismissError.setOnClickListener { dialog.dismiss() }
        if (resend) dialogBinding.resendError.visibility = View.VISIBLE
        dialogBinding.resendError.setOnClickListener {
            reSendLink(email)
            dialog.dismiss()
        }
    }

    private fun reSendLink(email: String?) {
        Toast.makeText(this,"Please Wait...", Toast.LENGTH_SHORT).show()
        val apiService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO){apiService.resendLink(email)}
            data.enqueue(object : Callback<Response>{
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    if (response.body()!!.status == "1"){
                        showSuccessDialog(response.body()!!.message ,email!!)
                    }else{
                        Toast.makeText(this@Login,"Error Please Try After Sometime",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Response>, t: Throwable) {

                }

            })
        }
    }

    fun showSuccessDialog(message:String , email: String = ""){
        val dialogBinding:SuccessDialogBinding = SuccessDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this@Login)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!
            .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog
        dialog.setCancelable(false)
        dialog.show()
        dialogBinding.successPrompt.text = message
        dialogBinding.dismissSuccess.setOnClickListener { dialog.dismiss() }
        dialogBinding.resendEmail.setOnClickListener {
            dialog.dismiss()
            reSendLink(email)
        }
    }
    fun loadingDialog(){
        val dialogBinding:DialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        dialogLoading = Dialog(this@Login)
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
    fun googleRegister(name:String,email:String){
        val apiService = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            val data = withContext(Dispatchers.IO){apiService.registerGoogle(name, email, "null")}
            data.enqueue(object : Callback<Response>{
                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    if (response.body()!!.user.isPremium){
                        getValidity(response.body()!!.message, email = email, name = name, login = "Google Account")
                    } else{
                        saveUser(name, uid = response.body()!!.message,email,"Google Account",)
                    }


                }

                override fun onFailure(call: Call<Response>, th: Throwable) {
                    Log.d("Fail",th.message.toString() )

                }
            })
        }
    }
    fun saveUser(name:String,uid:String,email:String, login:String , avatar : String = "1" , isPremium :Boolean = false ){
        try {
            dialogLoading.dismiss()
        } catch (e:java.lang.Exception){

        }

        val sharedPreferences = getSharedPreferences("USER-DATA",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("uid", uid)
        editor.putString("email", email)
        editor.putString("loginType",login )
        editor.putString("avatar", avatar)
        editor.putBoolean("isPremium", isPremium)
        editor.putBoolean("user",true)
        editor.apply()
        editor.commit()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    fun getValidity(uid:String , name: String ,email: String,login: String ){
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
                    saveUser(name, uid,email,login, isPremium = true)
                    val sharedPreferences = getSharedPreferences("USER-DATA",Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("validTill", response.body()!!.validTill)
                    Toast.makeText(this@Login,response.body()!!.validTill,Toast.LENGTH_SHORT).show()
                    editor.apply()
                    editor.commit()
                }

                override fun onFailure(call: Call<PremiumData>, t: Throwable) {

                }

            })
        }
    }
}