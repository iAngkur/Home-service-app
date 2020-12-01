package com.example.homeserviceapp.Model;

public class AvailableServiceProviders {
    String pid, lat, lang;

    public AvailableServiceProviders() {
    }

    public AvailableServiceProviders(String pid, String lat, String lang) {
        this.pid = pid;
        this.lat = lat;
        this.lang = lang;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
