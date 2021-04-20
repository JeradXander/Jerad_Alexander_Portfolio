package com.alexanderapps.rubbersidedown.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.fragments.RegisterFrag;
import com.alexanderapps.rubbersidedown.listeners.RegisterListener;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, RegisterListener {
    private static final String TAG = "MainActivity";
    Button registerButton;
    Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);

        getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, RegisterFrag.newInstance()).commit();
    }

    @Override
    public void onClick(View v) {


    }


    @Override
    public void registerButtonSelected(FirebaseUser user) {
        Intent profileIntent = new Intent(this, DashBoardActivity.class);
        profileIntent.putExtra("USER", user);
        startActivity(profileIntent);
        finish();
    }
}