package com.alexanderapps.rubbersidedown.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.activities.DashBoardActivity;
import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.MainActivity;
import com.alexanderapps.rubbersidedown.adapters.AdapterPosts;
import com.alexanderapps.rubbersidedown.dataModels.ModelPost;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    int totalLikes = 0;
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
    SignOutListener mListener;
    ProfileSelectedListener profileSelectedListener;
    androidx.appcompat.widget.SearchView searchView;

    //uri of picked image
    Uri image_uri;
    private static final int IMAGE_PICK_CAMERA__CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;

    //string for checking profile or cover photo
    String profileOrCoverPhoto;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dRef;
    private StorageReference storageReference;
    final String storagePath = "Users_Profile_Cover_Imgs/";
    FloatingActionButton editFAB;
    RecyclerView postRecyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    public static ProfileFrag newInstance() {

        Bundle args = new Bundle();

        ProfileFrag fragment = new ProfileFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_layout, container,false);
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
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        dRef = fdb.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();


        avatarVW = Objects.requireNonNull(getView()).findViewById((R.id.avatar));
        coverVW = getView().findViewById(R.id.cover_iv);
        editFAB = getView().findViewById(R.id.FabEdit);

        userNameTV = getView().findViewById(R.id.profile_name);
        locationTV = getView().findViewById(R.id.user_location);
        motorcyleTV = getView().findViewById(R.id.bike_value);
        likesTV = getView().findViewById(R.id.likes_value);
        postTV = getView().findViewById(R.id.posts_value);
        yearsExpTV = getView().findViewById(R.id.exp_value);
        postRecyclerView = getView().findViewById(R.id.user_recyclyview_posts);
        searchView = getView().findViewById(R.id.post_search);


        signoutBut = getView().findViewById(R.id.signout);

        //fab listener
        editFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hiding fab and getting form fragment
                showEditProfileDialog();
                editFAB.hide();
            }
        });
        signoutBut.setOnClickListener(this);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(query.trim())){
                    searchMyPosts(query);
                }else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user presses letter
                //happens when user presses search button from keyboard
                //if search is not empty
                if(!TextUtils.isEmpty(newText.trim())){
                    searchMyPosts(newText);
                }else {
                    loadMyPosts();
                }
                return false;
            }
        });

        checkUserStatus();
        loadProfileInfo();
        loadMyPosts();

        SharedPreferences sp = Objects.requireNonNull(getActivity()).getSharedPreferences("SP_USER", MODE_PRIVATE);

        if(sp.getBoolean("FirstLogIN",true)){

            welcomeDialog(sp);

        }


    }

    @SuppressLint("SetTextI18n")
    private void welcomeDialog(SharedPreferences sp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Welcome");

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final TextView textView = new TextView(getActivity());
        textView.setText(getString(R.string.welcome2) +
                getString(R.string.welcome));


        linearLayout.addView(textView);

        builder.setView(linearLayout);

        builder.setCancelable(true);

        builder.setNegativeButton("Continue",null);

        builder.create().show();

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("FirstLogIN",false);
        editor.apply();
    }

    private void loadMyPosts() {
        //Linear layout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout for recycleview
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                totalLikes = 0;
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
                likesTV.setText(String.valueOf(totalLikes));
                postTV.setText(String.valueOf(postList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void searchMyPosts(final String searchQuery) {
        //Linear layout for recycleview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout for recycleview
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
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
                    String pComments = Objects.requireNonNull(ds.child("pComments").getValue()).toString();

                    ModelPost modelPost = new ModelPost(pId,pTitle,pBody,pLikes,pComments,pImage,pTime,uid,uEmail,uDp,uName);

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

    public void showEditProfileDialog() {

        String[] options = {"Edit Profile Picture","Edit Cover Picture", "Edit Name", "Edit Motorcycle", "Edit Years of experience", "Edit Location"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //Edit Profile Pic
                        profileOrCoverPhoto = "image";
                        showEditPictureDialog();


                        break;
                    case 1:
                        //Edit Cover Pic
                        profileOrCoverPhoto = "cover";
                        showEditPictureDialog();
                        break;
                    case 2:
                        //Edit Name
                        showNameAndYearsUpdateDialog("name");
                        break;
                    case 3:
                        //Edit Motorcycle
                        motorcycleDIalog();
                        break;
                    case 4:
                        //Edit year
                        showNameAndYearsUpdateDialog("years");
                        break;
                    case 5:
                        //Edit Location
                        getLocation();

                        break;
                    default:
                        Log.d(TAG, "Shouldnt happen");
                        editFAB.show();
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                editFAB.show();
            }
        });
        builder.create().show();
    }

    private void showNameAndYearsUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText = new EditText(getActivity());
        if(key.equals("years")){
            editText.setHint("Enter " + key+ " of experience");
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else {
            editText.setHint("Enter " + key);

        }

        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    progressDialog.show();
                    HashMap<String,Object> results = new HashMap<>();

                    results.put(key, value);

                    dRef.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();

                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });


                    if(key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(Objects.requireNonNull(child)).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(Objects.requireNonNull(child)).hasChild("Comments")){
                                        String child1 = ""+ snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(Objects.requireNonNull(child)).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }else{
                    Toast.makeText(getActivity(),"Please enter a name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });



        builder.create().show();
        editFAB.show();
    }

    public void showEditPictureDialog(){
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Pick Image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //Camera Picked
                        if(!(ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA)
                                == (PackageManager.PERMISSION_GRANTED))){
                            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA},1);

                        }else {
                            pickFromCamera();
                        }
                        break;
                    case 1:
                        //Gallery picked
                        PickFromGallery();
                        break;
                    default:
                        Log.d(TAG, "Shouldnt happen");
                        editFAB.show();
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                editFAB.show();
            }
        });
        builder.create().show();
    }

    private void motorcycleDIalog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] options = {"Yamaha","Honda","Harley Davidson", "Kawasaki","Triumph","BMW","Harley Davidson","Aprilla",
        "KTM","MV Austa","Moto Guzzi","Royal Enfield","Indian Motorcycle","Benelli","Bajaj","Norton",
        "Victory","Bimota", "Hero"};
        builder.setTitle("Pick Motorcycle ");


        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final Spinner spinner = new Spinner(getActivity());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, options);
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        linearLayout.addView(spinner);

        builder.setView(linearLayout);

        builder.setNegativeButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
        editFAB.show();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                String value = options[position];

                progressDialog.show();
                HashMap<String,Object> results = new HashMap<>();

                results.put("motorcycle", value);

                dRef.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void PickFromGallery() {
        //Pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA__CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            switch (requestCode){
                case IMAGE_PICK_CAMERA__CODE:

                    uploadProfileCoverPhoto(image_uri);
                    break;
                case IMAGE_PICK_GALLERY_REQUEST_CODE:
                    assert data != null;
                    uploadProfileCoverPhoto(data.getData());
                    break;
                default:
                    Log.d(TAG,"shouldnt happen");
                    break;
            }
        }
    }

    private void getLocation() {

        String[] options = {"Yes","No"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Update Your Location with current City?");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        HashMap<String,Object> results = new HashMap<>();

                        results.put("location", DashBoardActivity.finalAddress);

                        dRef.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();

                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                        break;
                    case 1:
                        HashMap<String,Object> resultsNo = new HashMap<>();

                        resultsNo.put("location", "unknown");

                        dRef.child(user.getUid()).updateChildren(resultsNo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();

                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    dialog.dismiss();
                        break;
                    default:
                        Log.d(TAG, "Shouldnt happen");
                        editFAB.show();
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                editFAB.show();
            }
        });
        builder.create().show();
        editFAB.show();
    }

    private void uploadProfileCoverPhoto(Uri uri) {

        progressDialog.show();

        String filePathAndName = storagePath + "" + profileOrCoverPhoto+ "_" + user.getUid();
        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful()){
                            Log.d(TAG,"uploading");
                        }

                        final Uri downloadUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                HashMap<String, Object> results = new HashMap<>();
                                results.put(profileOrCoverPhoto, Objects.requireNonNull(downloadUri).toString());

                                dRef.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(),"Image Uploaded",Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });

                                if (profileOrCoverPhoto.equals("image")){
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                                    Query query = reference.orderByChild("uid").equalTo(uid);
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds:snapshot.getChildren()){
                                                String child = ds.getKey();
                                                snapshot.getRef().child(Objects.requireNonNull(child)).child("uDp").setValue(downloadUri.toString());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds: snapshot.getChildren()){
                                                String child = ds.getKey();
                                                if(snapshot.child(Objects.requireNonNull(child)).hasChild("Comments")){
                                                    String child1 = ""+ snapshot.child(child).getKey();
                                                    Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                    child2.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for(DataSnapshot ds: snapshot.getChildren()){
                                                                String child = ds.getKey();
                                                                snapshot.getRef().child(Objects.requireNonNull(child)).child("uDp").setValue(downloadUri.toString());
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(),"Some error occured",Toast.LENGTH_SHORT).show();
                            }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        editFAB.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                pickFromCamera();
            }
            else
            {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_SHORT).show();
                //checking sdk
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!showRationale) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Denied")
                                .setMessage("Please go to settings to allow us to use your location")


                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton("Change permission", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", Objects.requireNonNull(getActivity()).getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);

                                        handler.postDelayed(checkSettingOn, 1000);

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .show();

                    }
                    else {
                        Toast.makeText(getContext(), "you must have Camera permission to use this app.", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                    }
                }
            }
        }
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

    private void loadProfileInfo(){
        Query query = dRef.orderByChild("email").equalTo(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

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
                   // likesTV. setText(likes);
                  //  postTV.setText(post);
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

    private void checkUserStatus(){

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user signed in
            uid = user.getUid();

        }
        else {
            startActivity(new Intent(getContext(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        }
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

    final Handler handler = new Handler();
    final Runnable checkSettingOn = new Runnable() {

        @Override
        //@TargetApi(23)
        public void run() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

                return;
            }
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                return;
            }
            handler.postDelayed(this, 200);
        }
    };
}
