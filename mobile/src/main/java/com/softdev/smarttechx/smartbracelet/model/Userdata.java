package com.softdev.smarttechx.smartbracelet.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by SMARTTECHX on 11/2/2017.
 */

public class Userdata implements Serializable {

    public static Comparator<Userdata> step_sort = new Comparator<Userdata>() {

        public int compare(Userdata s1, Userdata s2) {

            int userstep1 = Integer.valueOf(s1.getStepdata());
            int userstep2 = Integer.valueOf(s2.getStepdata());

            return userstep2 - userstep1;
        }
    };
    public static Comparator<Userdata> bike_sort = new Comparator<Userdata>() {

        public int compare(Userdata s1, Userdata s2) {

            int userstep1 = Integer.valueOf(s1.getBikedata());
            int userstep2 = Integer.valueOf(s2.getBikedata());

            return userstep2 - userstep1;
        }
    };
    public String no;
    public String datatype;
    public String name;
    public String email;
    public String Stepdata;
    public String Bikedata;
    private String firstname;
    private String lastname;
    private String synctime;
    private String password;
    private String logintype;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getData_type() {
        return datatype;
    }

    public void setData_type(String datatype) {
        this.datatype = datatype;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginType() {

        return logintype;
    }

    public void setLoginType(String logintype) {

        this.logintype = logintype;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public String getSynctime() {

        return synctime;
    }

    public void setSynctime(String synctime) {

        this.synctime = synctime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStepdata() {
        return Stepdata;
    }

    public void setStepdata(String Stepdata) {
        this.Stepdata = Stepdata;
    }

    public String getBikedata() {
        return Bikedata;
    }

    public void setBikedata(String Bikedata) {
        this.Bikedata = Bikedata;
    }
}
