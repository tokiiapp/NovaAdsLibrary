package com.ads.library.callback

import com.google.android.gms.ads.AdValue

interface InterCallBack {
    fun onStartAction()
    fun onEventClickAdClosed()
    fun onAdShowed()
    fun onAdLoaded()
    fun onAdFail(error: String)
    fun onPaid(adValue: AdValue?)
}