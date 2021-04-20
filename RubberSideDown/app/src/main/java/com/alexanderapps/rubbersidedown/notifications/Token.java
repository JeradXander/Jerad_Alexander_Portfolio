package com.alexanderapps.rubbersidedown.notifications;

public class Token {
    //a Firebase core messing token or registration token. is a id
    //issued by the GCMconnection servers to the client app that allows it to recieve messages
    String token;

    public Token(String token){
        this.token = token;
    }

    public Token(){

    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }
}

