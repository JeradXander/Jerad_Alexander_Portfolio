package com.jerad_zac.solace.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.listeners.LoginListener;
import com.jerad_zac.solace.listeners.OnBoardListener;
import com.jerad_zac.solace.listeners.RegisterListener;

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Login_Frag extends Fragment  implements View.OnClickListener {
    //TODO: JERAD/ Comment code
    private static final String TAG = "Login_Frag";
    private static final int RC_SIGN_IN = 100;
    //variables
    LoginListener loginListener;
    RegisterListener registerListener;
    EditText emailEdit;
    EditText passwordEdit;
    TextView goToRegister;
    TextView forgotpassword;
    Button loginButton;
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser user;

        public static Login_Frag newInstance() {

            Bundle args = new Bundle();

            Login_Frag fragment = new Login_Frag();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            //setting window to show fields when keyboard is up
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            return inflater.inflate(R.layout.loginpage_layout, container, false);
        }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //attaching listeners
        if (context instanceof LoginListener) {
            loginListener = (LoginListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
        //attaching listeners
        if (context instanceof RegisterListener) {
            registerListener = (RegisterListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setProgress();

        //creating feilds
        loginButton = Objects.requireNonNull(getView()).findViewById(R.id.login_button);
        emailEdit = getView().findViewById(R.id.email_field);
        passwordEdit = getView().findViewById(R.id.pass_feild);
        goToRegister = getView().findViewById(R.id.register_link);
        forgotpassword = getView().findViewById(R.id.forgot_password);

        //setting click listeners
        loginButton.setOnClickListener(this);
        goToRegister.setOnClickListener(this);
        forgotpassword.setOnClickListener(this);
    }

    //registration_progress bar builder method
    private void setProgress()
    {

        //setting up dialog box to hold progressbar and text view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setView(R.layout.listener_loading);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
    }

    @Override
    public void onClick(View v) {
            //switch for button clicked
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
                    //TODO: JERAD/ Work on progress bar
                //    setProgress("login");
                    dialog.show();
                    TextView label = dialog.findViewById(R.id.labelIV);
                    label.setText("Loggin in");
                    loginUser(email, pwd);
                }

                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                break;
            case R.id.register_link:
                registerListener.registerButtonSelected();
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
                            loginListener.loginButtonSelected(user);
                            dialog.dismiss();

                        } else {
                            // If sign in fails, display a message to the user.
                            dialog.dismiss();
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).toString(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

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
//                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void recoverEmailHelper(final String email){
//       // setProgress("recover");
//        dialog.show();
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                dialog.dismiss();
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
//                dialog.dismiss();
                Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

