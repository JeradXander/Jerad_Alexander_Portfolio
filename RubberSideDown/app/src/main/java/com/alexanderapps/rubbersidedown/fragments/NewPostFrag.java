package com.alexanderapps.rubbersidedown.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.MainActivity;
import com.alexanderapps.rubbersidedown.listeners.NewPostListener;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class NewPostFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    Button signoutBut;
    EditText titleET,bodyET;
    ImageView imageVW;
    Button uploadBT;
    Dialog progressDialog;
    //uri of picked image
    Uri image_uri = null;
    private static final int IMAGE_PICK_CAMERA__CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    private static final int REQUEST_ALL = 0x101;


    SignOutListener signOutListener;
    NewPostListener newPostListener;

    private FirebaseAuth mAuth;
    DatabaseReference userIdRef;
    String mUid;

    String name, email, postersAvatar;


    public static NewPostFrag newInstance() {

        Bundle args = new Bundle();

        NewPostFrag fragment = new NewPostFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_post_layout, container,false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if(context instanceof SignOutListener){
            signOutListener = (SignOutListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

        //attaching listeners
        if(context instanceof NewPostListener){
            newPostListener = (NewPostListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

          titleET = Objects.requireNonNull(getView()).findViewById(R.id.post_title);
        bodyET = getView().findViewById(R.id.post_body);
        imageVW = getView().findViewById(R.id.post_imageVW);
        signoutBut = getView().findViewById(R.id.signout);
        uploadBT = getView().findViewById(R.id.post_upload);

        //get set image from camery or gallery on click
        signoutBut.setOnClickListener(this);
        uploadBT.setOnClickListener(this);
        imageVW.setOnClickListener(this);
        setProgress();

        //get info to add to post
        userIdRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userIdRef.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    name = "" +ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    postersAvatar = ""+ ds.child("image").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == signoutBut.getId()){
            Toast.makeText(getContext(),"signout selected", Toast.LENGTH_SHORT).show();
            signOutListener.SignOutPressed();
        }else if(v.getId() == imageVW.getId()){
            showEditPictureDialog();


        }else if(v.getId() == uploadBT.getId()){
            String title = titleET.getText().toString().trim();
            String body = bodyET.getText().toString().trim();

            if(TextUtils.isEmpty(title) || TextUtils.isEmpty(body)|| image_uri == null){
                Toast.makeText(getContext(), "Please make sure your Post contains a Title, Description, and Image",Toast.LENGTH_SHORT).show();
            }
            else{

                progressDialog.show();
                uploadData(title,body,String.valueOf(image_uri));
            }
        } else {
            Log.d(TAG, "shouldn't happen");
        }
    }

    private void uploadData(final String title, final String body, String uri) {
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+ "post_"+ timeStamp;
        if(!uri.equals("noImage")){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is oploading
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();

                            if(uriTask.isSuccessful()){
                                //uri was received and now upload post to firebase

                                HashMap<Object,String> hashMap = new HashMap<>();

                                hashMap.put("uid", mUid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", postersAvatar);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pBody", body);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Post Uploaded",Toast.LENGTH_SHORT).show();
                                        newPostListener.newPostComplete();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Post Failed \n"+e.getMessage() ,Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed
                    progressDialog.dismiss();
                }
            });
        }else{

        }
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
                            requestPermissions();
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

                }
            }
        });


        builder.create().show();
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

                    imageVW.setImageURI(image_uri);
                    break;
                case IMAGE_PICK_GALLERY_REQUEST_CODE:
                    assert data != null;
                    image_uri = data.getData();
                    imageVW.setImageURI(image_uri);


                    break;
                default:
                    Log.d(TAG,"shouldnt happen");
                    break;
            }
        }
    }


    //registration_progress bar builder method
    private void setProgress() {
        if("posting".equals("login")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.login_progress);
            progressDialog = builder.create();
        }else if("posting".equals("recover")){
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

    private void requestPermissions() {

        if (!hasPermissions()[0] || !hasPermissions()[1] || !hasPermissions()[2]) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ALL);
        }
    }

    private boolean[] hasPermissions() {
        boolean result = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return new boolean[]{result, result1, result2};
    }

    private void checkUserStatus(){
        FirebaseUser fUser = mAuth.getCurrentUser();

        if(fUser != null){
            //user signed in
            mUid = fUser.getUid();
            email = fUser.getEmail();



        }
        else {
            startActivity(new Intent(getContext(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        }
    }
}
