package com.softdev.smarttechx.smartbracelet.model;

import java.io.Serializable;

/**
 * Created by SMARTTECHX on 10/13/2017.
 */

public class UserDetails implements Serializable {
    private String sno;
    private String name;
    private String lastname;
    private String username;
    private String date;
    private String from;
    private String till;
    private String email;
    private String password;
    private String old_password;
    private String new_password;
    private String step;
    private String bike;
    private String logintype;
    private String distance;
    private String synctime;
    private String profilepic;
    private double stepVal;
    private Boolean reload = false;
    private int daycount;
    private String code;
    private String macaddress;
    private Boolean isBind;

    private String bandname;

    public Boolean getIsBind() {

        return isBind;
    }

    public void setIsBind(Boolean isBind) {

        this.isBind = isBind;
    }

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    public String getBandname() {

        return bandname;
    }

    public void setBandname(String bandname) {

        this.bandname = bandname;
    }

    public String getMacaddress() {

        return macaddress;
    }

    public void setMacaddress(String macaddress) {

        this.macaddress = macaddress;
    }

    public int getDaycount() {

        return daycount;
    }

    public void setDaycount(int daycount) {

        this.daycount = daycount;
    }

    public String getSNO() {

        return sno;
    }

    public void setSNO(String sno) {

        this.sno = sno;
    }

    public Boolean getReload() {

        return reload;
    }

    public void setReload(Boolean reload) {

        this.reload = reload;
    }

    public String getLoginType() {

        return logintype;
    }

    public void setLoginType(String logintype) {

        this.logintype = logintype;
    }

    public String getDate() {

        return date;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getDateFrom() {

        return from;
    }

    public void setDateFrom(String from) {

        this.from = from;
    }

    public String getDateTill() {

        return till;
    }

    public void setDateTill(String till) {

        this.till = till;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }


    public double getStepVal() {

        return stepVal;
    }

    public void setStepVal(double stepVal) {

        this.stepVal = stepVal;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSynctime() {

        return synctime;
    }

    public void setSynctime(String synctime) {

        this.synctime = synctime;
    }

    public String getLastname() {

        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public String getOldPassword() {

        return old_password;
    }

    public void setOldPassword(String old_pass) {
        this.old_password = old_pass;
    }

    public String getNewPassword() {

        return new_password;
    }

    public void setNewPassword(String new_pass) {
        this.new_password = new_pass;
    }

    public String getStep() {

        return step;
    }

    public void setStep(String step) {

        this.step = step;
    }

    public String getBike() {

        return bike;
    }

    public void setBike(String bike) {

        this.bike = bike;
    }

    public String getDistance() {

        return distance;
    }

    public void setDistance(String distance) {

        this.distance = distance;
    }

    public String getProfilepic() {

        return profilepic;
    }

    public void setProfilepic(String prop) {

        this.profilepic = prop;
    }
}
