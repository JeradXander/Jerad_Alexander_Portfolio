package com.alexanderapps.rubbersidedown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.fragments.MainFrag;
import com.alexanderapps.rubbersidedown.listeners.Login_Register_Listener;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements Login_Register_Listener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);

        //getting current user and logging in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            Intent profileIntent = new Intent(this, DashBoardActivity.class);
            profileIntent.putExtra("USER", mAuth.getCurrentUser());
            startActivity(profileIntent);
            finish();
        }

        //setting main fragment
        getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, MainFrag.newInstance()).commit();

    }

    //listend for login and register
    @Override
    public void loginSelected() {
        Intent loginIntent = new Intent(this, LoginRegisterActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public void registerSelected() {
        Intent regIntent = new Intent(this, RegistrationActivity.class);
        startActivity(regIntent);
    }
}