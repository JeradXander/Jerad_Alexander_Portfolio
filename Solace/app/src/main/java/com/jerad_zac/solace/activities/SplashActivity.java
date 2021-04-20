package com.jerad_zac.solace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.jerad_zac.solace.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    //TODO: JERAD/ Comment code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                if (mAuth.getCurrentUser() != null) {
                    // User is signed in (getCurrentUser() will be null if not signed in)
                    Intent profileIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                    profileIntent.putExtra("USER", mAuth.getCurrentUser());

                    startActivity(profileIntent);
                    finish();
                }else{
                    Intent startIntent = new Intent(getApplicationContext(), StartingActivity.class);
                    startActivity(startIntent);
                    finish();
                }
            }
        }, 2750);


}
}
