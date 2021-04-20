package com.alexanderapps.rubbersidedown.listeners;

import com.google.firebase.auth.FirebaseUser;

public interface LoginListener {

    void loginButtonSelected(FirebaseUser user);

}

