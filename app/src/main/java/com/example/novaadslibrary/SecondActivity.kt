package com.example.novaadslibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.library.callback.BannerCallBack
import com.ads.library.callback.InterCallBack
import com.ads.library.callback.NativeAdCallback
import com.ads.library.enumads.CollapsibleBanner
import com.ads.library.utils.AdmobUtil
import com.example.novaadslibrary.databinding.ActivitySecondBinding
import com.google.android.gms.ads.AdValue

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowInter.setOnClickListener {
            AdmobUtil.showInterByRemoteConfig(this, AdsManager.adInter, remoteCount = "2", true, object : InterCallBack {
                override fun onStartAction() {

                }

                override fun onEventClickAdClosed() {

                }

                override fun onAdShowed() {

                }

                override fun onAdLoaded() {

                }

                override fun onAdFail(error: String) {

                }

                override fun onPaid(adValue: AdValue?) {

                }
            })

        }

        binding.btnShowInterWithNative.setOnClickListener {
            AdmobUtil.showInterNativeByRemoteConfig(
                this,
                AdsManager.adInter,
                AdsManager.adNative,
                remoteInter = "3",
                remoteCount = "1",
                R.layout.ad_template_fullscreen, true
            ) {

            }
        }

        AdmobUtil.showNativeHighFloor(this, binding.flNative, AdsManager.adNative, R.layout.ad_unified_medium, object : NativeAdCallback {
            override fun onNativeAdLoaded() {

            }

            override fun onAdFail(error: String) {

            }

            override fun onAdPaid(adValue: AdValue?) {

            }

        })

        binding.btnLoadShowNativeCollapTop.setOnClickListener {
            AdmobUtil.loadAndShowBannerByRemoteConfig(
                this, "", AdsManager.adNative, "4", binding.flBannerTop, CollapsibleBanner.TOP, R.layout.ad_template_collap,
                R.layout.ad_template_banner, object : NativeAdCallback {}, object : BannerCallBack {})
        }

        binding.btnLoadShowNativeCollap.setOnClickListener {
            AdmobUtil.loadAndShowBannerByRemoteConfig(
                this, "", AdsManager.adNative, "4", binding.flBanner, CollapsibleBanner.BOTTOM, R.layout.ad_template_collap,
                R.layout.ad_template_banner, object : NativeAdCallback {}, object : BannerCallBack {})
        }
    }
}