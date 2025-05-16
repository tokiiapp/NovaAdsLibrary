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
import com.rate.ratedialog.RatingDialog

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowNative.setOnClickListener {
            AdmobUtil.showNativeHighFloorByRemoteConfig(this, "1",binding.flNative, AdsManager.adNative, R.layout.ad_unified_medium, object : NativeAdCallback {
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
            AdmobUtil.loadAndShowAdRewardByRemoteConfig(this,"1","",true,object : RewardAdCallback{
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

        binding.btnRate.setOnClickListener {
        val ratingDialog = RatingDialog.Builder(this)
            .session(1)
            .date(1)
            .setIcon(R.mipmap.ic_launcher)
            .setNameApp(getString(R.string.app_name))
            .setEmail("Momcenter.contact@gmail.com")
            .isShowButtonLater(true)
            .isClickLaterDismiss(true)
            .setOnlickRate {
            }
            .setTextButtonLater("Maybe later")
            .setOnlickMaybeLate { }
            .ratingButtonColor(com.ads.library.R.color.gnt_blue)
            .build()

        //Cancel On Touch Outside
        ratingDialog.setCanceledOnTouchOutside(true)
        //show
        ratingDialog.show()
        }
    }
}