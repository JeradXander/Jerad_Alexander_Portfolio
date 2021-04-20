package com.jerad_zac.solace.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Onboard1_Frag extends Fragment  implements View.OnClickListener {
//TODO: JERAD/ Comment code
    private static final String TAG = "Onboard1_Frag";
    private static final int RC_SIGN_IN = 100;
    //variables
    OnBoardListener mListener;
    EditText emailEdit;
    EditText passwordEdit;
    EditText passwordConfirm;
    TextView goToLogin;
    Button continueButton;
    Dialog dialog;

        public static Onboard1_Frag newInstance() {

            Bundle args = new Bundle();

            Onboard1_Frag fragment = new Onboard1_Frag();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            //setting window to show fields when keyboard is up
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            return inflater.inflate(R.layout.onboard_1_layout, container, false);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //creating feilds
        continueButton = Objects.requireNonNull(getView()).findViewById(R.id.continue_button);
        emailEdit = getView().findViewById(R.id.email_field);
        passwordEdit = getView().findViewById(R.id.pass_feild);
        passwordConfirm = getView().findViewById(R.id.confirm_pass_feild);
        goToLogin = getView().findViewById(R.id.login_link);

        //setting click listeners
        continueButton.setOnClickListener(this);
        goToLogin.setOnClickListener(this);
        passwordEdit.setOnClickListener(this);
        passwordConfirm.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
            //switch for button clicked
        switch (v.getId()){
            case R.id.continue_button:
                passwordEdit.clearFocus();
                passwordConfirm.clearFocus();
                String email = emailEdit.getText().toString().trim();
                String pwd = passwordEdit.getText().toString();
                String cPwd = passwordConfirm.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    emailEdit.setError("Invalid Email");
                    emailEdit.setFocusable(true);
                } else if (pwd.length() < 6) {
                    passwordEdit.setError("password must be 6 chararcters or more");
                    passwordEdit.setFocusable(true);

                } else if (!cPwd.equals(pwd)) {
                    passwordConfirm.setTransformationMethod(null);
                    passwordEdit.setTransformationMethod(null);
                    passwordConfirm.setError("password does not match ");
                    passwordConfirm.setFocusable(true);
                } else {

                    mListener.onboardContinueSelected(email,pwd);
                }
                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                break;

            case R.id.login_link:
                //TODO: Jerad/ need to work on Registration
              mListener.loginSelected();
                break;
            default:
                Log.d(TAG,"shouldnt happen");
        }
    }

}

