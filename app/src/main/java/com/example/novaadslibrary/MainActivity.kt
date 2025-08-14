package com.example.novaadslibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ads.library.callback.AppOpenSplashCallback
import com.ads.library.utils.AdmobUtil
import com.ads.library.utils.AppOpenManager
import com.google.android.gms.ads.AdValue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContentView(R.layout.activity_main)
        AdmobUtil.initAdmob(this, 10000, true, true)
        AppOpenManager.instance?.init(application, "")
        AppOpenManager.instance?.disableAppResumeWithActivity(MainActivity::class.java)
        AdmobUtil.loadInterHighFloor(this, AdsManager.adInter)

        AdmobUtil.loadNativeHighFloor(this, AdsManager.adNative)

        AdmobUtil.loadAndShowAdSplash(this, "3", AdsManager.aoaSplash, AdsManager.adInterSplash,
            AdsManager.adNative,R.layout.ad_template_fullscreen,object : AppOpenSplashCallback {
            override fun onAdFail(error: String) {
                onAdClosed()
            }

            override fun onAdClosed() {
                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                finish()
            }

            override fun onPaid(adValue: AdValue?) {

            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}