package com.alexanderapps.rubbersidedown.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.adapters.AdapterChat;
import com.alexanderapps.rubbersidedown.dataModels.ModelChat;
import com.alexanderapps.rubbersidedown.dataModels.ModelUser;
import com.alexanderapps.rubbersidedown.notifications.APIService;
import com.alexanderapps.rubbersidedown.notifications.Client;
import com.alexanderapps.rubbersidedown.notifications.Data;
import com.alexanderapps.rubbersidedown.notifications.Response;
import com.alexanderapps.rubbersidedown.notifications.Sender;
import com.alexanderapps.rubbersidedown.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    //variables
    Context mContext;
    APIService apiService;
    boolean notify = false;
    //views from xml
    Toolbar toolbar;
    RecyclerView recycleView;
    ImageView profileIV;
    TextView nameTV;
    TextView onlineStatusTV;
    EditText messageET;
    ImageButton sendButton;
    String selecteduid;
    String myuid;
    String selectedUsersImage;
    FirebaseUser user;

    //Firebase
    private FirebaseAuth mAuth;
    DatabaseReference userDatabaseReference;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    LinearLayoutManager linearLayoutManager;
    List<ModelChat> chatList;
    AdapterChat adapterChat;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mContext = this;
        //getting intent
        Intent intent = getIntent();
        selecteduid = intent.getStringExtra("uid");

        //initializing views and variables
        toolbar = findViewById(R.id.chat_tool);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        profileIV = findViewById(R.id.profileIV);
        nameTV = findViewById(R.id.chat_name);
        onlineStatusTV = findViewById(R.id.user_online_status);
        recycleView = findViewById(R.id.chat_recyclerView);
        messageET = findViewById(R.id.chat_message);
        sendButton = findViewById(R.id.chat_send);

        chatList = new ArrayList<>();

        //Layout for recycleView
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.scrollToPosition(chatList.size() - 1);
        linearLayoutManager.setStackFromEnd(true);

        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);


        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        //instance of firebase auth
        mAuth = FirebaseAuth.getInstance();
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
                    String name = Objects.requireNonNull(ds.child("name").getValue()).toString();
                    selectedUsersImage = Objects.requireNonNull(ds.child("image").getValue()).toString();

                    String typing = Objects.requireNonNull(ds.child("typingTo").getValue()).toString();

                    if (typing.equals(myuid)) {
                        onlineStatusTV.setText("Typing...");
                    } else {
                        String onlineStatus = Objects.requireNonNull(ds.child("onlineStatus").getValue()).toString();
                        if (onlineStatus.equals("online")) {
                            onlineStatusTV.setText(onlineStatus);
                        } else {
                            //convert timestamp
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy", cal).toString();
                            onlineStatusTV.setText("Last Seen " + dateTime);
                        }
                    }


                    nameTV.setText(name);


                    try {
                        Picasso.get().load(selectedUsersImage).placeholder(R.drawable.add_photo)
                                .transform(new CropCircleTransformation()).rotate(90)
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
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(selecteduid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //method for reading messages
        readMessages();
        //method for if meassage is seen
        seenMessages();

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
        checkTypingStatus("noOne");
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

    private void checkTypingStatus(String typing) {
        //checking typing status
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(myuid);

        HashMap<String, Object> hasmap = new HashMap<>();
        hasmap.put("typingTo", typing);

        dRef.updateChildren(hasmap);
    }

    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");

        //listener for seen
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String message = "" + ds.child("message").getValue();
                    String receiver = "" + ds.child("receiver").getValue();
                    String sender = "" + ds.child("sender").getValue();
                    String timestamp = "" + ds.child("timeStamp").getValue();
                    boolean isSeen = (boolean) ds.child("isSeen").getValue();

                    ModelChat chat = new ModelChat(message, receiver, sender, timestamp, isSeen);

                    if (chat.getReceiver().equals(myuid) && chat.getSender().equals(selecteduid)) {
                        HashMap<String, Object> hasSeenHash = new HashMap<>();

                        hasSeenHash.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHash);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //read messages method
    private void readMessages() {
        //this listens for changes in the chat tree and loads the adapter
        chatList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Chats");
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

                    ModelChat chat = new ModelChat(message, receiver, sender, timestamp, isSeen);


                    if (chat.getReceiver().equals(myuid) && chat.getSender().equals(selecteduid) ||
                            chat.getReceiver().equals(selecteduid) && chat.getSender().equals(myuid)) {
                        chatList.add(chat);
                        adapterChat = new AdapterChat(mContext, chatList, selectedUsersImage);
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", myuid);
        hashMap.put("receiver", selecteduid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //resetting editView
        messageET.setText("");

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myuid);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String uid = Objects.requireNonNull(snapshot.child("uid").getValue()).toString();
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                String moto = Objects.requireNonNull(snapshot.child("motorcycle").getValue()).toString();
                String search = "search";
                String loc = Objects.requireNonNull(snapshot.child("location").getValue()).toString();
                String likes = Objects.requireNonNull(snapshot.child("likes").getValue()).toString();
                String posts = Objects.requireNonNull(snapshot.child("posts").getValue()).toString();
                String image = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                String cover = Objects.requireNonNull(snapshot.child("cover").getValue()).toString();
                String years = Objects.requireNonNull(snapshot.child("years").getValue()).toString();
                String onlineStatus = Objects.requireNonNull(snapshot.child("onlineStatus").getValue()).toString();
                String typingTo = Objects.requireNonNull(snapshot.child("typingTo").getValue()).toString();


                ModelUser modelUser = new ModelUser(uid, name, email, moto, search, loc, likes, posts,
                        image, cover, years, onlineStatus, typingTo);

                if (notify) {
                    sendNotification(selecteduid, modelUser.getName(), message);

                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myuid).child(selecteduid);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(selecteduid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(selecteduid).child(myuid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("id").setValue(myuid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String selecteduid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(selecteduid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myuid, name + ":" + message, "New Message", selecteduid, R.drawable.icon);
                    Sender sender = new Sender(data, Objects.requireNonNull(token).getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                            if (response.code() == 200) {
                                if (response.body().success != 1) {  //   Toast.makeText(ChatActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
