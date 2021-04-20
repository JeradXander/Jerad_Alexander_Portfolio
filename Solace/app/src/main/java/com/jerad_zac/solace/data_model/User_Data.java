package com.jerad_zac.solace.data_model;

import java.io.Serializable;

public class User_Data implements Serializable {

    private String email;
    private String first_name;
    private String hardship;
    private String image;
    private boolean is_available;
    private String lastName;
    private String onlineStatus;
    private String typingTo;
    private String uid;
    private String username;
    private int karma;

    public User_Data(){

    }

    public User_Data(String email, String first_name, String hardship, String image, boolean is_available, int karma, String lastName, String onlineStatus, String typingTo, String uid, String username) {
        this.email = email;
        this.first_name = first_name;
        this.hardship = hardship;
        this.image = image;
        this.is_available = is_available;
        this.lastName = lastName;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.uid = uid;
        this.username = username;
        this.karma = karma;
    }


    // Getters

    public String getEmail() {
        return email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getHardship() {
        return hardship;
    }

    public String getImage() {
        return image;
    }

    public boolean isIs_available() {
        return is_available;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public int getKarma() {
        return karma;
    }

    // Setters


    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setHardship(String hardship) {
        this.hardship = hardship;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIs_available(boolean is_available) {
        this.is_available = is_available;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }
}
