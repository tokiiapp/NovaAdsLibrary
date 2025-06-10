package com.ads.library.model;

public class AdAoaSplash {
    private String idAoaHighFloor;
    private String idAoaMedium;
    private String idAoa;

    public AdAoaSplash(String idAoaHighFloor, String idAoaMedium, String idAoa) {
        this.idAoaHighFloor = idAoaHighFloor;
        this.idAoaMedium = idAoaMedium;
        this.idAoa = idAoa;
    }

    public String getIdAoaHighFloor() {
        return idAoaHighFloor;
    }

    public void setIdAoaHighFloor(String idAoaHighFloor) {
        this.idAoaHighFloor = idAoaHighFloor;
    }

    public String getIdAoaMedium() {
        return idAoaMedium;
    }

    public void setIdAoaMedium(String idAoaMedium) {
        this.idAoaMedium = idAoaMedium;
    }

    public String getIdAoa() {
        return idAoa;
    }

    public void setIdAoa(String idAoa) {
        this.idAoa = idAoa;
    }
}
