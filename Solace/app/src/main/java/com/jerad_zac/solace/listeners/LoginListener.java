package com.jerad_zac.solace.listeners;

import com.google.firebase.auth.FirebaseUser;

public interface LoginListener {

    void loginButtonSelected(FirebaseUser user);

}

