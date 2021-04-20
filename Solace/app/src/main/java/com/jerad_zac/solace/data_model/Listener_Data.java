package com.jerad_zac.solace.data_model;

public class Listener_Data {

    String uid;
    boolean searching;
    boolean matched;
    String matched_user;
    String hardship;



    public Listener_Data(String uid, boolean searching, boolean matched, String matched_user, String _hardship) {
        this.uid = uid;
        this.searching = searching;
        this.matched = matched;
        this.matched_user = matched_user;
        this.hardship = _hardship;
    }


    public String getHardship() {
        return hardship;
    }

    public void setHardship(String hardship) {
        this.hardship = hardship;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isSearching() {
        return searching;
    }

    public void setSearching(boolean searching) {
        this.searching = searching;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getMatched_user() {
        return matched_user;
    }

    public void setMatched_user(String matched_user) {
        this.matched_user = matched_user;
    }



}
