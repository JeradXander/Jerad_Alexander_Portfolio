package com.jerad_zac.solace.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.listeners.OnBoardListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static java.util.Objects.requireNonNull;

public class Onboard2_Frag extends Fragment implements View.OnClickListener {
    //TODO: JERAD/ Comment code
    private static final String TAG = "Login_Frag";
    private static final int RC_SIGN_IN = 100;
    //variables
    String userEmail;
    String userPassword;
    String hardship;
    String username = "";
    String fName = "";
    String lName = "";
    Bitmap profileBitmap;
    Bitmap bitmapToUpload;

    private static final String ARG_EMAIL = "ARG_EMAIL";
    private static final String ARG_PASSWORD = "ARG_PASSWORD";
    OnBoardListener mListener;
    EditText usernameField;
    EditText firstNameField;
    EditText lastNameField;
    TextView goToLogin;
    Button registerButton;
    ImageButton profileButton;
    Spinner hardhsipSpinner;
    //uri of picked image
    Uri image_uri;
    private static final int IMAGE_PICK_CAMERA__CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    final String storagePath = "Users_Profile_Cover_Imgs/";
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String uid;
    private DatabaseReference dRef;
    private StorageReference storageReference;
    android.graphics.Camera mCamera;

    public static Onboard2_Frag newInstance(String email, String password) {

        Bundle args = new Bundle();

        if (email != null && password != null) {
            args.putString(ARG_EMAIL, email);
            args.putString(ARG_PASSWORD, password);
        }

        Onboard2_Frag fragment = new Onboard2_Frag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //setting window to show fields when keyboard is up
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return inflater.inflate(R.layout.onboard_2_layout, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //attaching listeners
        if (context instanceof OnBoardListener) {
            mListener = (OnBoardListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setProgress();


        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userEmail = Objects.requireNonNull(getArguments()).getString(ARG_EMAIL);
        userPassword = Objects.requireNonNull(getArguments()).getString(ARG_PASSWORD);

        boolean cameraperissiondenied = getActivity().shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        boolean storageperissiondenied = getActivity().shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (cameraperissiondenied || storageperissiondenied) {
            alertDialogForMissingPermission();
        } else {
            //creating feilds
            usernameField = Objects.requireNonNull(getView()).findViewById(R.id.username_field);
            firstNameField = Objects.requireNonNull(getView()).findViewById(R.id.firstname_feild);
            lastNameField = Objects.requireNonNull(getView()).findViewById(R.id.lastname_feild);
            goToLogin = Objects.requireNonNull(getView()).findViewById(R.id.login_link);
            registerButton = Objects.requireNonNull(getView()).findViewById(R.id.register_button);
            profileButton = Objects.requireNonNull(getView()).findViewById(R.id.add_imagebuton);
            hardhsipSpinner = Objects.requireNonNull(getView()).findViewById(R.id.hard_spinner);

            goToLogin.setOnClickListener(this);
            registerButton.setOnClickListener(this);
            profileButton.setOnClickListener(this);
            profileButton.setOnClickListener(this);

            hardship = hardhsipSpinner.getSelectedItem().toString();
            Log.d(TAG, hardship);

            hardhsipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    hardship = parentView.getItemAtPosition(position).toString();
                    Log.d(TAG, hardship);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

        }
    }

    @Override
    public void onClick(View v) {
        //switch for button clicked
        switch (v.getId()) {
            case R.id.add_imagebuton:
                showEditPictureDialog();

                break;
            case R.id.login_link:
                mListener.loginSelected();
                break;
            case R.id.register_button:
                username = usernameField.getText().toString().trim();
                fName = firstNameField.getText().toString();
                lName = lastNameField.getText().toString();

                if ((username.length() < 21 && username.length() <= 2)) {

                    usernameField.setError("Must be between 3-20 characters");
                    usernameField.setFocusable(true);
                } else if (fName.equals("")) {
                    firstNameField.setError("Name cannot be empty");
                    firstNameField.setFocusable(true);

                } else if (lName.equals("")) {
                    lastNameField.setError("Name cannot be empty");
                    lastNameField.setFocusable(true);
                } else if (profileBitmap == null) {
                    Toast.makeText(getContext(), "In order to register you must pick a profile photo at the top left", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: JERAD/ Work on progress bar
                    //    setProgress("login");
//                   dialog.show();

                    createAndLoginNewUser(userEmail, userPassword, username, fName, lName, hardship, true, bitmapToUpload);
                }

                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                break;
            default:
                Log.d(TAG, "shouldnt happen");

        }
    }


    public void showEditPictureDialog() {
        String[] options = {"Camera", "Gallery"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

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
        builder.create().show();
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
                        Bitmap circleBitMap = getCircleBitmap(profileBitmap);

                        Bitmap scaleBp = Bitmap.createScaledBitmap(circleBitMap, profileButton.getWidth(), profileButton.getHeight(), true);
                        profileButton.setImageBitmap(scaleBp);
                        //        uploadProfileCoverPhoto(image_uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    break;
                case IMAGE_PICK_GALLERY_REQUEST_CODE:
                    assert data != null;

                    try {

                        profileBitmap = handleSamplingAndRotationBitmap(getContext(), data.getData());
                        bitmapToUpload = profileBitmap;
                        Bitmap circleBitMap = getCircleBitmap(profileBitmap);
                        Bitmap scaleBp = Bitmap.createScaledBitmap(circleBitMap, profileButton.getWidth(), profileButton.getHeight(), true);
                        profileButton.setImageBitmap(scaleBp);
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

    //registration_progress bar builder method
    private void setProgress()
    {

        //setting up dialog box to hold progressbar and text view
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setView(R.layout.listener_loading);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

    }


    private void createAndLoginNewUser(final String _email, String _userPassword, final String _userName, final String _fName, final String _lName, final String _hardship, final boolean _isAvailable, final Bitmap _profileBitmap) {
       dialog.show();
        TextView label = dialog.findViewById(R.id.labelIV);
        label.setText("Registering User");
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(_email, _userPassword)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(getContext(), "created user.",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            uid = user.getUid();

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            _profileBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            //   _profileBitmap.recycle();

                            Date currentTime = Calendar.getInstance().getTime();
                            String filePathAndName = storagePath + currentTime.toString() + "_" + userEmail;
                            StorageReference storageReference2 = storageReference.child(filePathAndName);

                            storageReference2.putBytes(byteArray)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                                            while (!uriTask.isSuccessful()) {
                                                Log.d(TAG, "uploading");
                                            }

                                            final Uri downloadUri = uriTask.getResult();

                                            if (uriTask.isSuccessful()) {
                                                HashMap<String, Object> results = new HashMap<>();
                                                results.put("profile", Objects.requireNonNull(downloadUri).toString());


                                                HashMap<String, Object> hashmap = new HashMap<>();
                                                hashmap.put("email", _email);
                                                hashmap.put("uid", uid);
                                                hashmap.put("username", _userName);
                                                hashmap.put("first_name", _fName);
                                                hashmap.put("lastName", _lName);
                                                hashmap.put("onlineStatus", "online");
                                                hashmap.put("typingTo", "noOne");
                                                hashmap.put("hardship", _hardship);
                                                hashmap.put("is_available", _isAvailable);
                                                hashmap.put("karma", 0);
                                                hashmap.put("image", Objects.requireNonNull(downloadUri).toString());

                                                FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                                                DatabaseReference ref = fdb.getReference("Users");
                                                ref.child(uid).setValue(hashmap);

                                                mListener.registerComplete();

                                                dialog.dismiss();

                                            } else {
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // progressDialog.dismiss();
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).toString(),
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            //  dialog.cancel();
                        }

                    }
                });


    }


    private void alertDialogForMissingPermission() {
        new AlertDialog.Builder(getContext())
                .setTitle("Missing Permission")
                .setMessage("Please Allow Denied Permissions in the settings")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent1 = new Intent();
                        intent1 = intent1.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent1.setData(uri);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent1);
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        //    img.recycle();
        return rotatedImg;
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

}

