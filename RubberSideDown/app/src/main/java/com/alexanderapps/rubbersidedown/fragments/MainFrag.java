package com.alexanderapps.rubbersidedown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.listeners.Login_Register_Listener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    Button registerButton;
    Button loginButton;
    Login_Register_Listener mListener;
    private FirebaseAuth mAuth;

    public static MainFrag newInstance() {

        Bundle args = new Bundle();

        MainFrag fragment = new MainFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mainlog_layout, container,false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if(context instanceof Login_Register_Listener){
            mListener = (Login_Register_Listener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerButton = Objects.requireNonNull(getView()).findViewById(R.id.register_button);
        loginButton = getView().findViewById(R.id.login_button);
        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v.getId() == registerButton.getId()){
            Toast.makeText(getContext(),"register selected", Toast.LENGTH_SHORT).show();
            mListener.registerSelected();
        }
        else if(v.getId() == loginButton.getId()){
            Toast.makeText(getContext(),"Login selected", Toast.LENGTH_SHORT).show();
            mListener.loginSelected();
        }else {
            Log.d(TAG, "shouldn't happen");
        }
    }


}
