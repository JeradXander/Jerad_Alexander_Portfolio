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

import com.alexanderapps.rubbersidedown.dataModels.ModelUser;
import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.adapters.AdapterUsers;
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

public class UsersFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    ModelUser modelUser;
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> usersList;
    Button signoutBut;
    androidx.appcompat.widget.SearchView searchView;

    FirebaseAuth mAuth;
    SignOutListener mListener;
    ProfileSelectedListener profileSelectedListener;

    public static UsersFrag newInstance() {

        Bundle args = new Bundle();

        UsersFrag fragment = new UsersFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.users_layout, container,false);

        recyclerView = view.findViewById(R.id.users_recycleview);
        searchView = view.findViewById(R.id.user_search);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        usersList = new ArrayList<>();

        getAllUsers();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(query.trim())){
                    searchUsers(query);
                }else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user presses letter
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(newText.trim())){
                    searchUsers(newText);
                }else {
                    getAllUsers();
                }
                return false;
            }
        });

        return view;
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
        mAuth = FirebaseAuth.getInstance();

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

    private void searchUsers(final String query){

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String uid =  Objects.requireNonNull(ds.child("uid").getValue()).toString();
                    String name = Objects.requireNonNull(ds.child("name").getValue()).toString();
                    String email =  Objects.requireNonNull(ds.child("email").getValue()).toString();
                    String moto = Objects.requireNonNull(ds.child("motorcycle").getValue()).toString();
                    String search = "search";
                    String loc = Objects.requireNonNull(ds.child("location").getValue()).toString();
                    String likes =  Objects.requireNonNull(ds.child("likes").getValue()).toString();
                    String posts = Objects.requireNonNull(ds.child("posts").getValue()).toString();
                    String image =  Objects.requireNonNull(ds.child("image").getValue()).toString();
                    String cover = Objects.requireNonNull(ds.child("cover").getValue()).toString();
                    String years = Objects.requireNonNull(ds.child("years").getValue()).toString();
                    String onlineStatus = Objects.requireNonNull(ds.child("onlineStatus").getValue()).toString();
                    String typingTo = Objects.requireNonNull(ds.child("typingTo").getValue()).toString();

                    if (TextUtils.isEmpty(name)){
                         modelUser = new ModelUser(uid,email,email,moto,search,loc,likes,posts,
                                image,cover,years,onlineStatus,typingTo);
                    }else{
                         modelUser = new ModelUser(uid,name,email,moto,search,loc,likes,posts,
                                image,cover,years,onlineStatus,typingTo);
                    }

                    if(!modelUser.getUid().equals(Objects.requireNonNull(fUser).getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUser);
                        }
                    }

                    adapterUsers = new AdapterUsers(getActivity(), usersList,profileSelectedListener);

                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAllUsers(){
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                   String uid =  ""+ds.child("uid").getValue().toString();
                   String name = ""+ds.child("name").getValue().toString();
                   String email =  ""+ds.child("email").getValue().toString();
                   String moto = ""+ds.child("motorcycle").getValue().toString();
                   String search = "search";
                   String loc = ""+ ds.child("location").getValue().toString();
                   String likes =  ""+ds.child("likes").getValue().toString();
                   String posts = ""+ ds.child("posts").getValue().toString();
                   String image =  ""+ ds.child("image").getValue().toString();
                   String cover = "" + ds.child("cover").getValue().toString();
                   String years = "" + ds.child("years").getValue().toString();
                    String onlineStatus = ""+ ds.child("onlineStatus").getValue().toString();
                    String typingTo = "" + ds.child("typingTo").getValue().toString();

                    if (TextUtils.isEmpty(name)){
                        modelUser = new ModelUser(uid,email,email,moto,search,loc,likes,posts,
                                image,cover,years,onlineStatus,typingTo);
                    }else{
                        modelUser = new ModelUser(uid,name,email,moto,search,loc,likes,posts,
                                image,cover,years,onlineStatus,typingTo);
                    }

                    if(!modelUser.getUid().equals(Objects.requireNonNull(fUser).getUid())){
                        usersList.add(modelUser);
                    }

                    adapterUsers = new AdapterUsers(getActivity(), usersList,profileSelectedListener);

                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
