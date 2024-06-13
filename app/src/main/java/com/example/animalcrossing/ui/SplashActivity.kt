package com.example.animalcrossing.ui

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.FetchRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var fetchRepository: FetchRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val imageLoader = ImageLoader.Builder(applicationContext)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        val splashGif: ImageView = findViewById(R.id.splashGif)
        splashGif.load(R.drawable.splash, imageLoader) {
            crossfade(true)
        }

        Handler().postDelayed({
            lifecycleScope.launch {
                fetchRepository.onStartApp()

                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }, 3000)

    }
}