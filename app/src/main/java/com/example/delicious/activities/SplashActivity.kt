package com.example.delicious.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.delicious.R

class SplashActivity : AppCompatActivity() {
    private lateinit var preferences: com.example.delicious.util.Preferences
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferences = com.example.delicious.util.Preferences(this)
        imageView = findViewById(R.id.SplashImage)
        imageView.visibility = View.VISIBLE
        val animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val anim1 = imageView.startAnimation(animationFadeIn)
        val handler = Handler()
        handler.postDelayed({ anim1 }, 1000)


        val background = object : Thread() {

            override fun run() {
                try {
                    sleep(3000)
                    if (preferences.isLoggedIn()) {
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(baseContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }

    override fun onPause() {
        super.onPause()
        finish()

    }

}
