package com.jerad_zac.solace.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.adapters.Chat_Adapter;
import com.jerad_zac.solace.adapters.Chatlist_Adapter;
import com.jerad_zac.solace.data_model.ChatList_Data;
import com.jerad_zac.solace.data_model.Chat_Data;
import com.jerad_zac.solace.data_model.User_Data;
import com.jerad_zac.solace.listeners.BlockListener;
import com.jerad_zac.solace.listeners.CrisisHotlineListener;
import com.jerad_zac.solace.listeners.SignOutListener;
import com.ramotion.fluidslider.FluidSlider;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static android.app.Activity.RESULT_OK;
import static com.jerad_zac.solace.fragments.Onboard2_Frag.handleSamplingAndRotationBitmap;

public class Profile_Frag extends Fragment {

    private static final String TAG = "Profile_Frag";
    SignOutListener mListener;
    CrisisHotlineListener mCrisisListener;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String uid;
    DatabaseReference myRef;
    FirebaseDatabase database;
    ArrayList<User_Data> userList;
    ArrayList<ChatList_Data> chatList_data;
    ArrayList<User_Data> blockedUsers;
    Chatlist_Adapter chatList_adapter;
    RecyclerView recyclerView;
    final String storagePath = "Users_Profile_Cover_Imgs/";
    private StorageReference storageReference;
    String userEmail;
    Uri image_uri;
    Bitmap profileBitmap;
    Bitmap bitmapToUpload;
    AlertDialog dialog;
    AlertDialog galleryOrCameraDialog;
    TextView karmalevel;
    Bitmap circleBitMap;
    String currentUsername;
    String currentEmail;
    String newUsername;
    String newEmail;
    View editProfileDialogBox;
    private static final int IMAGE_PICK_CAMERA__CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    ProgressBar karmaslider;
    private Spinner hardhsipSpinner;
    String snapShotImagePull;
    String newHardship;


    public static Profile_Frag newInstance(String uid) {

            Bundle args = new Bundle();
            args.putString(TAG, uid);
            Profile_Frag fragment = new Profile_Frag();
            fragment.setArguments(args);
            return fragment;
        }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //attaching signout listener
        if (context instanceof SignOutListener) {
            mListener = (SignOutListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

        if (context instanceof CrisisHotlineListener) {
            mCrisisListener = (CrisisHotlineListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
    }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.profile_layout, container, false);
        }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.user_recycleview_posts);
        TextView placeholderTV = getView().findViewById(R.id.placeholderTV);
        karmaslider = getView().findViewById(R.id.karma_bar);
        hardhsipSpinner = getView().findViewById(R.id.hardship_spinner);
        karmalevel = getView().findViewById(R.id.karma_level);
        mAuth = FirebaseAuth.getInstance();

        if (Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() != null) {
            currentEmail = mAuth.getCurrentUser().getEmail();
        }


