package com.softdev.smarttechx.smartbracelet.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 12/24/2017.
 */

public class UserList {
    @SerializedName("user")
    @Expose
    private ArrayList<UserDetails> user = new ArrayList<>();

    private String result;
    private String message;

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    /**
     * @return The users
     */
    public ArrayList<UserDetails> getUsers() {
        return user;
    }

    /**
     * @param user The users
     */
    public void setUserList(ArrayList<UserDetails> user) {
        this.user = user;
    }
}
