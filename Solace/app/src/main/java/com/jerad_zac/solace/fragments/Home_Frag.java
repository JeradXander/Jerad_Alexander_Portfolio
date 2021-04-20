package com.jerad_zac.solace.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.activities.ChatActivity;
import com.jerad_zac.solace.data_model.Api_Data;
import com.jerad_zac.solace.data_model.ChatList_Data;
import com.jerad_zac.solace.data_model.Listener_Data;
import com.jerad_zac.solace.listeners.CrisisHotlineListener;
import com.jerad_zac.solace.listeners.LoadWebViewListener;
import com.jerad_zac.solace.listeners.LoginListener;
import com.jerad_zac.solace.listeners.SignOutListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Home_Frag extends Fragment implements View.OnClickListener {

    private static final String TAG = "Home_Frag";
    SignOutListener mListener;
    CrisisHotlineListener mCrisisListener;
    LoadWebViewListener mWebViewListener;
    private Api_Data api_data;
    Button random_buttom;
    Button listener_button;
    Dialog dialog;
    private CountDownTimer listenerTimer;
    private CountDownTimer talkerTimer;
    int DIALOG_SIZE = 100;
    FirebaseUser mUser;
    List<Listener_Data> listenerList;
    List<Listener_Data> randomList;
    List<String> blockedUids;

    Random randomGenerator;
    String listenerUid = "";
    String talkerUid = "";
    String hardship = "";
    boolean matched = false;
    boolean searching = false;


        public static Home_Frag newInstance(Api_Data api_data) {

            Bundle args = new Bundle();
            args.putSerializable(TAG, api_data);
            Home_Frag fragment = new Home_Frag();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.home_layout, container, false);
        }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //attaching signout listener
        if (context instanceof SignOutListener) {
            mListener = (SignOutListener) context;
        }
        else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

        if (context instanceof CrisisHotlineListener) {
            mCrisisListener = (CrisisHotlineListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

        if (context instanceof LoadWebViewListener) {
            mWebViewListener = (LoadWebViewListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        blockedUids = new ArrayList<>();
        setProgress();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        loadUser();
        getBlockedusers();
        listenerForMatch();
        listeningCountdownTimer();
        randomCountdownTimer();
        listener_button = Objects.requireNonNull(getView()).findViewById(R.id.listener_button);
        random_buttom = Objects.requireNonNull(getView()).findViewById(R.id.select_random_button);
        listener_button.setOnClickListener(this);
        random_buttom.setOnClickListener(this);

        if (getArguments() != null && getView() != null) {

            api_data = (Api_Data) getArguments().getSerializable(TAG);

            if (api_data != null) {
                Log.d(TAG, "onActivityCreated: " + api_data.getBookDesc());

                TextView quoteTV = getView().findViewById(R.id.quoteOfDayTV);
                ImageButton crisisImgBtn = getView().findViewById(R.id.crisisHotlineImgBtn);
                ImageButton bookImgBtn = getView().findViewById(R.id.bookImgBtn);
                TextView bookTitleTV = getView().findViewById(R.id.bookTitleTV);
                TextView bookDescTV = getView().findViewById(R.id.bookDescTV);
                FrameLayout bookFrame = getView().findViewById(R.id.bookOfWeekFrame);

                quoteTV.setText(api_data.getQuote());
                bookTitleTV.setText(api_data.getBookTitle());
                bookDescTV.setText(api_data.getBookDesc());

                // Book Image button setup
                Picasso.get()
                        .load(api_data.getBookThumb())
                        .into(bookImgBtn);

                bookFrame.setOnClickListener(this);

                // Crisis Hotline button setup
                crisisImgBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCrisisListener.toCrisisFrag();
                    }
                });
            }


            // Resources setup
            Button anxietyResBtn = getView().findViewById(R.id.resAnxietyBtn);
            Button autismResBtn = getView().findViewById(R.id.resAutismBtn);
            Button depressionResBtn = getView().findViewById(R.id.resDepressionBtn);
            Button lgbtResBtn = getView().findViewById(R.id.resLGBTBtn);
            Button eatingDisResBtn = getView().findViewById(R.id.resEatingDisorderBtn);
            Button substanceResBtn = getView().findViewById(R.id.resSubstanceAbuseBtn);
            Button mentalIllnessResBtn = getView().findViewById(R.id.resMentalIllnessBtn);
            Button veteransWebResBtn = getView().findViewById(R.id.resVeteransBtn);
            Button domesticHotlineResBtn = getView().findViewById(R.id.resDomesticViolenceHotlineBtn);
            Button veteranHotlineResBtn = getView().findViewById(R.id.resVeteranHotlineBtn);

            anxietyResBtn.setOnClickListener(this);
            autismResBtn.setOnClickListener(this);
            depressionResBtn.setOnClickListener(this);
            lgbtResBtn.setOnClickListener(this);
            eatingDisResBtn.setOnClickListener(this);
            substanceResBtn.setOnClickListener(this);
            mentalIllnessResBtn.setOnClickListener(this);
            veteransWebResBtn.setOnClickListener(this);
            domesticHotlineResBtn.setOnClickListener(this);
            veteranHotlineResBtn.setOnClickListener(this);
        }


    }

    // Handles Resource Button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bookOfWeekFrame:
                mWebViewListener.openWebPage(api_data.getBookLink());
                break;
            case R.id.resAnxietyBtn:
                mWebViewListener.openWebPage("http://www.adaa.org/living-with-anxiety/ask-and-learn/resources");
                break;
            case R.id.resAutismBtn:
                mWebViewListener.openWebPage("http://www.autismspeaks.org/");
                break;
            case R.id.resDepressionBtn:
                mWebViewListener.openWebPage("http://www.dbsalliance.org/");
                break;
            case R.id.resLGBTBtn:
                mWebViewListener.openWebPage("http://www.glbtnationalhelpcenter.org/");
                break;
            case R.id.resEatingDisorderBtn:
                mWebViewListener.openWebPage("https://www.nationaleatingdisorders.org/");
                break;
            case R.id.resSubstanceAbuseBtn:
                mWebViewListener.openWebPage("http://www.samhsa.gov/");
                break;
            case R.id.resMentalIllnessBtn:
                mWebViewListener.openWebPage("http://www.mayoclinic.org/diseases-conditions/mental-illness/basics/definition/con-20033813");
                break;
            case R.id.resVeteransBtn:
                mWebViewListener.openWebPage("https://www.nami.org/Find-Support/Veterans-and-Active-Duty");
                break;
            case R.id.resDomesticViolenceHotlineBtn:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Uri number = Uri.parse("tel:18007997233");
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                if (getContext() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Dial National Domestic Violence Hotline").setMessage("Are you sure you wish to call the National Domestic Violence Hotline?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                break;
            case R.id.resVeteranHotlineBtn:
                DialogInterface.OnClickListener vetDialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Uri number = Uri.parse("tel:18002738255");
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                if (getContext() != null) {
                    AlertDialog.Builder builderVet = new AlertDialog.Builder(getContext());
                    builderVet.setTitle("Dial Veteran Crisis Hotline").setMessage("Are you sure you wish to call the Veteran Crisis Hotline?").setPositiveButton("Yes", vetDialogClickListener)
                            .setNegativeButton("No", vetDialogClickListener).show();
                }
                break;
            case R.id.select_random_button:

                startRandomMethod();
                break;
            case R.id.listener_button:
                startListnerMethod();

                break;
            default:
                Log.d(TAG, "HomeFrag - resourceListener's onClick: This shouldn't happen.");
                break;
        }
    }

    private void listenerForMatch() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Searching").child(mUser.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(searching){
                    matched = snapshot.child("matched").getValue(boolean.class);
                    String matched_uid = snapshot.child("matched_user").getValue(String.class);

                    if(matched){
                        TextView label = dialog.findViewById(R.id.labelIV);
                        listenerTimer.start().onTick(5000);
                        label.setText("Matched " );
                        talkerUid =  matched_uid;
                        listenerTimer.cancel();
                        listeningCountdownTimer();
                        listenerTimer.start();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void startListnerMethod() {
        dialog.show();
        matched = false;
        listenerTimer.start().onTick(30000);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        searchingAsHelper(true);
    }

    private void startRandomMethod() {
        dialog.show();
        talkerTimer.start();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        searchingForHelper();
    }



    //registration_progress bar builder method
    private void setProgress()
    {
        //setting up dialog box to hold progressbar and text view
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext(),R.style.AlertDialog);
        builder.setView(R.layout.listener_loading);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                searchingAsHelper(false);
                listenerTimer.cancel();
                talkerTimer.cancel();
            }
        });
        dialog = builder.create();
    }


    private void listeningCountdownTimer() {
        if(matched){
            listenerTimer = new CountDownTimer(5000, 1000) {

                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    if(dialog.isShowing()){
                        TextView countdowm = dialog.findViewById(R.id.countdownIV);
                        countdowm.setText(String.valueOf(millisUntilFinished/1000));
                    }
                }

                public void onFinish() {
                    if(!talkerUid.equals("")){

                        HashMap<String, Object> searchingMap = new HashMap<>();
                        searchingMap.put("matched",false);
                        searchingMap.put("matched_user","");
                        searchingMap.put("hardship", "");

                        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                        DatabaseReference ref = fdb.getReference("Searching");
                        ref.child(mUser.getUid()).setValue(searchingMap);
                        searchingAsHelper(false);
                        Log.d(TAG,"Presenting Chat");
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("uid", talkerUid);
                        getContext().startActivity(chatIntent);
                        talkerUid = "";
                        matched = false;
                        listeningCountdownTimer();
                    }
                    dialog.dismiss();
                    searchingAsHelper(false);
                }
            };
        }else{
            listenerTimer = new CountDownTimer(30000, 1000) {

                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    if(dialog.isShowing()){
                        TextView countdowm = dialog.findViewById(R.id.countdownIV);
                        countdowm.setText(String.valueOf(millisUntilFinished/1000));
                    }
                }

                public void onFinish() {
                    if(!talkerUid.equals("")){
                        searching = false;
                        searchingAsHelper(false);
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("uid", talkerUid);
                        Log.d(TAG,"Presenting Chat");
                        getContext().startActivity(chatIntent);
                        talkerUid = "";
                    }
                    dialog.dismiss();
                    searching = false;
                    searchingAsHelper(false);
                }
            };
        }
    }

    private void randomCountdownTimer() {
        talkerTimer = new CountDownTimer(5000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                TextView countdowm = dialog.findViewById(R.id.countdownIV);
                countdowm.setText(String.valueOf(millisUntilFinished/1000));

            }

            public void onFinish() {
                if(!listenerUid.equals("")){

                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                    chatIntent.putExtra("uid", listenerUid);
                    getContext().startActivity(chatIntent);
                    listenerUid = "";
                }
                dialog.dismiss();
            }
        };
    }



    private void searchingAsHelper(boolean isSearching){
        searching = true;
        TextView label = dialog.findViewById(R.id.labelIV);
        label.setText("Searching for Person in need");
        HashMap<String, Object> searchingMap = new HashMap<>();
        searchingMap.put("searching",isSearching);
        searchingMap.put("matched",false);
        searchingMap.put("matched_user","");
        searchingMap.put("hardship", hardship);
        searchingMap.put("uid",mUser.getUid());

        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        DatabaseReference ref = fdb.getReference("Searching");
        ref.child(mUser.getUid()).setValue(searchingMap);
    }

    private void searchingForHelper() {
        randomGenerator = new Random();
        TextView label = dialog.findViewById(R.id.labelIV);
        label.setText("Searching for Listener");
        listenerList = new ArrayList<>();
        randomList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Searching");
        Query query = ref.orderByChild("searching").equalTo(true).limitToFirst(1000);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listenerList.clear();
                randomList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    Listener_Data modelListener = new Listener_Data(ds.child("uid").getValue(String.class), ds.child("searching").getValue(boolean.class),ds.child("matched").getValue(boolean.class),
                            ds.child("matched_user").getValue(String.class),  ds.child("hardship").getValue(String.class));

                    if(!blockedUids.isEmpty() && blockedUids.contains(modelListener.getUid())){
                        Log.d(TAG, "onDataChange: blocked" + modelListener.getUid());
                    }else{
                        if(modelListener.isSearching() && modelListener.getHardship().equals(hardship)){
                            listenerList.add(modelListener);
                        }
                        else if (modelListener.isSearching()) {
                            randomList.add(modelListener);
                        }
                    }
                }

                if(listenerList.size() > 0){
                    int index = randomGenerator.nextInt(listenerList.size());
                    HashMap<String, Object> searchingMap = new HashMap<>();
                    searchingMap.put("matched",true);
                    searchingMap.put("matched_user",mUser.getUid());
                    searchingMap.put("searching",listenerList.get(index).isSearching());
                    searchingMap.put("uid",listenerList.get(index).getUid());

                    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                    DatabaseReference ref = fdb.getReference("Searching");
                    ref.child(listenerList.get(index).getUid()).setValue(searchingMap);
                    listenerUid = listenerList.get(index).getUid();

                }else if (randomList.size() > 0 ){
                    TextView label = dialog.findViewById(R.id.labelIV);
                    label.setText(R.string.randomlistener);
                    int index = randomGenerator.nextInt(randomList.size());
                    HashMap<String, Object> searchingMap = new HashMap<>();
                    searchingMap.put("matched",true);
                    searchingMap.put("matched_user",mUser.getUid());
                    searchingMap.put("searching",randomList.get(index).isSearching());
                    searchingMap.put("uid",randomList.get(index).getUid());

                    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                    DatabaseReference ref = fdb.getReference("Searching");
                    ref.child(randomList.get(index).getUid()).setValue(searchingMap);
                    listenerUid = randomList.get(index).getUid();

                }
                else {
                    TextView label = dialog.findViewById(R.id.labelIV);
                    label.setText(R.string.none_found);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG,  error.getMessage());
            }
        });
    }

    private void loadUser(){
        //database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = firebaseDatabase.getReference("Users");

        Query userQuery = myRef.orderByChild("uid").equalTo(mUser.getUid());
        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required if is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    hardship = Objects.requireNonNull(ds.child("hardship").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getBlockedusers(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users/" + mUser.getUid()).child("ChatList");
        blockedUids = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blockedUids.clear();
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                for (DataSnapshot ds : contactChildren) {
                    ChatList_Data data = ds.getValue(ChatList_Data.class);

                    if (data.isBlocked()){
                        blockedUids.add(data.getId());
                        Log.d(TAG, "onDataChange: user is blocked "  + data.getId());
                    }else{

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

