package com.jerad_zac.solace.data_model;

public class ChatList_Data {

    String id;
    boolean blocked;

    public ChatList_Data(String id, boolean blocked) {
        this.id = id;
        this.blocked = blocked;
    }

    public ChatList_Data() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public boolean isBlocked() {
        return blocked;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
