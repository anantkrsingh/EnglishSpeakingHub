package com.englishspeakinghub.freeoffline.Screens

import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.englishspeakinghub.freeoffline.Helpers.CONST
import com.englishspeakinghub.freeoffline.Helpers.RoomService
import com.englishspeakinghub.freeoffline.R
import com.englishspeakinghub.freeoffline.databinding.ActivityMeetingBinding
import com.englishspeakinghub.freeoffline.databinding.DialogLoadingBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Meeting : AppCompatActivity() {
    lateinit var binding:ActivityMeetingBinding
    lateinit var dialogLoading:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialogLoading = Dialog(this)
        requestPermissions()
        val sharedPreferences = getSharedPreferences("USER-DATA",Context.MODE_PRIVATE)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mixedContentMode = (WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE)
        val webSettings: WebSettings = binding.webView.getSettings()
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.defaultTextEncodingName = "utf-8"
        binding.webView.loadUrl("https://www.englishspeakinghub.online/?room=${intent.getStringExtra("room")}&user=${sharedPreferences.getString("uid","")}")
        binding.webView.webViewClient = object:  WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.e("URL",binding.webView.url.toString())
                dialogLoading.dismiss()
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingDialog()
            }

        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (request.origin.toString() == "https://hotshotincorporative.000webhostapp.com/index.html") {
                    // Grant camera and microphone permission
                    request.grant(request.resources)
                } else {
                    // Deny permission request
                    request.grant(request.resources)
                }
            }
        }
        val adRequest: AdRequest = AdManagerAdRequest.Builder().build()
        binding.meetingAdView.loadAd(adRequest)


    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                    requestPermissions()
                }
                return
            }
        }
    }
    override fun onDestroy() {
        enlistUser()
        binding.webView.clearCache(true)
        super.onDestroy()
    }

    fun enlistUser(){
        val api = Retrofit.Builder()
            .baseUrl(CONST.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoomService::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            var room = intent.getStringExtra("room")
            withContext(Dispatchers.IO){api.enlist(room = room)}
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data: Uri? = intent.data
        if (data != null) {
            Log.d(TAG, "Received URL: " + data.toString())
        }
    }
    fun loadingDialog(){
        val dialogBinding: DialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        dialogLoading = Dialog(this@Meeting)
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