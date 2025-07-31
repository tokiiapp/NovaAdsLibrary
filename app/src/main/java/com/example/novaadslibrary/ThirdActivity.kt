package com.example.novaadslibrary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ads.library.utils.AdmobUtil

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)


    }

    override fun onBackPressed() {
        AdmobUtil.showInterNativeByRemoteConfig(
            this,
            AdsManager.adInter,
            AdsManager.adNative,
            remoteInter = "3",
            remoteCount = "1",
            R.layout.ad_template_fullscreen, true
        ) {
            super.onBackPressed()
        }

    }
}