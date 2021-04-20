package com.alexanderapps.rubbersidedown.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.fragments.ChatListFrag;
import com.alexanderapps.rubbersidedown.fragments.HomeFrag;
import com.alexanderapps.rubbersidedown.fragments.NewPostFrag;
import com.alexanderapps.rubbersidedown.fragments.ProfileFrag;
import com.alexanderapps.rubbersidedown.fragments.TheirProfileFrag;
import com.alexanderapps.rubbersidedown.fragments.UsersFrag;
import com.alexanderapps.rubbersidedown.listeners.NewPostListener;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.alexanderapps.rubbersidedown.notifications.Token;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DashBoardActivity extends AppCompatActivity implements SignOutListener, LocationListener, NewPostListener, ProfileSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int REQUEST_ALL = 0x101;


    BottomNavigationView navigationView;

    FloatingActionButton fab;
    int selectedFragementId = 0;
    boolean newPost = false;
    boolean fromProfile = false;
    public static String finalAddress;

    private FirebaseAuth mAuth;
    String mUid;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);

        //checking permissions
        if (!hasPermissions()[0] || !hasPermissions()[1] || !hasPermissions()[2]) {
            requestPermissions();
        } else {
            LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

            startLocationUpdates();
            finalAddress = locationToString(Objects.requireNonNull(mgr).getLastKnownLocation(LocationManager.GPS_PROVIDER));
            Log.d(TAG, finalAddress);
        }

        //geting firebase auth and uid
        mAuth = FirebaseAuth.getInstance();
        mUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("28038942445-6l581fvs48ml7drfjqivoktm084ifua5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        navigationView = findViewById(R.id.nav_bar);
        navigationView.setItemIconTintList(ColorStateList.valueOf(Color.WHITE));
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        //floating action button
        fab = findViewById(R.id.Fab);

        //fab listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hiding fab and getting form fragment
                fab.hide();
                newPost = true;

                getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, NewPostFrag.newInstance()).addToBackStack("tag").commit();
            }
        });

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);

        if (sp.getBoolean("FirstLogIN", true)) {
            getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, ProfileFrag.newInstance()).commit();
        }

        //setting home frag
        getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, HomeFrag.newInstance()).commit();

        checkUserStatus();

        //update Token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUid).setValue(mToken);
    }

    //listenr for navigation bar
    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fab.show();
            selectedFragementId = navigationView.getSelectedItemId();
            switch (item.getItemId()) {
                case R.id.nav_home:
                    if (selectedFragementId != item.getItemId() || newPost || fromProfile) {
                        newPost = false;
                        fromProfile = false;

                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, HomeFrag.newInstance()).addToBackStack("tag").commit();
                    }
                    return true;
                case R.id.nav_profile:
                    if (selectedFragementId != item.getItemId() || newPost || fromProfile) {
                        newPost = false;
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, ProfileFrag.newInstance()).addToBackStack("tag").commit();
                    }
                    return true;
                case R.id.nav_users:

                    if (selectedFragementId != item.getItemId() || newPost || fromProfile) {
                        newPost = false;
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, UsersFrag.newInstance()).addToBackStack("tag").commit();

                    }
                    return true;
                case R.id.nav_messsages:
                    if (selectedFragementId != item.getItemId() || newPost || fromProfile) {
                        newPost = false;
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, ChatListFrag.newInstance()).addToBackStack("tag").commit();

                    }
                    return true;
                default:
                    Log.d(TAG, "Shouldnt happen");
            }
            return false;
        }
    };


    private void requestPermissions() {

        if (!hasPermissions()[0] || !hasPermissions()[1] || !hasPermissions()[2]) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ALL);
        }
    }

    private boolean[] hasPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return new boolean[]{result, result1, result2};
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void startLocationUpdates() {
        //starting location updates
        LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mgr != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ALL);

                return;
            }
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10.0f, this);
        }
    }

    @Override
    public void SignOutPressed() {

        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        finalAddress = locationToString(location);
    }
    //method for location to string
    private String locationToString(Location location) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault()); //it is Geocoder
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String addressSt = address.get(0).getLocality();


            return addressSt + ", " + address.get(0).getAdminArea(); //This is the complete address.
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void checkUserStatus() {
        //checking user status
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //user signed in
            mUid = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUid);

            editor.apply();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public void newPostComplete() {
        newPost = false;
        fromProfile = true;
        getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, HomeFrag.newInstance()).commit();
        fab.show();
    }

    @Override
    public void theirProfileSelected(String uid) {

        if (!uid.equals(mUid)) {
            newPost = false;
            fromProfile = true;
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, TheirProfileFrag.newInstance(uid)).addToBackStack("tag").commit();
            fab.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fab.show();
    }
}