        if (getView() != null && getContext() != null) {

            if (getArguments() != null) {
                uid = getArguments().getString(TAG);
                database = FirebaseDatabase.getInstance();
                getDataFromFirebase();
            }

            // Crisis Hotline button setup
            ImageButton crisisImgBtn = getView().findViewById(R.id.crisisHotlineImgBtn);
            crisisImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCrisisListener.toCrisisFrag();
                }
            });

            hardhsipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                  // karmaslider.setProgress(position * 40, true);
                    //Log.d(TAG, "slider change");

                    newHardship = parentView.getSelectedItem().toString();
                    updateUserInfo(202);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });


            myRef = FirebaseDatabase.getInstance().getReference("Users/" + uid).child("ChatList");
            chatList_data = new ArrayList<>();
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatList_data.clear();
                    Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                    for (DataSnapshot ds : contactChildren) {
                        ChatList_Data data = ds.getValue(ChatList_Data.class);

                        if (data.isBlocked()){
                            Log.d(TAG, "onDataChange: user is blocked "  + data.getId());
                        }else{
                            chatList_data.add(data);
                        }
                    }
                    if (chatList_data.isEmpty()) {
                        placeholderTV.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        placeholderTV.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        loadchats();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            final ImageView hamburgerMenuBtn = getView().findViewById(R.id.hamburgerMenuBtn);
            hamburgerMenuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(getContext(), hamburgerMenuBtn);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.profile_hamburger_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_profile:
                                    showEditProfileAlertDialog();
                                    break;
                                case R.id.sign_out:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    builder.setTitle("Are you sure you wish to sign out?");

                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            myRef = null;
                                            mListener.SignOutPressed();
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    builder.setCancelable(true);
                                    builder.create();
                                    builder.show();

                                    return true;
                                default:
                                    Log.d(TAG, "Profile Hamburger Menu - Error: This shouldn't be happening.");
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();

                }
            });

        }
    }

    public void showEditProfileAlertDialog() {
        if (getContext() != null) {
            // create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // set the custom layout
            editProfileDialogBox = getLayoutInflater().inflate(R.layout.edit_profile_layout, null);
            builder.setView(editProfileDialogBox);

            // When Profile ImageButton is clicked
            final ImageView editProfileIVBtn = editProfileDialogBox.findViewById(R.id.editAvatar);

            Picasso.get()
                    .load(snapShotImagePull)
                    .transform(new CropCircleTransformation())
                    .into(editProfileIVBtn);

            editProfileIVBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditPictureDialog();
                }
            });

            final EditText editUsernameET = editProfileDialogBox.findViewById(R.id.editUsernameET);
            editUsernameET.setHint(currentUsername);

            // When save button is clicked
            Button saveBtn = editProfileDialogBox.findViewById(R.id.save_button);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!editUsernameET.getText().toString().trim().equals("")) {
                        newUsername = editUsernameET.getText().toString();
                        updateUserInfo(101);
                    } else {
                       newUsername = currentUsername;
                        updateUserInfo(101);
                    }
                    dialog.dismiss();
                }
            });

            // create and show the alert dialog
            dialog = builder.create();
            dialog.show();
        }
    }

    private void showEditPictureDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Pick Image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //Camera Picked
                        if (!(ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA)
                                == (PackageManager.PERMISSION_GRANTED))) {
                            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA}, 1);

                        } else {
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

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        galleryOrCameraDialog = builder.create();
        galleryOrCameraDialog.show();
    }

    private void PickFromGallery() {
        //Pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void pickFromCamera() {
        //STEP #1: Get rotation degrees

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA__CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case IMAGE_PICK_CAMERA__CODE:

                    try {

                        profileBitmap = handleSamplingAndRotationBitmap(getContext(), image_uri);
                        bitmapToUpload = profileBitmap;
                        circleBitMap = getCircleBitmap(profileBitmap);

                        ImageView editAvatarIB = editProfileDialogBox.findViewById(R.id.editAvatar);
                        Bitmap scaleBp = Bitmap.createScaledBitmap(circleBitMap, editAvatarIB.getWidth(), editAvatarIB.getHeight(), true);
                        editAvatarIB.setImageBitmap(scaleBp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    break;
                case IMAGE_PICK_GALLERY_REQUEST_CODE:
                    assert data != null;

                    try {

                        profileBitmap = handleSamplingAndRotationBitmap(getContext(), data.getData());
                        bitmapToUpload = profileBitmap;
                        circleBitMap = getCircleBitmap(profileBitmap);
                        ImageView editAvatarIB = editProfileDialogBox.findViewById(R.id.editAvatar);
                        Bitmap scaleBp = Bitmap.createScaledBitmap(circleBitMap, editAvatarIB.getWidth(), editAvatarIB.getHeight(), true);
                        editAvatarIB.setImageBitmap(scaleBp);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Log.d(TAG, "shouldnt happen");
                    break;
            }
        }
    }

    private void updateUserInfo(int whichCall) {
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        userEmail = user.getEmail();

        if (whichCall == 101) {
            // Image change and possibly username change
            if (bitmapToUpload != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapToUpload.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Date currentTime = Calendar.getInstance().getTime();
                String filePathAndName = storagePath + currentTime.toString() + "_" + userEmail;
                StorageReference storageReference2 = storageReference.child(filePathAndName);

                storageReference2.putBytes(byteArray)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                Log.d(TAG, "onSuccess: Hello");
                                while (!uriTask.isSuccessful()) {
                                    Log.d(TAG, "uploading");
                                }

                                final Uri downloadUri = uriTask.getResult();

                                if (uriTask.isSuccessful()) {
                                    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                                    DatabaseReference ref = fdb.getReference("Users");
                                    Map<String, Object> updates = new HashMap<>();
                                    if (downloadUri != null) {
                                        updates.put("image", Objects.requireNonNull(downloadUri).toString());
                                    }
                                    if (newUsername != null) {
                                        updates.put("username", newUsername);
                                    }
                                    ref.child(uid).updateChildren(updates);
                                }
                            }
                        });
            } else {
                // Username Change but not Profile Picture
                if (newUsername != null) {
                FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                DatabaseReference ref = fdb.getReference("Users");
                Map<String, Object> updates = new HashMap<>();
                updates.put("username", newUsername);
                ref.child(uid).updateChildren(updates);
                }
            }
        }
        // Hardship change
        else if (whichCall == 202) {
            FirebaseDatabase fdb = FirebaseDatabase.getInstance();
            DatabaseReference ref = fdb.getReference("Users");
            Map<String, Object> updates = new HashMap<>();
            updates.put("hardship", newHardship);
            ref.child(uid).updateChildren(updates);
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        //    bitmap.recycle();

        return output;
    }

    private void loadchats() {
        userList = new ArrayList<>();
        myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d(TAG, "onDataChange: " + snapshot);
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                for (DataSnapshot ds : contactChildren) {

                    User_Data user_data = ds.getValue(User_Data.class);


                    for (ChatList_Data chatlList : chatList_data) {
                        if (user_data.getUid().equals(chatlList.getId())) {
                            if (!chatlList.isBlocked()) {
                                Log.d(TAG, "onDataChange: " + chatlList.isBlocked() + "  " + chatlList.getId());
                                userList.add(user_data);
                            } else {
                                // TODO: Logic here
                                Log.d(TAG, "onDataChange: block");
                                blockedUsers.add(user_data);
                            }
                            break;
                        }
                    }
                    BlockListener blockListener = new BlockListener() {
                        @Override
                        public void blockPressed() {
                            Log.d(TAG, "blockPressed: user blocked");
                        }
                    };
                    chatList_adapter = new Chatlist_Adapter(getContext(), uid, userList, blockListener);
                    // set adapter
                    recyclerView.setAdapter(chatList_adapter);
                    // get last message
                    for (int i = 0; i < userList.size(); i++) {
                         lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // This is called when brewery data is needed from firebase
    private void getDataFromFirebase() {
        //database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("Users");

        Query userQuery = myRef.orderByChild("uid").equalTo(uid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required if is recieved
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String image = Objects.requireNonNull(ds.child("image").getValue()).toString();
                    String hardship = Objects.requireNonNull(ds.child("hardship").getValue()).toString();
                    String username = Objects.requireNonNull(ds.child("username").getValue()).toString();
                    String karmaString =  Objects.requireNonNull(ds.child("karma").getValue()).toString();
                    int karma = Integer.parseInt(karmaString);
                    int level = karma / 1000;

                    currentUsername = username;
                    snapShotImagePull = image;
                    karmaslider.setProgress(karma - (1000 * level));
                    karmalevel.setText("Karma Level - "+ level);


                    if (getView() != null) {
                        TextView usernameTV = getView().findViewById(R.id.usernameTV);
                        Spinner hardshipSpin = getView().findViewById(R.id.hardship_spinner);
                        ImageView profileIV = getView().findViewById(R.id.avatar);


                        usernameTV.setText(username);
                        // Book Image button setup
                        Picasso.get()
                                .load(image)
                                .transform(new CropCircleTransformation())
                                .into(profileIV);

                        switch (hardship) {
                            case "Relationships":
                                hardshipSpin.setSelection(0);

                                break;
                            case "Family":
                                hardshipSpin.setSelection(1);

                                break;
                            case "Death":
                                hardshipSpin.setSelection(2);

                                break;
                            case "Job":
                                hardshipSpin.setSelection(3);

                                break;
                            case "Natural Disaster":
                                hardshipSpin.setSelection(4);

                                break;
                            default:
                                hardshipSpin.setSelection(5);
                                break;
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void lastMessage(final String userId) {
        if (myRef != null) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String message = "" + ds.child("message").getValue();
                    String receiver = "" + ds.child("receiver").getValue();
                    String sender = "" + ds.child("sender").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    boolean isSeen = (boolean) ds.child("isSeen").getValue();

                    Chat_Data chat = new Chat_Data(message, receiver, sender, timestamp, isSeen);
                    if (chat == null) {
                        continue;
                    } else {


                        if (sender == null || receiver == null) {
                            continue;
                        }

                        if (chat.getReceiver().equals(uid) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) &&
                                        chat.getSender().equals(uid)) {
                            theLastMessage = chat.getMessage();

                        }
                    }
                    chatList_adapter.setLastMessageMap(userId, theLastMessage);
                    chatList_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }
    }

    private void setUpKarmaSlider(){

        if (myRef != null) {
            myRef = FirebaseDatabase.getInstance().getReference("Users/" + uid);

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
}

