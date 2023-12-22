package com.example.uas_papb

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_papb.databinding.ActivitySplashBinding
import com.example.uas_papb.tool.AddOn.isNetworkAvailable
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sharedpref : SharedPreferences
    private lateinit var auth: FirebaseAuth

    companion object{
        const val SHAREDPREF = "shared_key"
        const val EMAIL = "email"
        const val PASS = "password"
    }
    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        sharedpref = getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        email = sharedpref.getString(EMAIL, null)
        password = sharedpref.getString(PASS, null)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(baseContext, LoginActivity::class.java))
            finish()
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser == null) {
            val editor = sharedpref.edit()
            editor.clear()
            editor.apply()
            return
        }
        if(auth.currentUser != null && email == auth.currentUser?.email.toString()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}