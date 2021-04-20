package com.jerad_zac.solace.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.jerad_zac.solace.R;

public class

Crisis_Frag extends Fragment {

    private static final String TAG = "Crisis_Frag";

    private boolean iscalling = true;
    private CountDownTimer timer;
    private TextView callingInTV;
    private TextView countdownTV;


    public Crisis_Frag() {
    }

    public static Crisis_Frag newInstance() {

        Bundle args = new Bundle();

        Crisis_Frag fragment = new Crisis_Frag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.suicide_hotline_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null && getContext() != null) {

            callingInTV = getView().findViewById(R.id.callingInTV);
            countdownTV = getView().findViewById(R.id.countdownIV);

            LottieAnimationView crisisHotline = getView().findViewById(R.id.hotlineImgBtn);
            crisisHotline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    callingInTV.setVisibility(View.VISIBLE);
                    countdownTV.setVisibility(View.VISIBLE);

                    if (iscalling) {
                        iscalling = false;
                        countdownTimer();
                        timer.start();
                    } else {
                        iscalling = true;
                        timer.cancel();
                        callingInTV.setVisibility(View.INVISIBLE);
                        countdownTV.setVisibility(View.INVISIBLE);
                    }
                }
            });

            // Go Back button logic
            TextView goBackTVBtn = getView().findViewById(R.id.goBackTVBtn);
            goBackTVBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getFragmentManager() != null)
                    getFragmentManager().popBackStackImmediate();
                }
            });
        }
    }

    private void countdownTimer() {
        timer = new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTV.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                callingInTV.setVisibility(View.INVISIBLE);
                countdownTV.setVisibility(View.INVISIBLE);

                Uri number = Uri.parse("tel:18002738255");
                Intent intent = new Intent(Intent.ACTION_CALL, number);
                startActivity(intent);
            }
        };
    }
}
