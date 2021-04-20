package com.alexanderapps.rubbersidedown.listeners;

import com.google.firebase.auth.FirebaseUser;

public interface RegisterListener {

    void registerButtonSelected(FirebaseUser user);
}

