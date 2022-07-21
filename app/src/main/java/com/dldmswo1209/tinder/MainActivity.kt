package com.dldmswo1209.tinder

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.dldmswo1209.tinder.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding? = null
    private val binding get() = mBinding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
        } else{
            startActivity(Intent(this, LikeActivity::class.java))
        }
    }
}