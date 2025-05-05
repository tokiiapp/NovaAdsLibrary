package com.ads.library.model;

import com.google.android.gms.ads.interstitial.InterstitialAd;

public class AdInter {

    private String idInterHighFloor;
    private String idInter;
    protected StatusAd status = StatusAd.AD_INIT;
    private InterstitialAd interstitialAdHighFloor;
    private InterstitialAd interstitialAd;
    private int countInter = 0;

    public AdInter(String idInterHighFloor, String idInter) {
        this.idInterHighFloor = idInterHighFloor;
        this.idInter = idInter;
    }

    public void setInterstitialAdHighFloor(InterstitialAd interstitialAd) {
        this.interstitialAdHighFloor = interstitialAd;
        status = StatusAd.AD_LOADED;
    }
    public void setInterstitialAd(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
        status = StatusAd.AD_LOADED;
    }

    public InterstitialAd getInterstitialAdHighFloor() {
        return interstitialAdHighFloor;
    }

    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    public StatusAd getStatus() {
        return status;
    }

    public void setStatus(StatusAd status) {
        this.status = status;
    }

    public boolean isReady(){
        return interstitialAd != null;
    }

    public int getCountInter() {
        return countInter;
    }

    public void setCountInter(int countInter) {
        this.countInter = countInter;
    }

    public String getIdInterHighFloor() {
        return idInterHighFloor;
    }

    public void setIdInterHighFloor(String idInterHighFloor) {
        this.idInterHighFloor = idInterHighFloor;
    }

    public String getIdInter() {
        return idInter;
    }

    public void setIdInter(String idInter) {
        this.idInter = idInter;
    }
}
