package com.ads.library.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.library.R
import com.ads.library.callback.AppOpenSplashCallback
import com.ads.library.callback.BannerCallBack
import com.ads.library.callback.InterCallBack
import com.ads.library.callback.NativeAdCallback
import com.ads.library.callback.RewardAdCallback
import com.ads.library.enumads.CollapsibleBanner
import com.ads.library.enumads.GoogleENative
import com.ads.library.model.AdAoaSplash
import com.ads.library.model.AdInter
import com.ads.library.model.AdNative
import com.ads.library.model.StatusAd
import com.ads.library.utils.NativeHelper.Companion.populateNativeAdView
import com.ads.library.utils.NativeHelper.Companion.populateNativeAdViewCollap
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Date

object AdmobUtil {
    //Ẩn hiện quảng cáo
    var isShowAds = true

    //Dùng ID Test để hiển thị quảng cáo
    var isTesting = false

    // Timeout init admob
    var timeOut = 0

    // Biến check lần cuối hiển thị quảng cáo
    var lastTimeShowInterstitial: Long = 0

    //Check quảng cáo đang show hay không
    var isAdShowing = false

    var dialogLoadingFullScreen: Dialog? = null
    var mRewardedAd: RewardedAd? = null

    var shimmerFrameLayout: ShimmerFrameLayout? = null

    @JvmField
    var mBannerCollapView: AdView? = null

