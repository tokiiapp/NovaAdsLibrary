package com.example.novaadslibrary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.library.callback.AppOpenSplashCallback
import com.ads.library.utils.AdmobUtil
import com.ads.library.utils.AppOpenManager
import com.google.android.gms.ads.AdValue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        AdmobUtil.initAdmob(this, 10000, true, true)
        AppOpenManager.instance?.init(application, "")
        AppOpenManager.instance?.disableAppResumeWithActivity(MainActivity::class.java)
        AdmobUtil.loadInterHighFloor(this, AdsManager.adInter)

        AdmobUtil.loadNativeHighFloor(this, AdsManager.adNative)

        AdmobUtil.loadAndShowAdSplash(this, "1", AdsManager.aoaSplash, AdsManager.adInterSplash, object : AppOpenSplashCallback {
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
}