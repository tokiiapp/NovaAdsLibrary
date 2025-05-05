package com.ads.library.callback

import com.google.android.gms.ads.AdValue

interface NativeAdCallback {
    fun onNativeAdLoaded(){}
    fun onAdFail(error: String){}
    fun onAdPaid(adValue: AdValue?){}
}