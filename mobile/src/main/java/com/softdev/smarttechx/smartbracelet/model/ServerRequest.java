package com.softdev.smarttechx.smartbracelet.model;


public class ServerRequest {

    private String operation;
    private UserDetails user;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
