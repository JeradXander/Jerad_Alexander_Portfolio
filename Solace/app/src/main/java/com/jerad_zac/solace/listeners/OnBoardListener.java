package com.jerad_zac.solace.listeners;

public interface OnBoardListener {

    void onboardContinueSelected(String email, String password);
    void registerComplete();
    void loginSelected();
}

