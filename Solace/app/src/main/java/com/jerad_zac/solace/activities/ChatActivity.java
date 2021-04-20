package com.jerad_zac.solace.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.adapters.Chat_Adapter;
import com.jerad_zac.solace.data_model.Chat_Data;
import com.jerad_zac.solace.notification.Data;
import com.jerad_zac.solace.notification.Sender;
import com.jerad_zac.solace.notification.Token;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    //TODO: JERAD/ Comment code
    //variables
    Context mContext;
    //volely request queue for notification
    private RequestQueue requestQueue;
    boolean notify = false;
    String selecteduid;
    String myuid;
    String selectedUsersImage;
    String chatterUsername;
    String chatterKarma;
    String chatterHardship;
    AlertDialog dialog;
    LinearLayoutManager linearLayoutManager;
    List<Chat_Data> chatList;

    //TODO: notifications beta
    //   APIService apiService;

    //views from xml
    Toolbar toolbar;
    RecyclerView recycleView;
    ImageView profileIV;
    TextView nameTV;
    TextView onlineStatusTV;
    EditText messageET;
    ImageButton sendButton;
    Chat_Adapter adapterChat;


    //Firebase
    FirebaseUser user;
    private FirebaseAuth mAuth;
    DatabaseReference userDatabaseReference;
    ValueEventListener seenListener;
    ValueEventListener seenListener2;
    DatabaseReference userRefForSeen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        overridePendingTransition(R.anim.act_fadein, R.anim.act_fade_ou);

        mContext = this;
        //getting intent
        Intent intent = getIntent();
        selecteduid = intent.getStringExtra("uid");

        //instance of firebase auth
        mAuth = FirebaseAuth.getInstance();
        myuid = mAuth.getCurrentUser().getUid();

        //initializing views and variables
        chatList = new ArrayList<>();

        toolbar = findViewById(R.id.chat_tool);
        toolbar.setOnClickListener(toolBarClickListener);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        profileIV = findViewById(R.id.profileIV);
        nameTV = findViewById(R.id.chat_name);
        onlineStatusTV = findViewById(R.id.user_online_status);
        recycleView = findViewById(R.id.chat_recyclerView);
        messageET = findViewById(R.id.chat_message);
        sendButton = findViewById(R.id.chat_send);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //Layout for recycleView
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.scrollToPosition(chatList.size() - 1);
        linearLayoutManager.setStackFromEnd(true);
        //recycle view
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);


        //database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseReference = firebaseDatabase.getReference("Users");

        Query userQuery = userDatabaseReference.orderByChild("uid").equalTo(selecteduid);

        //listener for chat
        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required if is recieved
                for (DataSnapshot ds : snapshot.getChildren()) {
                    chatterUsername = Objects.requireNonNull(ds.child("username").getValue()).toString();
                    chatterKarma = Objects.requireNonNull(ds.child("karma").getValue()).toString();
                    chatterHardship = Objects.requireNonNull(ds.child("hardship").getValue()).toString();
                    selectedUsersImage = Objects.requireNonNull(ds.child("image").getValue()).toString();
                    String typing = Objects.requireNonNull(ds.child("typingTo").getValue()).toString();

                    if (typing.equals(myuid)) {
                        onlineStatusTV.setText("Typing...");
                    } else {
                        String onlineStatus = Objects.requireNonNull(ds.child("onlineStatus").getValue()).toString();
                        if (onlineStatus.equals("online")) {
                            onlineStatusTV.setText(onlineStatus);
                        } else {

                            onlineStatusTV.setText("offline");
                        }
                    }

                    nameTV.setText(chatterUsername);

                    try {
                        Picasso.get().load(selectedUsersImage).placeholder(R.drawable.addphoto)
                                .transform(new CropCircleTransformation())
                                .into(profileIV);
                    } catch (Exception e) {
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;

                //get text from chat bar
                String message = messageET.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(mContext, "Cannot send Blank message", Toast.LENGTH_SHORT);
                } else {
                    sendMessage(message);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(imm).hideSoftInputFromWindow(messageET.getWindowToken(), 0);
                }
            }
        });

        //check edittext change listener

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.toString().trim().length() == 0) {
//                    checkTypingStatus("noOne");
//                } else {
//                    checkTypingStatus(selecteduid);
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    notify = true;

                    //get text from chat bar
                    String message = messageET.getText().toString().trim();

                    if (TextUtils.isEmpty(message)) {
                        Toast.makeText(mContext, "Cannot send Blank message", Toast.LENGTH_SHORT);
                    } else {
                        sendMessage(message);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).hideSoftInputFromWindow(messageET.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        //method for reading messages
        loadMessages();

        //method for ˜if meassage is seen
        seenMessages();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(chatList.size() == 0){
                    displayProfile();
                }
            }
        }, 1000);   //5 seconds
    }

    private void loadMessages() {

        //this listens for changes in the chat tree and loads the adapter
        chatList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(myuid).child("Chats");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String message = "" + ds.child("message").getValue();
                    String receiver = "" + ds.child("receiver").getValue();
                    String sender = "" + ds.child("sender").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    boolean isSeen = (boolean) ds.child("isSeen").getValue();

                    Chat_Data chat = new Chat_Data(message, receiver, sender, timestamp, isSeen);


                    if (chat.getReceiver().equals(myuid) && chat.getSender().equals(selecteduid) ||
                            chat.getReceiver().equals(selecteduid) && chat.getSender().equals(myuid)) {

                        addChatToEachUsersChatList("receiver");
                        chatList.add(chat);
                        adapterChat = new Chat_Adapter(mContext, chatList, selectedUsersImage);
                        adapterChat.notifyDataSetChanged();
                        //set adapter

                        linearLayoutManager.scrollToPosition(chatList.size() - 1);
                        linearLayoutManager.setStackFromEnd(true);
                        recycleView.setHasFixedSize(true);
                        recycleView.setLayoutManager(linearLayoutManager);
                        recycleView.setAdapter(adapterChat);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void sendMessage(final String message) {
        //sending message listern
        DatabaseReference databaseReferenceforSender = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
        DatabaseReference databaseReferenceforReceiver = FirebaseDatabase.getInstance().getReference("Users").child(selecteduid);

        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", myuid);
        hashMap.put("receiver", selecteduid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("isSeen", false);
        databaseReferenceforSender.child("Chats").push().setValue(hashMap);
        databaseReferenceforReceiver.child("Chats").push().setValue(hashMap);
        databaseReferenceforReceiver.child("karma").setValue(ServerValue.increment(20));
        databaseReferenceforSender.child("karma").setValue(ServerValue.increment(20));

        //resetting editView
        messageET.setText("");


        //TODO: Send Notifiaction logic
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String username = snapshot.child("username").getValue().toString();
//
                if (notify) {
                    sendNotification(selecteduid, username, message);

                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addChatToEachUsersChatList("sent");

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
        userRefForSeen.removeEventListener(seenListener);
        checkOnlineStatus(timeStamp);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnlineStatus("online");
    }

    private void checkOnlineStatus(String status) {
        //checking online status
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(myuid);

        HashMap<String, Object> hasmap = new HashMap<>();
        hasmap.put("onlineStatus", status);

        dRef.updateChildren(hasmap);
    }



    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Users").child(myuid).child("Chats");
        DatabaseReference userRefForSeen2 = FirebaseDatabase.getInstance().getReference("Users").child(selecteduid).child("Chats");
        //listener for seen
        seenListener = userRefForSeen2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String message = "" + ds.child("message").getValue();
                    String receiver = "" + ds.child("receiver").getValue();
                    String sender = "" + ds.child("sender").getValue();
                    String timestamp = "" + ds.child("timeStamp").getValue();
                    boolean isSeen = (boolean) ds.child("isSeen").getValue();

                    Chat_Data chat = new Chat_Data(message, receiver, sender, timestamp, isSeen);

                    if (chat.getReceiver().equals(myuid) && chat.getSender().equals(selecteduid)) {
                        HashMap<String, Object> hasSeenHash = new HashMap<>();

                        if(chat.getReceiver().equals(myuid)){
                            hasSeenHash.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHash);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void checkUserStatus() {
        user = mAuth.getCurrentUser();

        if (user != null) {
            //user signed in
            myuid = user.getUid();

        } else {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }

    private void addChatToEachUsersChatList(String sentOrReceived){

        if(sentOrReceived.equals("sent")){
            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(myuid).child("ChatList").child(selecteduid);

            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.equals(selecteduid)){
                        if (!snapshot.exists()) {
                            chatRef.child("id").setValue(selecteduid);
                            chatRef.child("blocked").setValue(false);
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(myuid);
                            userRef.child("karma").setValue(ServerValue.increment(50));

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else if (sentOrReceived.equals("receiver")){

            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(myuid).child("ChatList").child(selecteduid);

            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.equals(selecteduid)){
                        if (!snapshot.exists()) {
                            chatRef.child("id").setValue(selecteduid);
                            chatRef.child("blocked").setValue(false);

                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(myuid);
                            userRef.child("karma").setValue(ServerValue.increment(50));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendNotification(final String selecteduid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(selecteduid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myuid, name + ": " + message, "New Message", selecteduid, R.drawable.solace_logo);
                    Sender sender = new Sender(data, Objects.requireNonNull(token).getToken());

                    //from json object request
                    try {
                        JSONObject senderJson = new JSONObject(new Gson().toJson(sender));

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJson, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //response for request
                                Log.d("Json_response", "on response"+ response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Json_response", "on response"+ error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put parameters

                                Map<String,String> headers = new HashMap<>();

                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAZNRy-1Q:APA91bGYQhxr5xJcG8MppduE022ldP3WDygnHvQI7WiAS84Z_aoWlCPJnEIdMdHhhGrPjj5KIYtb9bBhBPJHQCdOsuvczNKRsW8_h-WBvnYAKl-L_t3GPUoUQtNY0mTaKmwPmQ14fsWT");
                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    View.OnClickListener toolBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//
            displayProfile();
        }
    };

    private void displayProfile(){
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        // Set the custom layout
        View displayProfileView = getLayoutInflater().inflate(R.layout.display_user_profile_layout, null);
        builder.setView(displayProfileView);

        // findViewById's
        ImageView profilePic = displayProfileView.findViewById(R.id.displayAvatar);
        TextView usernameTV = displayProfileView.findViewById(R.id.displayUsernameTV);
        TextView karmaTV = displayProfileView.findViewById(R.id.displayKarmaTV);
        TextView hardshipTV = displayProfileView.findViewById(R.id.displayHardshipTV);
        Button closeBtn = displayProfileView.findViewById(R.id.displayCloseBtn);

        Picasso.get()
                .load(selectedUsersImage)
                .transform(new CropCircleTransformation())
                .into(profilePic);

        usernameTV.setText(chatterUsername);

        int karma = 0;

        if(chatList.size() == 0){
            Log.d(TAG, "displayProfile: chat is 0");
        }else {
            karma = Integer.parseInt(chatterKarma);
        }


        int level = karma / 1000;
        String totalDisplay = "Karma Level - "+ level;
        karmaTV.setText(totalDisplay);

        hardshipTV.setText(chatterHardship);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // create and show the alert dialog
        dialog = builder.create();
        dialog.show();
    }
}
