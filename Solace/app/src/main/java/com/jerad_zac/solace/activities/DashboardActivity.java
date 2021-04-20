package com.jerad_zac.solace.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.data_model.Api_Data;
import com.jerad_zac.solace.data_model.BOW_Data;
import com.jerad_zac.solace.fragments.Crisis_Frag;
import com.jerad_zac.solace.fragments.Home_Frag;
import com.jerad_zac.solace.fragments.Profile_Frag;
import com.jerad_zac.solace.fragments.WebView_Frag;
import com.jerad_zac.solace.listeners.CrisisHotlineListener;
import com.jerad_zac.solace.listeners.LoadWebViewListener;
import com.jerad_zac.solace.listeners.SignOutListener;
import com.jerad_zac.solace.notification.Token;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class DashboardActivity extends AppCompatActivity implements SignOutListener, CrisisHotlineListener, LoadWebViewListener {

    private static final String TAG = "DashboardActivity";
    FirebaseAuth mAuth;
    private String uid;
    BottomNavigationView navigationView;
    int selectedFragementId = 0;
    boolean newPost = false;
    boolean fromProfile = false;
    Api_Data api_data = new Api_Data(null,null,null,null,null);
    boolean quoteApiFinished = false;
    boolean bookApiFinished = false;
    Date date = new Date();
    long DAY_IN_MS = 1000 * 60 * 60 * 24;
    Dialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_main);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);
        setProgress();

        //instance of auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        date = new Date();
        SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);


        // Checks internet connection.
        // If good - start DataTask to pull API Data
        if (checkConnection()) {
            // Quote DataTask begins

            DataTask dataTask = new DataTask();
            dataTask.execute("https://type.fit/api/quotes");


            whichNav(0);
            updateFrag();

        } else {
            whichNav(0);

            updateFrag();
        }
        //update Token
        updateToken(FirebaseInstanceId.getInstance().getToken());
        //get current user from Shared Preferences



    }

    // logic on which navigation bar to display
    public void whichNav(int index) {
        BottomNavigationView navView_Home = findViewById(R.id.nav_bar_home);
        BottomNavigationView navView_Web = findViewById(R.id.nav_bar_web);
        if (index == 0) {
            navView_Home.setVisibility(View.VISIBLE);
            navView_Web.setVisibility(View.INVISIBLE);
            navigationView = navView_Home;
            navView_Home.setOnNavigationItemSelectedListener(selectedListener);
        } else {
            navView_Home.setVisibility(View.INVISIBLE);
            navView_Web.setVisibility(View.VISIBLE);
            navigationView = navView_Web;
            navView_Web.setOnNavigationItemSelectedListener(selectedListener);
        }
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(uid).setValue(mToken);
    }
    //registration_progress bar builder method
    private void setProgress()
    {

        //setting up dialog box to hold progressbar and text view
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setView(R.layout.listener_loading);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

    }

    // Calls next DataTask Execution
    private void nextApiPull() {
        DataTask dataTask = new DataTask();
        if (quoteApiFinished) {
            dataTask.execute("https://www.googleapis.com/books/v1/volumes?q=self%20help");
        }
    }

    // Checks if can connect to internet, if not, error message
    private boolean checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo info = mgr.getActiveNetworkInfo();
            if (info != null) {
                return true;
            } else {
                Log.i(TAG, "checkConnection: NetworkInfo is null");
            }
        } else {
            Log.i(TAG, "checkConnection: ConnectivityManager is null");
        }
        return false;
    }

    @Override
    public void openWebPage(String url) {
        whichNav(1);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, WebView_Frag.newInstance(url)).addToBackStack("tag").commit();
    }



    // Pulls JSON data
    private class DataTask extends AsyncTask<String, Void, List<String>> {

        // Tests connection to api link and pulls data from api url
        @Override
        protected List<String> doInBackground(String... params) {
            try {
                // Api connection
                URL url = new URL(params[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();
                String data = IOUtils.toString(is);
                is.close();
                connection.disconnect();

                // List to pass to onPostExecute which holds the key to which api data it's pulling
                List<String> whichUrl = new ArrayList<>();

                // quote api
                if (params[0].equals("https://type.fit/api/quotes")) {
                    whichUrl.add("one");
                    whichUrl.add(data);
                }
                // book api
                else {
                    whichUrl.add("two");
                    whichUrl.add(data);
                }

                return whichUrl;

            } catch (IOException e) {
                Log.i(TAG, "doInBackground: " + e);
            }

            Log.i(TAG, "doInBackground: doInBackground returned null value");
            return null;
        }

        // Json pull
        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);

            try {
                if (s.get(0).equals("one")) {
                    // Alert Dialog - loading
                    dialog.show();
                    TextView label = dialog.findViewById(R.id.labelIV);
                    String loading = "Loading";
                    label.setText(loading);

                    // JSON pull
                    String quoteOfDay = "";
                    JSONArray outerMostObjects = new JSONArray(s.get(1));
                    int rnd = new Random().nextInt(outerMostObjects.length());
                    JSONObject object = outerMostObjects.getJSONObject(rnd);
                    quoteOfDay = "\"" + object.getString("text") + "\"";
                    quoteOfDay += (" - " + object.getString("author"));

                    // Overwrites api-data's blank entries
                    api_data.setQuote(quoteOfDay);
                    quoteApiFinished = true;


                    SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);


                    Gson gson = new Gson();
                    String json = mPrefs.getString("bookOfWeek", "");
                    BOW_Data bookOfWeek = gson.fromJson(json, BOW_Data.class);
                    if(bookOfWeek != null){
                    if(isWithinWeek(bookOfWeek.getTimestamp())) {
                        api_data.setBookTitle(bookOfWeek.getBookTitle());
                        api_data.setBookDesc(bookOfWeek.getBookDesc());
                        api_data.setBookLink(bookOfWeek.getBookLink());
                        api_data.setBookThumb(bookOfWeek.getBookThumb());
                        bookApiFinished = true;
                        updateFrag();
                    }
                    }else {

                        nextApiPull();
                    }
                }
                else if (s.get(0).equals("two")) {
                    JSONObject outerMostObject = new JSONObject(s.get(1));
                    JSONArray items = outerMostObject.getJSONArray("items");
                    int rnd = new Random().nextInt(items.length());
                    JSONObject object = items.getJSONObject(rnd);
                    JSONObject volumeInfo = object.getJSONObject("volumeInfo");

                    String title = volumeInfo.getString("title");
                    String description = volumeInfo.getString("description");

                    JSONObject thumbArray = volumeInfo.getJSONObject("imageLinks");
                    String thumb = thumbArray.getString("smallThumbnail");

                    String link = volumeInfo.getString("canonicalVolumeLink");

                    // Overwrites api-data's blank entries
                    api_data.setBookTitle(title); api_data.setBookDesc(description);
                    api_data.setBookThumb(thumb); api_data.setBookLink(link);

                    //adding book as book of week

                    BOW_Data bookOfWeek = new BOW_Data(title,description,thumb,link, date);

                    SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(bookOfWeek);
                    prefsEditor.putString("bookOfWeek", json);
                    prefsEditor.apply();

                    bookApiFinished = true;
                }

                if (quoteApiFinished && bookApiFinished) {
                    // updates the frag
                    updateFrag();
                }


            } catch (JSONException e) {
                Log.d(TAG, "onPostExecute: " + e);
                dialog.dismiss();
            }
        }
    }

    // Handles the bottom navigation bar that appears on most pages
    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener  = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedFragementId = navigationView.getSelectedItemId();
            switch (item.getItemId()) {
                case R.id.nav_home:
                    if (selectedFragementId != item.getItemId() || newPost || fromProfile) {
                        newPost = false;
                        fromProfile = false;
                        Log.d(TAG, "Home frag");
                        //this transistions to correct fragment
                        updateFrag();
                  }
                    return true;
                case R.id.nav_profile:
                    if (selectedFragementId != item.getItemId() ) {
                        Log.d(TAG, "Profile frag");
                        //this transistions to correct fragment
                        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, Profile_Frag.newInstance(uid)).addToBackStack("tag").commit();

                    }
                    return true;
                case R.id.backToHomeNavBtn:
                    updateFrag();
                    whichNav(0);
                    return true;
                default:
                    Log.d(TAG, "Shouldnt happen");
            }
            return false;
        }
    };


    @Override
    public void SignOutPressed() {
        //signing out user going to splash
        mAuth.signOut();
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    @Override
    public void toCrisisFrag() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, Crisis_Frag.newInstance()).addToBackStack("tag").commit();
    }

    // Checks to make sure Api_Data is null then updates the frag accordingly
    private void updateFrag() {
        if (api_data.getBookTitle() != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.frag_containner, Home_Frag.newInstance(api_data)).addToBackStack("tag").commit();
            dialog.dismiss();
        } else {
            dialog.dismiss();
            // Generic api-data in case no internet
            api_data.setQuote("\"" + "I always advice people - Don't wait ! Do something when you are young, " +
                    "when you have no responsibilities. Invest time in yourself to have great Experiences" +
                    " that are going to enrich you, then you can't possibly lose." + "\" - Steve Jobs" );
            api_data.setBookTitle("Relentless");
            api_data.setBookDesc("An award-winning trainer draws on experience with such top athletes " +
                    "as Michael Jordan, Kobe Bryant and Ken Griffey, Jr. to explain how to tap " +
                    "dark competitive reflexes in order to succeed regardless of circumstances, explaining " +
                    "the importance of finding internal resources and harnessing the power of personal fears and instincts.");
            api_data.setBookThumb("http://books.google.com/books/content?id=ZK36AgAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api");
            api_data.setBookLink("https://books.google.com/books/about/Relentless.html?hl=&id=ZK36AgAAQBAJ");


            getSupportFragmentManager().beginTransaction().replace(R.id.frag_containner, Home_Frag.newInstance(api_data)).addToBackStack("tag").commit();

        }
    }

    @Override
    public void onBackPressed() {
        int backstackcount = getSupportFragmentManager().getBackStackEntryCount();
        if(backstackcount > 0){
            this.moveTaskToBack(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
        checkOnlineStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnlineStatus("online");
    }

    private void checkOnlineStatus(String status) {
        //checking online status
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        HashMap<String, Object> hasmap = new HashMap<>();
        hasmap.put("onlineStatus", status);

        dRef.updateChildren(hasmap);
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            //user signed in
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }

    boolean isWithinWeek(Date lastBook) {

        if (lastBook.after(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS)))){
            return true;
        }else{
            return false;
        }
    }
}