    fun getAdRequest(): AdRequest {
        return AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    fun initAdmob(context: Context, timeout: Int, isDebug: Boolean, isEnableAds: Boolean) {
        timeOut = if (timeout > 0) {
            timeout
        } else {
            10000
        }


        isTesting = isDebug

        isShowAds = isEnableAds

        MobileAds.initialize(context) {

        }
    }

    fun setTestDeviceList(testDeviceList: List<String>) {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceList)
                .build()
        )
    }

    //check open network
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun dismissAdDialog() {
        dialogLoadingFullScreen?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun dialogLoading(context: Context) {
        dialogLoadingFullScreen = Dialog(context)
        dialogLoadingFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogLoadingFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogLoadingFullScreen?.setCancelable(false)
        dialogLoadingFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogLoadingFullScreen?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialogLoadingFullScreen?.show()
    }

    /**
     * load and show Reward theo remote config
     * @param remoteValue :
     * - 0 : tăt
     * - 1 : bật
     */
    @JvmStatic
    fun loadAndShowAdRewardByRemoteConfig(
        activity: Activity,
        remoteValue: String,
        admobId: String,
        enableLoadingDialog: Boolean,
        adCallback: RewardAdCallback
    ) {
        if (remoteValue == "0") {
            adCallback.onAdFail("Tắt reward")
        } else {
            loadAndShowAdRewardWithCallback(activity, admobId, enableLoadingDialog, adCallback)
        }
    }

    /**
     * show Native theo remote config
     * @param remoteValue :
     * - 0 : tắt
     * - 1 : bật
     */
    @JvmStatic
    fun showNativeHighFloorByRemoteConfig(
        activity: Activity,
        remoteValue: String,
        viewGroup: ViewGroup,
        adNative: AdNative,
        layout: Int,
        nativeAdCallback: NativeAdCallback
    ) {
        if (remoteValue == "0") {
            viewGroup.gone()
        } else {
            showNativeHighFloor(activity, viewGroup, adNative, layout, nativeAdCallback)
        }
    }

    /**
     * Load Banner Thường, Banner Collap, Native thường, Native Collap dựa trên biến remote config
     * @param remoteValue :
     * - 1 : load Banner
     * - 2 : load Banner Collap
     * - 3 : load Native Small, nếu layoutBanner = null thì sẽ load Native Medium
     * - 4 : load Native Collap
     *
     * @param collapsibleBannersize: Chỉ định hướng của banner hoặc native colap. Nếu là CollapsibleBanner.TOP thì sẽ là bên trên và ngược lại
     *
     */
    @JvmStatic
    fun loadAndShowBannerByRemoteConfig(
        activity: Activity,
        bannerId: String,
        adNative: AdNative,
        remoteValue: String,
        viewGroup: ViewGroup,
        collapsibleBannersize: CollapsibleBanner,
        layoutCollap: Int,
        layoutBanner: Int?,
        nativeAdCallback: NativeAdCallback,
        bannerCallBack: BannerCallBack
    ) {
        when (remoteValue) {
            "0" -> {
                viewGroup.gone()
            }

            "1" -> {//* Banner
                loadAndShowBanner(activity, bannerId, viewGroup, bannerCallBack)
            }

            "2" -> {//* Banner Collap
                loadAndShowBannerCollapsible(activity, bannerId, collapsibleBannersize, viewGroup, bannerCallBack)
            }

            "3" -> {//* Native Small
                if (layoutBanner == null) {
                    loadAndShowNativeHighFloor(activity, viewGroup, adNative, GoogleENative.UNIFIED_MEDIUM, layoutCollap, nativeAdCallback)
                } else {
                    loadAndShowNativeHighFloor(activity, viewGroup, adNative, GoogleENative.UNIFIED_SMALL, layoutBanner, nativeAdCallback)
                }
            }

            "4" -> {//* Native Collap
                val anchor = if (collapsibleBannersize == CollapsibleBanner.TOP) "top" else "bottom"
                loadAndShowNativeCollap(activity, viewGroup, adNative, layoutCollap, layoutBanner, anchor, nativeAdCallback)
            }

            else -> {
                viewGroup.gone()
            }
        }
    }

    /**
     * show Inter Native dựa trên biến remote config

     * @param remoteInter
     * - 1 : show Inter bình thường
     * - 2 : chỉ show Native Full Screen
     * - 3 : show Inter Native
     *
     *@param remoteCount
     * - 0 : không show Inter
     * - 1 : show Inter bình thường
     * - 2 : show Inter từ những lần chia hết cho 2
     * - 3 : show Inter từ những lần chia hết cho 3
     *
     */
    @JvmStatic
    fun showInterNativeByRemoteConfig(
        activity: Activity, adInter: AdInter, adNative: AdNative, remoteInter: String,
        remoteCount: String, layout: Int, enableLoadingDialog: Boolean, onFinished: () -> Unit
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false

            onFinished()
            return
        }

        adInter.countInter++

        if (remoteCount == "0") {
            onFinished()
        } else if (remoteCount == "1" || adInter.countInter % remoteCount.toInt() == 0) {

            val tag = "native_full_view"
            var decorView: ViewGroup? = null
            runCatching {
                decorView = activity.window.decorView as ViewGroup
                decorView.findViewWithTag<View>(tag)?.let { decorView.removeView(it) }
            }

            when (remoteInter) {
                "1" -> {
                    showInterHighFloor(activity, adInter, enableLoadingDialog, object : InterCallBack {
                        override fun onStartAction() {

                        }

                        override fun onEventClickAdClosed() {
                            onFinished()
                        }

                        override fun onAdShowed() {

                        }

                        override fun onAdLoaded() {

                        }

                        override fun onAdFail(error: String) {
                            onFinished()
                        }

                        override fun onPaid(adValue: AdValue?) {

                        }

                    })
                }

                "2" -> {
                    destroyBannerCollapView()
                    val container = activity.layoutInflater.inflate(R.layout.ad_native_inter_container, null, false)
                    val viewGroup = container.findViewById<FrameLayout>(R.id.viewGroup)
                    val btnClose = container.findViewById<View>(R.id.ad_close)
                    val tvTimer = container.findViewById<TextView>(R.id.ad_timer)

                    try {
                        container.tag = tag
                        decorView!!.addView(container)
                    } catch (e: Exception) {

                        onFinished()
                        return
                    }
                    container.visible()
                    tvTimer.gone()
                    btnClose.invisible()
                    isAdShowing = true

                    btnClose.setOnClickListener {
                        container.gone()
                        runCatching { decorView?.removeView(container) }
                        onFinished()
                    }

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        container.gone()
                        runCatching { decorView?.removeView(container) }
                        onFinished()
                        isAdShowing = false
                    }, 15000) //* Timeout 15s for loading NativeFull

                    showNativeHighFloor(activity, viewGroup, adNative, layout, object : NativeAdCallback {
                        override fun onNativeAdLoaded() {
                            isAdShowing = true
                            btnClose.visible()
                            handler.removeCallbacksAndMessages(null)
                        }

                        override fun onAdFail(error: String) {
                            handler.removeCallbacksAndMessages(null)
                            container.gone()
                            runCatching { decorView?.removeView(container) }
                            onFinished()
                        }

                        override fun onAdPaid(adValue: AdValue?) {

                        }
                    })
                }

                "3" -> {
                    destroyBannerCollapView()
                    val container = activity.layoutInflater.inflate(R.layout.ad_native_inter_container, null, false)
                    val viewGroup = container.findViewById<FrameLayout>(R.id.viewGroup)
                    val btnClose = container.findViewById<View>(R.id.ad_close)
                    val tvTimer = container.findViewById<TextView>(R.id.ad_timer)

                    try {
                        container.tag = tag
                        decorView!!.addView(container)
                    } catch (e: Exception) {

                        onFinished()
                        return
                    }
                    container.visible()
                    tvTimer.gone()
                    btnClose.invisible()
                    btnClose.setOnClickListener {
                        container.gone()
                        runCatching { decorView?.removeView(container) }
                        onFinished()
                        isAdShowing = false
                    }

                    showInterHighFloor(activity, adInter, enableLoadingDialog, object : InterCallBack {
                        override fun onStartAction() {

                        }

                        override fun onEventClickAdClosed() {
                            if (adNative.status == StatusAd.AD_LOADED) {
                                btnClose.visible()
                                showNativeHighFloor(activity, viewGroup, adNative, layout, object : NativeAdCallback {
                                    override fun onNativeAdLoaded() {
                                        isAdShowing = true
                                        btnClose.visible()
                                    }

                                    override fun onAdFail(error: String) {
                                        container.gone()
                                        runCatching { decorView?.removeView(container) }
                                        onFinished()
                                    }

                                    override fun onAdPaid(adValue: AdValue?) {

                                    }
                                })
                            } else {
                                container.gone()
                                runCatching { decorView?.removeView(container) }
                                onFinished()
                            }
                        }

                        override fun onAdShowed() {

                        }

                        override fun onAdLoaded() {

                        }

                        override fun onAdFail(error: String) {
                            onEventClickAdClosed()
                        }

                        override fun onPaid(adValue: AdValue?) {

                        }

                    })
                }
            }

        } else {
            onFinished()
        }
    }

    /**
     * show Inter dựa trên biến remote config
     *
     * @param remoteCount
     * - 0 : không show Inter
     * - 1 : show Inter bình thường
     * - 2 : show Inter từ những lần chia hết cho 2
     * - 3 : show Inter từ những lần chia hết cho 3
     */
    @JvmStatic
    fun showInterByRemoteConfig(activity: Activity, adInter: AdInter, remoteCount: String, enableLoadingDialog: Boolean, callBack: InterCallBack) {
        if (remoteCount == "0") {
            callBack.onAdFail("Not show inter")
        } else if (remoteCount == "1") {
            showInterHighFloor(activity, adInter, enableLoadingDialog, callBack)
            Log.d("AAA", "show Inter")
        } else {
            adInter.countInter++
            if (adInter.countInter % remoteCount.toInt() != 0) {
                Log.d("AAA", "Not show Inter: count=${adInter.countInter}")
                callBack.onAdFail("Not show Inter: count=${adInter.countInter}")
            } else {
                Log.d("AAA", "show Inter: count=${adInter.countInter}")
                showInterHighFloor(activity, adInter, enableLoadingDialog, callBack)
            }
        }
    }

    /**
     * show Adsplash dựa trên biến remote config
     *
     * @param remoteValue
     * - 0 : không show Ad plash
     * - 1 : show AOA
     * - 2 : show Inter
     */
    @JvmStatic
    fun loadAndShowAdSplash(activity: Activity, remoteValue: String, adAoaSplash: AdAoaSplash, adInter: AdInter, callBack: AppOpenSplashCallback) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            callBack.onAdFail("No internet")
            return
        }
        when (remoteValue) {
            "0" -> {
                callBack.onAdFail("No show Ad")
            }

            "1" -> {
                loadAndShowAppOpenSplashMulti(activity, adAoaSplash, object : AppOpenSplashCallback {
                    override fun onAdFail(error: String) {
                        callBack.onAdFail(error)
                    }

                    override fun onAdClosed() {
                        callBack.onAdClosed()
                    }

                    override fun onPaid(adValue: AdValue?) {

                    }
                })
            }

            "2" -> {
                loadAndShowAdInterstitialMulti(activity, adInter, object : InterCallBack {
                    override fun onStartAction() {

                    }

                    override fun onEventClickAdClosed() {
                        callBack.onAdClosed()
                    }

                    override fun onAdShowed() {

                    }

                    override fun onAdLoaded() {

                    }

                    override fun onAdFail(error: String) {
                        callBack.onAdFail(error)
                    }

                    override fun onPaid(adValue: AdValue?) {
                        callBack.onPaid(adValue)
                    }

                }, false)
            }

            else -> {
                callBack.onAdFail("No show Ad")
            }
        }
    }

    @JvmStatic
    fun loadAndShowAppOpenSplashMulti(
        activity: Activity,
        adAoaSplash: AdAoaSplash,
        appOpenSplashCallback: AppOpenSplashCallback
    ) {
        var appResumeAdId = ""

        appResumeAdId = if (isTesting) {
            activity.getString(R.string.test_ads_admob_app_open)
        } else {
            adAoaSplash.idAoaHighFloor
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            appOpenSplashCallback.onAdFail("No internet")
            return
        }

        AppOpenAd.load(activity, appResumeAdId, getAdRequest(), object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                appOpenAd.show(activity)
                appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        appOpenSplashCallback.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        appOpenSplashCallback.onAdFail(adError.message)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                AppOpenAd.load(activity, adAoaSplash.idAoaMedium, getAdRequest(), object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(appOpenAd: AppOpenAd) {
                        super.onAdLoaded(appOpenAd)
                        appOpenAd.show(activity)
                        appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                appOpenSplashCallback.onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                appOpenSplashCallback.onAdFail(adError.message)
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        AppOpenAd.load(activity, adAoaSplash.idAoa, getAdRequest(), object : AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                                super.onAdLoaded(appOpenAd)
                                appOpenAd.show(activity)
                                appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                                    override fun onAdClicked() {
                                        super.onAdClicked()
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        appOpenSplashCallback.onAdClosed()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        super.onAdFailedToShowFullScreenContent(adError)
                                        appOpenSplashCallback.onAdFail(adError.message)
                                    }

                                    override fun onAdImpression() {
                                        super.onAdImpression()
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                    }
                                }
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                appOpenSplashCallback.onAdFail(loadAdError.message)
                            }

                        })

                    }

                })

            }

        })

    }

    @JvmStatic
    fun loadAndShowAppOpenSplash(activity: Activity, appOpenId: String, appOpenSplashCallback: AppOpenSplashCallback) {
        var appResumeAdId = ""

        appResumeAdId = if (isTesting) {
            activity.getString(R.string.test_ads_admob_app_open)
        } else {
            appOpenId
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            appOpenSplashCallback.onAdFail("No internet")
            return
        }

        AppOpenAd.load(activity, appResumeAdId, getAdRequest(), object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                appOpenAd.show(activity)
                appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        appOpenSplashCallback.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        appOpenSplashCallback.onAdFail(adError.message)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                appOpenSplashCallback.onAdFail(loadAdError.message)
            }

        })

    }

    @JvmStatic
    fun loadAndShowAdInterstitialMulti(activity: Activity, adInter: AdInter, adsInterCallBack: InterCallBack, enableLoadingDialog: Boolean) {
        isAdShowing = false

        if (!isShowAds || !isNetworkConnected(activity)) {
            adsInterCallBack.onAdFail("No internet")
            return
        }
        var id = adInter.idInterHighFloor
        if (isTesting) {
            id = activity.getString(R.string.test_ads_admob_inter_id)
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }

        InterstitialAd.load(activity, id, getAdRequest(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                adsInterCallBack.onAdLoaded()
                interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? -> adsInterCallBack.onPaid(adValue) }
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        isAdShowing = false
                        lastTimeShowInterstitial = Date().time
                        adsInterCallBack.onEventClickAdClosed()
                        dismissAdDialog()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        isAdShowing = false

                        adsInterCallBack.onAdFail(adError.message)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        isAdShowing = true
                        adsInterCallBack.onAdShowed()
                        try {
                            interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                        } catch (e: Exception) {
                        }
                        dismissAdDialog()

                    }
                }
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        isAdShowing = true
                        adsInterCallBack.onStartAction()
                        interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                        interstitialAd.show(activity)
                    }, 400)

                } else {
                    isAdShowing = false
                    dismissAdDialog()
                    adsInterCallBack.onAdFail("onResume")
                }

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                InterstitialAd.load(activity, adInter.idInterMedium, getAdRequest(), object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        adsInterCallBack.onAdLoaded()
                        interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? -> adsInterCallBack.onPaid(adValue) }
                        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                isAdShowing = false
                                lastTimeShowInterstitial = Date().time
                                adsInterCallBack.onEventClickAdClosed()
                                dismissAdDialog()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                isAdShowing = false

                                adsInterCallBack.onAdFail(adError.message)
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                isAdShowing = true
                                adsInterCallBack.onAdShowed()
                                try {
                                    interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                                } catch (e: Exception) {
                                }
                                dismissAdDialog()

                            }
                        }
                        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                isAdShowing = true
                                adsInterCallBack.onStartAction()
                                interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                                interstitialAd.show(activity)
                            }, 400)

                        } else {
                            isAdShowing = false
                            dismissAdDialog()
                            adsInterCallBack.onAdFail("onResume")
                        }

                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        InterstitialAd.load(activity, adInter.idInter, getAdRequest(), object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                super.onAdLoaded(interstitialAd)
                                adsInterCallBack.onAdLoaded()
                                interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? -> adsInterCallBack.onPaid(adValue) }
                                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        isAdShowing = false
                                        lastTimeShowInterstitial = Date().time
                                        adsInterCallBack.onEventClickAdClosed()
                                        dismissAdDialog()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        super.onAdFailedToShowFullScreenContent(adError)
                                        isAdShowing = false

                                        adsInterCallBack.onAdFail(adError.message)
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        isAdShowing = true
                                        adsInterCallBack.onAdShowed()
                                        try {
                                            interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                                        } catch (e: Exception) {
                                        }
                                        dismissAdDialog()

                                    }
                                }
                                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        isAdShowing = true
                                        adsInterCallBack.onStartAction()
                                        interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                                        interstitialAd.show(activity)
                                    }, 400)

                                } else {
                                    isAdShowing = false
                                    dismissAdDialog()
                                    adsInterCallBack.onAdFail("onResume")
                                }

                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                isAdShowing = false
                                adsInterCallBack.onAdFail(loadAdError.message)
                                dismissAdDialog()
                            }
                        })
                    }
                })
            }
        })

    }

    @JvmStatic
    fun loadAndShowAdInterstitial(activity: Activity, admobId: String, adsInterCallBack: InterCallBack, enableLoadingDialog: Boolean) {
        isAdShowing = false

        if (!isShowAds || !isNetworkConnected(activity)) {
            adsInterCallBack.onAdFail("No internet")
            return
        }
        var id = admobId
        if (isTesting) {
            id = activity.getString(R.string.test_ads_admob_inter_id)
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }

        InterstitialAd.load(activity, id, getAdRequest(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                adsInterCallBack.onAdLoaded()
                interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? -> adsInterCallBack.onPaid(adValue) }
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        isAdShowing = false
                        lastTimeShowInterstitial = Date().time
                        adsInterCallBack.onEventClickAdClosed()
                        dismissAdDialog()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        isAdShowing = false

                        adsInterCallBack.onAdFail(adError.message)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        isAdShowing = true
                        adsInterCallBack.onAdShowed()
                        try {
                            interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                        } catch (e: Exception) {
                        }
                        dismissAdDialog()

                    }
                }
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        isAdShowing = true
                        adsInterCallBack.onStartAction()
                        interstitialAd.setOnPaidEventListener(adsInterCallBack::onPaid)
                        interstitialAd.show(activity)
                    }, 400)

                } else {
                    isAdShowing = false
                    dismissAdDialog()
                    adsInterCallBack.onAdFail("onResume")
                }

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isAdShowing = false
                adsInterCallBack.onAdFail(loadAdError.message)
                dismissAdDialog()
            }
        })

    }

    //loadInterHighFloor
    @JvmStatic
    fun loadInterHighFloor(context: Context, adInter: AdInter) {
        if (!isShowAds || !isNetworkConnected(context)) {
            return
        }
        var mIdHighFloor = adInter.idInterHighFloor
        if (isTesting) {
            mIdHighFloor = context.getString(R.string.test_ads_admob_inter_id)
        }
        adInter.status = StatusAd.AD_LOADING
        InterstitialAd.load(context, mIdHighFloor, getAdRequest(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                adInter.interstitialAdHighFloor = interstitialAd
                adInter.status = StatusAd.AD_LOADED
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                adInter.status = StatusAd.AD_LOADING

                InterstitialAd.load(context, adInter.idInterMedium, getAdRequest(), object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        adInter.interstitialAdMedium = interstitialAd
                        adInter.status = StatusAd.AD_LOADED
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        adInter.status = StatusAd.AD_LOADING

                        InterstitialAd.load(context, adInter.idInter, getAdRequest(), object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                super.onAdLoaded(interstitialAd)
                                adInter.interstitialAd = interstitialAd
                                adInter.status = StatusAd.AD_LOADED
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                super.onAdFailedToLoad(p0)
                                adInter.status = StatusAd.AD_LOAD_FAIL
                            }
                        })
                    }
                })

            }
        })

    }

    //showInterHighFloor
    @JvmStatic
    fun showInterHighFloor(activity: Activity, adInter: AdInter, enableLoadingDialog: Boolean, callBack: InterCallBack) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false

            callBack.onAdFail("No internet")
            return
        }
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (adInter.status != StatusAd.AD_LOADED) {

                isAdShowing = false
                dismissAdDialog()
                callBack.onAdFail("timeout")
            }
        }
        handler.postDelayed(runnable, 10000)

        var interstitialAd = adInter.interstitialAdHighFloor
            ?: adInter.interstitialAdMedium
            ?: adInter.interstitialAd
        if (interstitialAd != null) {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }

            interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    isAdShowing = false
                    callBack.onEventClickAdClosed()
                    dismissAdDialog()
                    adInter.interstitialAdHighFloor = null
                    adInter.interstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    isAdShowing = false
                    callBack.onAdFail(adError.message)
                    handler.removeCallbacksAndMessages(null)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    handler.removeCallbacksAndMessages(null)
                    isAdShowing = true
                    callBack.onAdShowed()
                    try {
                        interstitialAd.setOnPaidEventListener(callBack::onPaid)
                    } catch (e: Exception) {
                    }
                    dismissAdDialog()

                }

            }
            if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    isAdShowing = true
                    callBack.onStartAction()
                    interstitialAd.setOnPaidEventListener(callBack::onPaid)
                    interstitialAd.show(activity)
                }, 400)

            } else {
                isAdShowing = false
                dismissAdDialog()
                callBack.onAdFail("onResume")
            }
        } else {
            isAdShowing = false
            dismissAdDialog()
            callBack.onAdFail("No ad")
            return
        }
    }

    //loadNativeHighFloor
    @JvmStatic
    fun loadNativeHighFloor(context: Context, adNative: AdNative) {
        if (!isShowAds || !isNetworkConnected(context)) {
            adNative.status = StatusAd.AD_LOAD_FAIL
            return
        }
        var mIdNativeHighFloor = adNative.idNativeHighFloor
        if (isTesting) {
            mIdNativeHighFloor = context.getString(R.string.test_ads_admob_native_id)
        }
        adNative.status = StatusAd.AD_LOADING
        val adLoaderHighFloor: AdLoader = AdLoader.Builder(context, mIdNativeHighFloor).forNativeAd {
            adNative.nativeAdHighFloor = it
            adNative.status = StatusAd.AD_LOADED
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)

                val adLoader: AdLoader = AdLoader.Builder(context, adNative.idNativeMedium).forNativeAd {
                    adNative.nativeAdMedium = it
                    adNative.status = StatusAd.AD_LOADED
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)

                        val adLoader: AdLoader = AdLoader.Builder(context, adNative.idNative).forNativeAd {
                            adNative.nativeAd = it
                            adNative.status = StatusAd.AD_LOADED
                        }.withAdListener(object : AdListener() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                super.onAdFailedToLoad(adError)
                                adNative.status = StatusAd.AD_LOAD_FAIL
                            }
                        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                        runCatching {
                            adLoader.loadAd(getAdRequest())
                        }
                    }
                }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                runCatching {
                    adLoader.loadAd(getAdRequest())
                }

            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        runCatching {
            adLoaderHighFloor.loadAd(getAdRequest())
        }

    }

    //showNativeHighFloor
    @JvmStatic
    private fun showNativeHighFloor(activity: Activity, viewGroup: ViewGroup, adNative: AdNative, layout: Int, nativeAdCallback: NativeAdCallback) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.gone()
            return
        }
        runCatching {
            viewGroup.removeAllViews()
        }

        var nativeAd = adNative.nativeAdHighFloor
            ?: adNative.nativeAdMedium
            ?: adNative.nativeAd

        if (nativeAd != null) {
            val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
            populateNativeAdView(nativeAd, adView, adNative.nativeSize)
            runCatching {
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
            }
            nativeAdCallback.onNativeAdLoaded()

            adNative.nativeAdHighFloor = null
            adNative.nativeAd = null
        } else {
            nativeAdCallback.onAdFail("No ad")
        }

    }

    @JvmStatic
    private fun loadAndShowNativeHighFloor(
        activity: Activity,
        viewGroup: ViewGroup,
        adNative: AdNative,
        size: GoogleENative,
        layout: Int,
        nativeAdCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            adNative.status = StatusAd.AD_LOAD_FAIL
            viewGroup.gone()
            return
        }
        var mIdNativeHighFloor = adNative.idNativeHighFloor
        var mIdNative = adNative.idNative

        if (isTesting) {
            mIdNativeHighFloor = activity.getString(R.string.test_ads_admob_native_id)
            mIdNative = activity.getString(R.string.test_ads_admob_native_id)
        }

        adNative.status = StatusAd.AD_LOADING
        adNative.nativeSize = size
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        if (shimmerFrameLayout == null) shimmerFrameLayout = tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        val adLoaderHighFloor: AdLoader = AdLoader.Builder(activity, mIdNativeHighFloor).forNativeAd {
            adNative.nativeAdHighFloor = it
            adNative.status = StatusAd.AD_LOADED
            if (shimmerFrameLayout != null) {
                shimmerFrameLayout!!.stopShimmer()
            }
            showNativeHighFloor(activity, viewGroup, adNative, layout, nativeAdCallback)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)

                val adLoader: AdLoader = AdLoader.Builder(activity, mIdNative).forNativeAd {
                    adNative.nativeAd = it
                    adNative.status = StatusAd.AD_LOADED
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout!!.stopShimmer()
                    }
                    showNativeHighFloor(activity, viewGroup, adNative, layout, nativeAdCallback)
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        adNative.status = StatusAd.AD_LOAD_FAIL
                        viewGroup.gone()
                    }
                }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                runCatching {
                    adLoader.loadAd(getAdRequest())
                }

            }

        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        runCatching {
            adLoaderHighFloor.loadAd(getAdRequest())

        }
    }

    @JvmStatic
    private fun loadAndShowNativeCollap(
        activity: Activity,
        viewGroup: ViewGroup,
        adNative: AdNative,
        layoutCollap: Int,
        layoutSmall: Int?,
        anchor: String,
        nativeAdCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            adNative.status = StatusAd.AD_LOAD_FAIL
            viewGroup.gone()
            return
        }

        var decorView: ViewGroup? = null
        val tag = "native_collap_view"
        runCatching {
            decorView = activity.window.decorView as ViewGroup
            decorView!!.findViewWithTag<View>(tag)?.let { decorView!!.removeView(it) }
        }

        if (isNativeInterShowing(activity)) {
            viewGroup.gone()
            nativeAdCallback.onAdFail("Native Inter is showing")
            return
        }

        var mIdNativeHighFloor = adNative.idNativeHighFloor
        var mIdNative = adNative.idNative

        if (isTesting) {
            mIdNativeHighFloor = activity.getString(R.string.test_ads_admob_native_id)
            mIdNative = activity.getString(R.string.test_ads_admob_native_id)
        }

        adNative.status = StatusAd.AD_LOADING

        val tagView: View = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        runCatching {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView, 0)
        }
        if (shimmerFrameLayout == null) shimmerFrameLayout = tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        val adLoaderHighFloor: AdLoader = AdLoader.Builder(activity, mIdNativeHighFloor).forNativeAd {
            adNative.nativeAdHighFloor = it
            adNative.status = StatusAd.AD_LOADED
            shimmerFrameLayout?.stopShimmer()

            nativeAdCallback.onNativeAdLoaded()

            val adViewCollap = activity.layoutInflater.inflate(layoutCollap, null) as NativeAdView
            adViewCollap.tag = tag
            populateNativeAdViewCollap(it, adViewCollap, GoogleENative.UNIFIED_MEDIUM, anchor) {
                runCatching { //* On icon collapse clicked
                    decorView?.removeView(adViewCollap)
                    if (layoutSmall == null) {
                        NativeHelper.reConstraintNativeCollapView(adViewCollap)
                        viewGroup.addView(adViewCollap)
                    }
                }
            }
            runCatching {
                viewGroup.removeView(tagView)
                val layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    gravity = if (anchor == "top") Gravity.TOP else Gravity.BOTTOM
                }
                decorView?.addView(adViewCollap, layoutParams)
                if (layoutSmall != null) {
                    val adViewSmall = activity.layoutInflater.inflate(layoutSmall, null) as NativeAdView
                    populateNativeAdView(it, adViewSmall, GoogleENative.UNIFIED_SMALL)
                    viewGroup.addView(adViewSmall)
                }
            }

        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)

                val adLoader: AdLoader = AdLoader.Builder(activity, mIdNative).forNativeAd {
                    adNative.nativeAd = it
                    adNative.status = StatusAd.AD_LOADED
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout!!.stopShimmer()
                    }
                    nativeAdCallback.onNativeAdLoaded()

                    val adViewCollap = activity.layoutInflater.inflate(layoutCollap, null) as NativeAdView
                    adViewCollap.tag = tag
                    populateNativeAdViewCollap(it, adViewCollap, GoogleENative.UNIFIED_MEDIUM, anchor) {
                        runCatching { //* On icon collapse clicked
                            decorView?.removeView(adViewCollap)
                            if (layoutSmall == null) {
                                NativeHelper.reConstraintNativeCollapView(adViewCollap)
                                viewGroup.addView(adViewCollap)
                            }
                        }
                    }
                    runCatching {
                        viewGroup.removeView(tagView)
                        val layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                            gravity = if (anchor == "top") Gravity.TOP else Gravity.BOTTOM
                        }
                        decorView?.addView(adViewCollap, layoutParams)
                        if (layoutSmall != null) {
                            val adViewSmall = activity.layoutInflater.inflate(layoutSmall, null) as NativeAdView
                            populateNativeAdView(it, adViewSmall, GoogleENative.UNIFIED_SMALL)
                            viewGroup.addView(adViewSmall)
                        }
                    }

                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        adNative.status = StatusAd.AD_LOAD_FAIL
                        viewGroup.gone()
                        nativeAdCallback.onAdFail(adError.message)
                    }
                }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                runCatching {
                    adLoader.loadAd(getAdRequest())
                }

            }

        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        runCatching {
            adLoaderHighFloor.loadAd(getAdRequest())
        }
    }

    @JvmStatic
    private fun loadAndShowBanner(activity: Activity, bannerId: String, viewGroup: ViewGroup, bannerCallBack: BannerCallBack) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            bannerCallBack.onFailed("No internet")
            return
        }

        val mAdView = AdView(activity)
        var id = bannerId
        if (isTesting) {
            id = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = id
        val adSize: AdSize = getAdSize(activity)

        mAdView.setAdSize(adSize)
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        mAdView.onPaidEventListener = OnPaidEventListener { adValue -> bannerCallBack.onPaid(adValue, mAdView) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerCallBack.onLoad()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                viewGroup.gone()
                bannerCallBack.onFailed(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        mAdView.loadAd(getAdRequest())

    }

    @JvmStatic
    private fun loadAndShowBannerCollapsible(
        activity: Activity,
        bannerId: String,
        collapsibleBannersize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerCallBack
    ) {
        var bannerId1 = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId1 = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId1
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        mAdView.onPaidEventListener = OnPaidEventListener { adValue -> callback.onPaid(adValue, mAdView) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onLoad()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                viewGroup.gone()
                callback.onFailed(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                callback.onClosed(adSize)
            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannersize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        if (adRequest2 != null) {
            mAdView.loadAd(adRequest2)
        }
        Log.e(" Admod", "loadAdBanner")
        mBannerCollapView = mAdView
    }

    private fun getAdSize(context: Activity): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = context.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }


    @JvmStatic
    private fun loadAndShowAdRewardWithCallback(
        activity: Activity,
        admobId: String,
        enableLoadingDialog: Boolean,
        adCallback: RewardAdCallback
    ) {
        var admobId = admobId
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdClosed()
            return
        }

        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_reward_id)
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }
        isAdShowing = false
        if (AppOpenManager.instance?.isInitialized == true) {
            AppOpenManager.instance?.isAppResumeEnabled = false
        }
        RewardedAd.load(
            activity, admobId!!,
            getAdRequest(), object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    mRewardedAd = null
                    adCallback.onAdFail(loadAdError.message)
                    dismissAdDialog()
                    if (AppOpenManager.instance?.isInitialized == true) {
                        AppOpenManager.instance?.isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.message)
                    Log.e("Admodfail", "errorCodeAds" + loadAdError.cause)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    if (mRewardedAd != null) {
                        mRewardedAd?.setOnPaidEventListener { adCallback.onPaid(it) }
                        mRewardedAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdShowedFullScreenContent() {
                                    isAdShowing = true
                                    adCallback.onAdShowed()
                                    if (AppOpenManager.instance?.isInitialized == true) {
                                        AppOpenManager.instance?.isAppResumeEnabled = false
                                    }
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when ad fails to show.
                                    if (adError.code != 1) {
                                        isAdShowing = false
                                        adCallback.onAdFail(adError.message)
                                        mRewardedAd = null
                                        dismissAdDialog()
                                    }
                                    if (AppOpenManager.instance?.isInitialized == true) {
                                        AppOpenManager.instance?.isAppResumeEnabled = true
                                    }
                                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    mRewardedAd = null
                                    isAdShowing = false
                                    adCallback.onAdClosed()
                                    dismissAdDialog()
                                    if (AppOpenManager.instance?.isInitialized == true) {
                                        AppOpenManager.instance?.isAppResumeEnabled = true
                                    }
                                }
                            }
                        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            if (AppOpenManager.instance?.isInitialized == true) {
                                AppOpenManager.instance?.isAppResumeEnabled = false
                            }
                            mRewardedAd?.show(activity) { adCallback.onEarned() }
                            isAdShowing = true
                        } else {
                            mRewardedAd = null
                            dismissAdDialog()
                            isAdShowing = false
                            if (AppOpenManager.instance?.isInitialized == true) {
                                AppOpenManager.instance?.isAppResumeEnabled = true
                            }
                        }
                    } else {
                        isAdShowing = false
                        adCallback.onAdFail("None Show")
                        dismissAdDialog()
                        if (AppOpenManager.instance?.isInitialized == true) {
                            AppOpenManager.instance?.isAppResumeEnabled = true
                        }
                    }
                }
            })
    }

    @JvmStatic
    fun isNativeInterShowing(activity: Activity): Boolean {
        runCatching {
            val decorView = activity.window.decorView as ViewGroup
            val tag = "native_full_view"
            val nativeView = decorView.findViewWithTag<View>(tag)
            return nativeView != null && nativeView.isVisible
        }
        return false
    }

    private fun destroyBannerCollapView() {
        runCatching {
            mBannerCollapView?.destroy()
            (mBannerCollapView?.parent as? ViewGroup)?.removeView(mBannerCollapView)
        }.onFailure {

        }
    }
}