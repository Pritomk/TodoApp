package com.example.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.todoapp.databinding.ActivitySplashBinding
import android.content.Intent

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val thread = object : Thread() {
            override fun run() {
                try {
                    sleep(2000);
                    startActivity(Intent(this@SplashActivity,LoginActivity::class.java))
                    finish()
                }catch ( e : Exception) {
                }
            }
        }
        thread.start()
    }
}