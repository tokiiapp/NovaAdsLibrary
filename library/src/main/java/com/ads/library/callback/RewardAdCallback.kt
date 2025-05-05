package com.ads.library.callback

import com.google.android.gms.ads.AdValue

interface RewardAdCallback {
    fun onAdClosed()
    fun onAdShowed()
    fun onAdFail(error: String)
    fun onEarned()
    fun onPaid(adValue: AdValue?)
}
