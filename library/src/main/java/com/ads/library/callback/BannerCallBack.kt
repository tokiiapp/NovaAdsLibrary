package com.ads.library.callback

import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView

interface BannerCallBack {
    fun onLoad(){}
    fun onFailed(error: String){}
    fun onClosed(adSize: AdSize){}
    fun onPaid(adValue: AdValue?, mAdView: AdView?){}

}