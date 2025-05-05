package com.ads.library.model;

import com.ads.library.enumads.GoogleENative;
import com.google.android.gms.ads.nativead.NativeAd;

public class AdNative {
    private String idNativeHighFloor;
    private String idNative;
    protected StatusAd status = StatusAd.AD_INIT;
    private NativeAd nativeAdHighFloor;
    private NativeAd nativeAd;
    private GoogleENative nativeSize = GoogleENative.UNIFIED_MEDIUM;

    public AdNative(String idNativeHighFloor, String idNative) {
        this.idNativeHighFloor = idNativeHighFloor;
        this.idNative = idNative;
    }

    public void setNativeAdHighFloor(NativeAd nativeAd) {
        this.nativeAdHighFloor = nativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    public StatusAd getStatus() {
        return status;
    }

    public void setStatus(StatusAd status) {
        this.status = status;
    }

    public NativeAd getNativeAdHighFloor() {
        return nativeAdHighFloor;
    }

    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public GoogleENative getNativeSize() {
        return nativeSize;
    }

    public void setNativeSize(GoogleENative nativeSize) {
        this.nativeSize = nativeSize;
    }

    public String getIdNativeHighFloor() {
        return idNativeHighFloor;
    }

    public void setIdNativeHighFloor(String idNativeHighFloor) {
        this.idNativeHighFloor = idNativeHighFloor;
    }

    public String getIdNative() {
        return idNative;
    }

    public void setIdNative(String idNative) {
        this.idNative = idNative;
    }
}
