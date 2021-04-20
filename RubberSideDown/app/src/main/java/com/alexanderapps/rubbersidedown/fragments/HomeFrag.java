package com.alexanderapps.rubbersidedown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.adapters.AdapterPosts;
import com.alexanderapps.rubbersidedown.dataModels.ModelPost;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    Button signoutBut;

    SignOutListener mListener;
    ProfileSelectedListener profileSelectedListener;

    androidx.appcompat.widget.SearchView searchView;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    public static HomeFrag newInstance() {

        Bundle args = new Bundle();

        HomeFrag fragment = new HomeFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.home_layout, container,false);

        searchView = view.findViewById(R.id.post_search);
        recyclerView = view.findViewById(R.id.post_recycle_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        postList = new ArrayList<>();

        loadPosts();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(query.trim())){
                    searchPosts(query);
                }else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user presses letter
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(newText.trim())){
                    searchPosts(newText);
                }else {
                    loadPosts();
                }
                return false;
            }
        });

        return view;
    }

    private void loadPosts() {

        //Loading posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
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
                        String pComments = Objects.requireNonNull(ds.child("pComments").getValue()).toString();

                        ModelPost modelPost = new ModelPost(pId,pTitle,pBody,pLikes,pComments,pImage,pTime,uid,uEmail,uDp,uName);
                        postList.add(modelPost);

                        adapterPosts = new AdapterPosts(getActivity(), postList, profileSelectedListener);
                        recyclerView.setAdapter(adapterPosts);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG,error.getMessage());
            }
        });
    }


    private void searchPosts(final String searchQuery){

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        databaseReference.addValueEventListener(new ValueEventListener() {
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
                    String pComments = Objects.requireNonNull(ds.child("pComments").getValue()).toString();


                    ModelPost modelPost = new ModelPost(pId,pTitle,pBody,pLikes,pComments,pImage,pTime,uid,uEmail,uDp,uName);


                        if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                modelPost.getpBody().toLowerCase().contains(searchQuery.toLowerCase())){
                            postList.add(modelPost);
                        }


                    adapterPosts = new AdapterPosts(getActivity(), postList, profileSelectedListener);

                    adapterPosts.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        signoutBut = Objects.requireNonNull(getView()).findViewById(R.id.signout);
        signoutBut.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == signoutBut.getId()){
            Toast.makeText(getContext(),"signout selected", Toast.LENGTH_SHORT).show();
            mListener.SignOutPressed();
        }else {
            Log.d(TAG, "shouldn't happen");
        }
    }


}
