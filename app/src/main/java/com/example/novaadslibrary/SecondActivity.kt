package com.example.novaadslibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.library.callback.BannerCallBack
import com.ads.library.callback.InterCallBack
import com.ads.library.callback.NativeAdCallback
import com.ads.library.callback.RewardAdCallback
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

        binding.btnLoadShowBanner.setOnClickListener {
            AdmobUtil.loadAndShowBanner(this,"", binding.flBanner, object : BannerCallBack {})
        }

        binding.btnLoadShowBannerCollap.setOnClickListener {
            AdmobUtil.loadAndShowBannerCollapsible(this, "", CollapsibleBanner.BOTTOM, binding.flBanner, object : BannerCallBack {})

        }

        binding.btnLoadShowBannerCollapTop.setOnClickListener {
            AdmobUtil.loadAndShowBannerCollapsible(this, "", CollapsibleBanner.TOP, binding.flBannerTop, object : BannerCallBack {})

        }

        binding.btnLoadShowNative.setOnClickListener {
            AdmobUtil.showNativeHighFloor(this, binding.flNative, AdsManager.adNative, R.layout.ad_unified_medium, object : NativeAdCallback {
                override fun onNativeAdLoaded() {

                }

                override fun onAdFail(error: String) {

                }

                override fun onAdPaid(adValue: AdValue?) {

                }

            })
        }

        binding.btnLoadShowNativeCollap.setOnClickListener {
            AdmobUtil.loadAndShowBannerByRemoteConfig(
                this, "", AdsManager.adNative, "4", binding.flBanner, CollapsibleBanner.BOTTOM, R.layout.ad_template_collap,
                R.layout.ad_template_banner, object : NativeAdCallback {}, object : BannerCallBack {})
        }

        binding.btnLoadShowNativeCollapTop.setOnClickListener {
            AdmobUtil.loadAndShowBannerByRemoteConfig(
                this, "", AdsManager.adNative, "4", binding.flBannerTop, CollapsibleBanner.TOP, R.layout.ad_template_collap,
                R.layout.ad_template_banner, object : NativeAdCallback {}, object : BannerCallBack {})
        }


        binding.btnShowInter.setOnClickListener {
            AdmobUtil.showInterByRemoteConfig(this, AdsManager.adInter, remoteCount = "1", true, object : InterCallBack {
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
        
        binding.btnLoadAndShowReward.setOnClickListener {
            AdmobUtil.loadAndShowAdRewardWithCallback(this,"",true,object : RewardAdCallback{
                override fun onAdClosed() {

                }

                override fun onAdShowed() {
                    
                }

                override fun onAdFail(error: String) {
                    
                }

                override fun onEarned() {
                    
                }

                override fun onPaid(adValue: AdValue?) {
                    
                }
            })
        }

    }
}