package com.alexanderapps.rubbersidedown.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.fragments.LoginFrag;
import com.alexanderapps.rubbersidedown.listeners.LoginListener;
import com.google.firebase.auth.FirebaseUser;

public class LoginRegisterActivity extends AppCompatActivity implements View.OnClickListener, LoginListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);
        getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, LoginFrag.newInstance()).commit();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void loginButtonSelected(FirebaseUser user) {
        //login intent
        Intent profileIntent = new Intent(this, DashBoardActivity.class);
        profileIntent.putExtra("USER", user);
        startActivity(profileIntent);
        finish();
    }

}