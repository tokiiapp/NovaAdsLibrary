package com.ads.library.callback

import com.google.android.gms.ads.AdValue

interface AppOpenSplashCallback {
    fun onAdFail(error: String)
    fun onAdClosed()
    fun onPaid(adValue: AdValue?)
}