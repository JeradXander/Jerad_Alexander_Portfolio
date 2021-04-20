package com.alexanderapps.rubbersidedown.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.RegistrationActivity;
import com.alexanderapps.rubbersidedown.listeners.LoginListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class LoginFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 100;

    Button loginButton;

    EditText emailEdit;
    EditText passwordEdit;
    TextView goToRegister;
    TextView forgotpassword;
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser user;
    LoginListener mListener;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;

    public static LoginFrag newInstance() {

        Bundle args = new Bundle();

        LoginFrag fragment = new LoginFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loginpage_layout, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if (context instanceof LoginListener) {
            mListener = (LoginListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginButton = Objects.requireNonNull(getView()).findViewById(R.id.login_button);

        emailEdit = getView().findViewById(R.id.email_field);
        passwordEdit = getView().findViewById(R.id.pass_feild);
        goToRegister = getView().findViewById(R.id.register_link);
        forgotpassword = getView().findViewById(R.id.forgot_password);


        setProgress("login");

        loginButton.setOnClickListener(this);
        goToRegister.setOnClickListener(this);
        forgotpassword.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        // Configure sign-in to request the user's ID, email address, and basic
//        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
       gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestIdToken("28038942445-6l581fvs48ml7drfjqivoktm084ifua5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getContext()), gso);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.login_button:
                String email = emailEdit.getText().toString().trim();
                String pwd = passwordEdit.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    emailEdit.setError("Invalid Email");
                    emailEdit.setFocusable(true);
                } else if (passwordEdit.getText().length() < 6) {
                    passwordEdit.setError("password must be 6 chararcters or more");
                    passwordEdit.setFocusable(true);
                } else {
                    setProgress("login");
                    dialog.show();
                    loginUser(email, pwd);
                }

                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                break;
            case R.id.register_link:
                startActivity(new Intent(getActivity(), RegistrationActivity.class));
                Objects.requireNonNull(getActivity()).finish();
                break;
            case R.id.forgot_password:

                showpasswordDialog();
                break;
            default:
                Log.d(TAG,"shouldnt happen");
        }
    }

    private void loginUser(String email, String pass) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(getContext(), "Logged in user.",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            mListener.loginButtonSelected(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                });

    }

    private void googleloginFlow(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showpasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(getContext());
        final EditText emailEdit = new EditText(getContext());
        emailEdit.setHint("Email");
        emailEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEdit.setMinEms(14);

        linearLayout.addView(emailEdit);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailString = emailEdit.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {

                    emailEdit.setError("Invalid Email");
                    emailEdit.setFocusable(true);
                }else {
                    recoverEmailHelper(emailString);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void recoverEmailHelper(final String email){
        setProgress("recover");
        dialog.show();
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getContext(),"Email Sent to " + email, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(),"Failed to send to " + email, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //registration_progress bar builder method
    private void setProgress(String type) {
        if(type.equals("login")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.login_progress);
            dialog = builder.create();
        }else if(type.equals("recover")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(R.layout.recovering_progress);
            dialog = builder.create();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + Objects.requireNonNull(account).getId());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
}
