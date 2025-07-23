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
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            AdmobUtil.loadInterHighFloor(this@ThirdActivity, AdsManager.adInter)

            AdmobUtil.loadNativeHighFloor(this@ThirdActivity, AdsManager.adNative)
            super.onBackPressed()
        }

    }
}