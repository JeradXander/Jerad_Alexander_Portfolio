package com.alexanderapps.rubbersidedown.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.ChatActivity;
import com.alexanderapps.rubbersidedown.adapters.AdapterPosts;
import com.alexanderapps.rubbersidedown.dataModels.ModelPost;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class TheirProfileFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    int totalLikes = 0;
    int totalPosts = 0;
    Button signoutBut;
    TextView useremailTV;
    TextView userNameTV;
    TextView locationTV;
    TextView motorcyleTV;
    TextView yearsExpTV;
    TextView likesTV;
    TextView postTV;
    ImageView avatarVW, coverVW;
    Dialog progressDialog;
    FloatingActionButton floatingActionButton;

    RecyclerView postRecyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    SignOutListener mListener;
    ProfileSelectedListener profileSelectedListener;
    androidx.appcompat.widget.SearchView searchView;


    //uri of picked image
    Uri image_uri;
    String theirUID;
    private static final int IMAGE_PICK_CAMERA__CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;

    //string for checking profile or cover photo
    String profileOrCoverPhoto;

    private DatabaseReference dRef;

    private static final String ARG_THEIRUID = "ARG_THEIRUID";

    public static TheirProfileFrag newInstance(String uid) {

        Bundle args = new Bundle();

        if(uid != null){
            args.putString(ARG_THEIRUID, uid);
        }

        TheirProfileFrag fragment = new TheirProfileFrag();
        fragment.setArguments(args);
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.their_profile, container,false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if(context instanceof SignOutListener){
            mListener = (SignOutListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
        //attaching listeners
        if(context instanceof ProfileSelectedListener){
            profileSelectedListener = (ProfileSelectedListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setProgress();

        postList = new ArrayList<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        dRef = fdb.getReference("Users");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        theirUID = Objects.requireNonNull(getArguments()).getString(ARG_THEIRUID);
        avatarVW = Objects.requireNonNull(getView()).findViewById((R.id.avatar));
        coverVW = getView().findViewById(R.id.cover_iv);
        floatingActionButton = getView().findViewById(R.id.fabMessage);

        userNameTV = getView().findViewById(R.id.profile_name);
        locationTV = getView().findViewById(R.id.user_location);
        motorcyleTV = getView().findViewById(R.id.bike_value);
        likesTV = getView().findViewById(R.id.likes_value);
        postTV = getView().findViewById(R.id.posts_value);
        yearsExpTV = getView().findViewById(R.id.exp_value);
        postRecyclerView = getView().findViewById(R.id.their_recycleview_Post);
        searchView = getView().findViewById(R.id.post_search);

        signoutBut = getView().findViewById(R.id.signout);

        signoutBut.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);

        loadProfileInfo();

        loadTheirPosts();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(query.trim())){
                    searchTheirPosts(query);
                }else {

                    searchTheirPosts(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user presses letter
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(newText.trim())){
                    searchTheirPosts(newText);
                }else {
                    loadTheirPosts();
                }
                return false;
            }
        });

    }

    private void searchTheirPosts(final String searchQuery) {
        //Linear layout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout for recycleview
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        final Query query = ref.orderByChild("uid").equalTo(theirUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String pBody =  Objects.requireNonNull(ds.child("pBody").getValue()).toString();
                    String pId = Objects.requireNonNull(ds.child("pId").getValue()).toString();
                    String pImage =  Objects.requireNonNull(ds.child("pImage").getValue()).toString();
                    String pTime = Objects.requireNonNull(ds.child("pTime").getValue()).toString();
                    String pTitle = Objects.requireNonNull(ds.child("pTitle").getValue()).toString();
                    String uDp =  Objects.requireNonNull(ds.child("uDp").getValue()).toString();
                    String uEmail = Objects.requireNonNull(ds.child("uEmail").getValue()).toString();
                    String uName =  Objects.requireNonNull(ds.child("uName").getValue()).toString();
                    String uid = Objects.requireNonNull(ds.child("uid").getValue()).toString();
                    String pLikes = Objects.requireNonNull(ds.child("pLikes").getValue()).toString();
                    String pCommetnts = Objects.requireNonNull(ds.child("pComments").getValue()).toString();

                    ModelPost modelPost = new ModelPost(pId,pTitle,pBody,pLikes,pCommetnts,pImage,pTime,uid,uEmail,uDp,uName);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpBody().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }
                    adapterPosts = new AdapterPosts(getActivity(), postList, profileSelectedListener);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadTheirPosts() {
        //Linear layout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout for recycleview
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(theirUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                totalLikes = 0;
                totalPosts = 0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    String pBody =  Objects.requireNonNull(ds.child("pBody").getValue()).toString();
                    String pId = Objects.requireNonNull(ds.child("pId").getValue()).toString();
                    String pImage =  Objects.requireNonNull(ds.child("pImage").getValue()).toString();
                    String pTime = Objects.requireNonNull(ds.child("pTime").getValue()).toString();
                    String pTitle = Objects.requireNonNull(ds.child("pTitle").getValue()).toString();
                    String uDp =  Objects.requireNonNull(ds.child("uDp").getValue()).toString();
                    String uEmail = Objects.requireNonNull(ds.child("uEmail").getValue()).toString();
                    String uName =  Objects.requireNonNull(ds.child("uName").getValue()).toString();
                    String uid = Objects.requireNonNull(ds.child("uid").getValue()).toString();
                    String pLikes = Objects.requireNonNull(ds.child("pLikes").getValue()).toString();
                    String pComments = Objects.requireNonNull(ds.child("pComments").getValue()).toString();

                    totalLikes += Integer.parseInt(pLikes);


                    ModelPost modelPost = new ModelPost(pId,pTitle,pBody,pLikes,pComments,pImage,pTime,uid,uEmail,uDp,uName);
                    postList.add(modelPost);

                    adapterPosts = new AdapterPosts(getActivity(), postList, profileSelectedListener);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likesTV.setText(String.valueOf(totalLikes));
        postTV.setText(String.valueOf(postList.size()));
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == signoutBut.getId()){
            Toast.makeText(getContext(),"signout selected", Toast.LENGTH_SHORT).show();
            mListener.SignOutPressed();
        }else if(v.getId() == floatingActionButton.getId()){
            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
            chatIntent.putExtra("uid", theirUID);
            Objects.requireNonNull(getContext()).startActivity(chatIntent);
        } else {
            Log.d(TAG, "shouldn't happen");
        }
    }

    private void loadProfileInfo(){
        Query query = dRef.orderByChild("uid").equalTo(theirUID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String location = "" + ds.child("location").getValue();
                    String motorcycle = "" + ds.child("motorcycle").getValue();
                    String years = "" + ds.child("years").getValue();
                    String likes = "" + ds.child("likes").getValue();
                    String post = "" + ds.child("posts").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();


                    userNameTV.setText(name);
                    locationTV.setText(location);
                    motorcyleTV.setText(motorcycle);
                    likesTV. setText(likes);
                    postTV.setText(post);
                    yearsExpTV.setText(years);

                    try{

                        Picasso.get().load(image).fit()
                                .transform(new CropCircleTransformation()).rotate(90)
                                .into(avatarVW);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.add_photo).into(avatarVW);
                    }

                    try{

                        Picasso.get().load(cover).fit().rotate(90)
                                .into(coverVW);

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //registration_progress bar builder method
    private void setProgress() {
        if("".equals("login")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.login_progress);
            progressDialog = builder.create();
        }else if("".equals("recover")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.recovering_progress);
            progressDialog = builder.create();
        }else {
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.updating_progress);
            progressDialog = builder.create();
        }
    }

}
