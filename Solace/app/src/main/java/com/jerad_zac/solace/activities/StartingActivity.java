package com.jerad_zac.solace.activities;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.fragments.Login_Frag;
import com.jerad_zac.solace.fragments.Onboard1_Frag;
import com.jerad_zac.solace.fragments.Onboard2_Frag;
import com.jerad_zac.solace.listeners.LoginListener;
import com.jerad_zac.solace.listeners.OnBoardListener;
import com.jerad_zac.solace.listeners.RegisterListener;
import com.jerad_zac.solace.notification.FirebaseMessaging;
import com.jerad_zac.solace.utilities.PermissionUtils;

import java.util.HashMap;
import java.util.Objects;

public class StartingActivity extends AppCompatActivity implements LoginListener, RegisterListener , OnBoardListener {
    //TODO: JERAD/ Comment code
    private static final String TAG = "StartingActivity";
    FirebaseAuth mAuth;
    boolean[] permissionsbool = new boolean[3];
    Activity mActivity;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);
        mActivity = this;

       permissionsbool = PermissionUtils.hasPermissions(this);

        if (!permissionsbool[0] || !permissionsbool[1] || !permissionsbool[2]) {
            PermissionUtils.requestPermissions(this);
        }else{
            getSupportFragmentManager().beginTransaction().add(R.id.frag_container, Login_Frag.newInstance()).commit();
        }
       }


    @Override
    public void loginButtonSelected(FirebaseUser user) {
        //login intent
        Intent profileIntent = new Intent(this, DashboardActivity.class);
        profileIntent.putExtra("USER", user);
        //starts dashboard activity if user login is successful
        startActivity(profileIntent);
        finish();
    }

    @Override
    public void registerButtonSelected() {
        //  setting main fragment
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, Onboard1_Frag.newInstance()).addToBackStack("my_fragment").commit();
    }


    @Override
    public void onboardContinueSelected(String email, String password) {
        //  setting main fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, Onboard2_Frag.newInstance(email, password)).addToBackStack("my_fragment").commit();
    }

    @Override
    public void registerComplete() {
        //login intent
        Intent profileIntent = new Intent(this, DashboardActivity.class);
        startActivity(profileIntent);
        finish();
    }

    @Override
    public void loginSelected() {
        //  setting main fragment
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, Login_Frag.newInstance()).addToBackStack("my_fragment").commit();
    }

    private void alertDialogForMissingPermission(){
        new AlertDialog.Builder(this)
                .setTitle("Missing Permission")
                .setMessage("In order to use Solace we need to access all permissions")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {

                            Intent restartActivity = new Intent(mActivity, StartingActivity.class);
                            startActivity(restartActivity);
                            finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allGranted = false;
        for(int grant: grantResults){
            if(grant == PackageManager.PERMISSION_GRANTED){
                allGranted = true;
            }else {
                allGranted = false;
            }
        }

        if(allGranted){
            getSupportFragmentManager().beginTransaction().add(R.id.frag_container, Login_Frag.newInstance()).commit();
        }else {
            alertDialogForMissingPermission();
        }

    }

    public static class Restarter extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Broadcast Listened", "Service tried to stop");
            Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, FirebaseMessaging.class));
            } else {
                context.startService(new Intent(context, FirebaseMessaging.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
