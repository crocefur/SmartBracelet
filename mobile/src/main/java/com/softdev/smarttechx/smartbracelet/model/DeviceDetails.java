package com.softdev.smarttechx.smartbracelet.model;

import java.io.Serializable;

/**
 * Created by SMARTTECHX on 11/2/2017.
 */

public class DeviceDetails implements Serializable {

    public String mac;
    public String rssi;
    public String name;


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

}
