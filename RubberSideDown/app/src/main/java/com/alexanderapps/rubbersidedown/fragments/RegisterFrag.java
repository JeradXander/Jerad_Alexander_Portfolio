package com.alexanderapps.rubbersidedown.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alexanderapps.rubbersidedown.activities.LoginRegisterActivity;
import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.listeners.RegisterListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class RegisterFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    Button registerButton;
    EditText emailEdit;
    EditText passwordEdit;
    TextView goToLogin;
    Dialog dialog;
    FirebaseAuth mAuth;
    RegisterListener mListener;


    public static RegisterFrag newInstance() {

        Bundle args = new Bundle();

        RegisterFrag fragment = new RegisterFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_layout, container,false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if(context instanceof RegisterListener){
            mListener = (RegisterListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
          registerButton = Objects.requireNonNull(getView()).findViewById(R.id.register_button);
          emailEdit = getView().findViewById(R.id.email_field);
          passwordEdit = getView().findViewById(R.id.pass_feild);
          goToLogin = getView().findViewById(R.id.login_link);


          setProgress();

          registerButton.setOnClickListener(this);
          goToLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == registerButton.getId()){
            String email = emailEdit.getText().toString().trim();
            String pwd = passwordEdit.getText().toString();

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                emailEdit.setError("Invalid Email");
                emailEdit.setFocusable(true);
            }else if(passwordEdit.getText().length() < 6){
                passwordEdit.setError("password must be 6 chararcters or more");
                passwordEdit.setFocusable(true);
            }else {
                 dialog.show();
                RegisterEmailAndPassword(email,pwd);
            }

            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);

        }
        else if(v.getId() == goToLogin.getId()){
            startActivity(new Intent(getActivity(), LoginRegisterActivity.class));
            Objects.requireNonNull(getActivity()).finish();

        }
        else{
            Log.d(TAG, "shouldn't happen");
        }
    }

    private void RegisterEmailAndPassword(String email, String pass){
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(getContext(), "created user.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String useremail = Objects.requireNonNull(user).getEmail();
                            String uid = user.getUid();

                            HashMap<String, String> hashmap = new HashMap<>();
                            hashmap.put("email",useremail);
                            hashmap.put("uid",uid);
                            //TODO: add later
                            hashmap.put("name","");
                            hashmap.put("location","");
                            hashmap.put("onlineStatus","online");
                            hashmap.put("typingTo","noOne");
                            hashmap.put("motorcycle","");
                            hashmap.put("years","");
                            hashmap.put("likes","0");
                            hashmap.put("posts","0");
                            hashmap.put("image", "");
                            hashmap.put("cover", "");

                            SharedPreferences sp = Objects.requireNonNull(getActivity()).getSharedPreferences("SP_USER", MODE_PRIVATE);

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("Current_USERID",uid);
                            editor.putBoolean("FirstLogIN", true);

                            editor.apply();

                            FirebaseDatabase fdb = FirebaseDatabase.getInstance();
                            DatabaseReference ref = fdb.getReference("Users");
                            ref.child(uid).setValue(hashmap);



                            mListener.registerButtonSelected(user);
                            dialog.cancel();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).toString(),
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            dialog.cancel();
                        }

                    }
                });

    }

    //registration_progress bar builder method
    private void setProgress()
    {
        //setting up dialog box to hold progressbar and text view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.registration_progress);
        dialog = builder.create();
    }
}
