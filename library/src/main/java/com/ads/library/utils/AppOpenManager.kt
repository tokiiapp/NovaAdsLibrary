package com.ads.library.utils

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*
import com.ads.library.R

class AppOpenManager : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var appResumeAd: AppOpenAd? = null
    private var splashAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    private var appResumeAdId: String? = null
    private var currentActivity: Activity? = null
    private var myApplication: Application? = null
    private var appResumeLoadTime: Long = 0
    private var splashLoadTime: Long = 0
    private val splashTimeout = 0
    var isInitialized = false
        private set
    var isAppResumeEnabled = true
    private val disabledAppOpenList: MutableList<Class<*>>
    private val splashActivity: Class<*>? = null
    private var isTimeout = false
    private var dialogFullScreen: Dialog? = null
    private val timeoutHandler = Handler { msg: Message ->
        if (msg.what == TIMEOUT_MSG) {
            isTimeout = true
        }
        false
    }

    /**
     * Init AppOpenManager
     *
     * @param application
     */
    fun init(application: Application, appOpenAdId: String?) {
        isInitialized = true
        myApplication = application
        initAdRequest()
        if (AdmobUtil.isTesting) {
            appResumeAdId = application.getString(R.string.test_ads_admob_app_open)
        } else {
            appResumeAdId = appOpenAdId
        }
        myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (!isAdAvailable(false) && appOpenAdId != null) {
            fetchAd(false)
        }
    }

    var adRequest: AdRequest? = null

    // get AdRequest
    fun initAdRequest() {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(5000)
            .build()
    }

    /**
     * Check app open ads is showing
     *
     * @return
     */
    val isShowingAd: Boolean
        get() = Companion.isShowingAd

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.remove(activityClass)
    }

    fun setAppResumeAdId(appResumeAdId: String?) {
        this.appResumeAdId = appResumeAdId
    }

    fun setFullScreenContentCallback(callback: FullScreenContentCallback?) {
        fullScreenContentCallback = callback
    }

    fun removeFullScreenContentCallback() {
        fullScreenContentCallback = null
    }

    fun fetchAd(isSplash: Boolean) {
        Log.d(TAG, "fetchAd: isSplash = $isSplash")
        if (isAdAvailable(isSplash) || appResumeAdId == null) {
            return
        }
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.d(TAG, "onAppOpenAdLoaded: isSplash = $isSplash")
                if (!isSplash) {
                    appResumeAd = ad
                    appResumeLoadTime = Date().time
                } else {
                    splashAd = ad
                    splashLoadTime = Date().time
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                val a = "fail"
            }
        }
        if (myApplication != null && appResumeAdId != null && adRequest != null && loadCallback != null) {

            AppOpenAd.load(
                myApplication!!, appResumeAdId!!, adRequest!!,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback!!
            )
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(loadTime: Long, numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun isAdAvailable(isSplash: Boolean): Boolean {
        val loadTime = if (isSplash) splashLoadTime else appResumeLoadTime
        val wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4)
        Log.d(TAG, "isAdAvailable: $wasLoadTimeLessThanNHoursAgo")
        return ((if (isSplash) splashAd != null else appResumeAd != null)
                && wasLoadTimeLessThanNHoursAgo)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        if (splashActivity == null) {
            if (activity.javaClass.name != AdActivity::class.java.getName()) {
                fetchAd(false)
            }
        } else {
            if (activity.javaClass.name != splashActivity.name && activity.javaClass.name != AdActivity::class.java.getName()) {
                fetchAd(false)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        try {
            if (currentActivity?.isDestroyed == false) {
                dialogFullScreen?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
            }
        } catch (_: Exception) {
        }
        currentActivity = null
    }

    fun showAdIfAvailable(isSplash: Boolean) {
        if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            try {
                if (currentActivity?.isDestroyed == false) {
                    dialogFullScreen?.let {
                        if (it.isShowing) {
                            it.dismiss()
                        }
                    }
                }
            } catch (_: Exception) {
            }
            fullScreenContentCallback?.onAdDismissedFullScreenContent()

            return
        }
        if (!Companion.isShowingAd && isAdAvailable(isSplash)) {
            val callback: FullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    try {
                        if (currentActivity?.isDestroyed == false) {
                            dialogFullScreen?.let {
                                if (it.isShowing) {
                                    it.dismiss()
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                    // Set the reference to null so isAdAvailable() returns false.
                    appResumeAd = null
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback?.onAdDismissedFullScreenContent()
                    }
                    Companion.isShowingAd = false
                    fetchAd(isSplash)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    try {
                        if (currentActivity?.isDestroyed == false) {
                            dialogFullScreen?.let {
                                if (it.isShowing) {
                                    it.dismiss()
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback?.onAdFailedToShowFullScreenContent(adError)
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "onAdShowedFullScreenContent: isSplash = $isSplash")
                    Companion.isShowingAd = true
                    if (isSplash) {
                        splashAd = null
                    } else {
                        appResumeAd = null
                    }
                }
            }
            showAdsResume(isSplash, callback)
        } else {
            Log.d(TAG, "Ad is not ready")
            if (!isSplash) {
                fetchAd(false)
            }
        }
    }

    private fun showAdsResume(isSplash: Boolean, callback: FullScreenContentCallback) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Handler().postDelayed({
                currentActivity?.let {
                    if (isSplash) {
                        splashAd?.fullScreenContentCallback = callback
                        if (currentActivity != null) showDialog(currentActivity)
                        splashAd?.show(it)
                    } else {
                        if (appResumeAd != null) {
                            appResumeAd?.fullScreenContentCallback = callback
                            if (currentActivity != null) showDialog(currentActivity)
                            appResumeAd?.show(it)
                        }
                    }
                }

            }, 100)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume() {

        if (currentActivity == null) {
            return
        }
        if (AdmobUtil.isAdShowing) {
            return
        }
        if (!AdmobUtil.isShowAds) {
            return
        }

        for (activity in disabledAppOpenList) {
            if (activity.name == currentActivity!!.javaClass.name) {
                Log.d(TAG, "onStart: activity is disabled")
                return
            }
        }
        showAdIfAvailable(false)
    }

    fun showDialog(context: Context?) {
        dialogFullScreen = Dialog(context!!)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen_onresume)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        dialogFullScreen?.show()
    }

    companion object {
        private const val TAG = "AppOpenManager"

        @Volatile
        private var INSTANCE: AppOpenManager? = null
        private var isShowingAd = false
        private const val TIMEOUT_MSG = 11

        @get:Synchronized
        val instance: AppOpenManager?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppOpenManager()
                }
                return INSTANCE
            }
    }

    /**
     * Constructor
     */
    init {
        disabledAppOpenList = ArrayList()
    }
}