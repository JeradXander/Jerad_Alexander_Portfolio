package com.alexanderapps.rubbersidedown.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.adapters.AdapterComments;
import com.alexanderapps.rubbersidedown.dataModels.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static android.content.ContentValues.TAG;

public class PostDetailActivity extends AppCompatActivity {

    String mUid,mEmail,mName, mAvatar,postId,pLikes,theirAvatar, theirName, pImage;
    ImageView uPictureIV, pImageIv;
    TextView nameTV,pTimeTV,pTitleTV,pBodyTC,pLikeTV,pCommentTV;
    ImageView moreButton;
    Button likeBT,shareBT;
    LinearLayout profileLayout;
    Dialog progressDialog;

    boolean mProcessComment = false;
    boolean mProcessLike= false;

    EditText commentET;
    ImageButton sendBT;
    ImageView cAvatIV;
    RecyclerView recyclerView;
    List<ModelComment> commentList;
    AdapterComments adapterComments;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        setProgress();
        checkUserStatus();
        //get id of post using extra
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        //init comments list
        commentList = new ArrayList<>();

        uPictureIV = findViewById(R.id.post_avatar);
        pImageIv = findViewById(R.id.post_pImageIV);
        nameTV = findViewById(R.id.uNameTV);
        pTimeTV = findViewById(R.id.pTimeTV);
        pTitleTV = findViewById(R.id.pTitleTV);
        pCommentTV = findViewById(R.id.pCommentTV);
        pBodyTC = findViewById(R.id.pBodyTV);
        pLikeTV = findViewById(R.id.pLikesTV);
        moreButton = findViewById(R.id.post_more);
        likeBT = findViewById(R.id.pLikeBT);
        shareBT = findViewById(R.id.pShareBT);
        profileLayout = findViewById(R.id.profile_layout);
        recyclerView = findViewById(R.id.comment_recycle_view);
        //layout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recycleview
        recyclerView.setLayoutManager(layoutManager);
        adapterComments = new AdapterComments(getApplicationContext(), commentList);
        recyclerView.setAdapter(adapterComments);


        commentET = findViewById(R.id.cCommentEt);
        sendBT = findViewById(R.id.comment_send);
        cAvatIV = findViewById(R.id.cAvatarIV);

        loadPostInfo();
        loadUserInfo();
        setLikes();
        loadComments();

        sendBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(commentET.getText().toString().trim());
            }
        });

        //handle like

        likeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });


            moreButton.setVisibility(View.GONE);

    }

    private void loadComments() {

        //path of the post to get the comments

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postId).child("Comment");
       ref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               commentList.clear();
               for (DataSnapshot ds: snapshot.getChildren()){

                   String cid = ""+ds.child("cId").getValue();
                   String comment = ""+ds.child("comment").getValue();
                   String pTimestamp = ""+ds.child("timeStamp").getValue();
                   String uEmail = ""+ds.child("uEmail").getValue();
                   String uName = ""+ds.child("uName").getValue();
                   String uid = ""+ds.child("uid").getValue();
                   String uDp = ""+ds.child("uDp").getValue();

                   ModelComment modelComment = new ModelComment(cid,comment,pTimestamp,uid,uEmail,uDp,uName);
                   commentList.add(modelComment);
                   adapterComments = new AdapterComments(getApplicationContext(), commentList);
                   recyclerView.setAdapter(adapterComments);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });



    }

    private void setLikes() {

        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(mUid)){
                    //user has Liked this post
                    likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_24,0,0,0);
                    likeBT.setText("Liked");
                }
                else{
                    //user has not Liked this post
                    likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumbs,0,0,0);
                    likeBT.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        mProcessLike = true;

        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(mUid)){
                        //ALREADYLIKED SO REMOVE LIKE
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes) -1));
                        likeRef.child(postId).child(mUid).removeValue();
                        mProcessLike =false;

                        likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumbs,0,0,0);
                        likeBT.setText("Like");
                    }
                    else{
                        //not liked so like it
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes) -1));
                        likeRef.child(postId).child(mUid).setValue("Liked");
                        mProcessLike = false;
                        likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_24,0,0,0);
                        likeBT.setText("Liked");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment(String comment){
        progressDialog.show();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this,"Please enter a comment to send",Toast.LENGTH_SHORT).show();
            return;
        }
        //each post will have a child comments that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comment");
        String timeStamp =  String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("uid",mUid);
        hashMap.put("uEmail",mEmail);
        hashMap.put("uDp",mAvatar);
        hashMap.put("uName",mName);

        //put this data in db
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //added
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Added",Toast.LENGTH_SHORT).show();
                commentET.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).hideSoftInputFromWindow(commentET.getWindowToken(), 0);
                updateCommentCount();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Add Failed",Toast.LENGTH_SHORT).show();


            }
        });

    }

    //updating comment count
    private void updateCommentCount() {

        mProcessComment = true;
       final DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Posts").child(postId);
       ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(mProcessComment){
                   String comments = ""+ snapshot.child("pComments").getValue();

                   int newCommentBal = Integer.parseInt(comments)+ 1;
                   ref.child("pComments").setValue(""+ newCommentBal);
                   mProcessComment = false;
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }

    //loading user information
    private void loadUserInfo() {
        //listener for user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    mName = ""+ds.child("name").getValue();
                    mAvatar = ""+ds.child("image").getValue();

                    try{
                        Picasso.get().load(mAvatar).placeholder(R.drawable.add_photo)
                                .resize(500, 500)
                                .transform(new CropCircleTransformation()).rotate(90)
                                .into(cAvatIV);
                    }catch (Exception e){
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //reference for posts and loading posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pBody = ""+ds.child("pBody").getValue();
                    String pTimestamp = ""+ds.child("pTime").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pImage = ""+ds.child("pImage").getValue();
                    theirAvatar = ""+ds.child("uDp").getValue();
                    theirName = ""+ds.child("uName").getValue();
                    String uid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    String commentsCount = ""+ds.child("pComments").getValue();

                    //Convert timestamp
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                    String pTime = DateFormat.format("dd/MM/yyyy",calendar).toString();

                    //set data
                    pTitleTV.setText(pTitle);
                    pBodyTC.setText(pBody);
                    pLikeTV.setText(pLikes + " Likes");
                    pTimeTV.setText(pTime);
                    nameTV.setText(theirName);
                    pCommentTV.setText(commentsCount+ " Comments");


                    try{
                        Picasso.get().load(pImage).placeholder(R.drawable.add_photo)
                                .resize(500, 500).rotate(90)
                                .into(pImageIv);
                    }catch (Exception e){
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }

                    try{
                        Picasso.get().load(theirAvatar).placeholder(R.drawable.add_photo)
                                .resize(500, 500)
                                .transform(new CropCircleTransformation()).rotate(90)
                                .into(uPictureIV);
                    }catch (Exception e){
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            mEmail = user.getEmail();
            mUid = user.getUid();
        }
        else{
            startActivity(new Intent(this,MainActivity.class));
        }
    }

    //registration_progress bar builder method
    private void setProgress() {
        if("".equals("login")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.login_progress);
            progressDialog = builder.create();
        }else if("".equals("recover")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.recovering_progress);
            progressDialog = builder.create();
        }else {
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.updating_progress);
            progressDialog = builder.create();
        }
    }
}
