package com.smartelectronics.lovelylight.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartelectronics.lovelylight.R;
import com.smartelectronics.lovelylight.interfaces.FinishTimer;
import com.smartelectronics.lovelylight.utils.CustomToast;
import com.smartelectronics.lovelylight.utils.colorPicker.HoloCircleSeekBar;
import com.smartelectronics.lovelylight.utils.PrefUtils;
import com.smartelectronics.lovelylight.utils.ProgressBarAnimation;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment {

    private View view;

    private ImageView   reset, start, stop;
    private ProgressBar progressBarCircle;
    private HoloCircleSeekBar holoCircleSeekBar;
    private TextView    textViewTime;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private long timeCountInMilliSeconds = 1 * 60000;
    private boolean timerFlag;
    private CountDownTimer countDownTimer;
    private FinishTimer finishTimer;

    private CustomToast toast;

    public TimerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onResume() {
        super.onResume();

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        resumeState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_timer, container, false);

        init();

        return view;
    }


    private void initViews(){

        reset = view.findViewById(R.id.img_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        start = view.findViewById(R.id.img_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(timerFlag)
                    start();
                else
                    toast.showMessage("Pleas set timer mode in settings.");


            }
        });

        stop = view.findViewById(R.id.img_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });

        progressBarCircle = view.findViewById(R.id.progressBarCircle);
        holoCircleSeekBar = view.findViewById(R.id.seekBar);

        textViewTime      = view.findViewById(R.id.textViewTime);
        Typeface type = Typeface.createFromAsset(getContext().getAssets(),"fonts/roboto_thin.ttf");
        textViewTime.setTypeface(type);
    }
    private void init(){

        initViews();

        // init listener
        finishTimer = (FinishTimer) getContext();

        // read setting
        timerFlag = PrefUtils.getFromPrefs(getContext(), "timer", false);

        toast = new CustomToast(getContext());
    }
    private void reset() {
        stopCountDownTimer();
        startCountDownTimer();
    }
    private void start(){

        // call to initialize the timer values
        setTimerValues();
        // call to initialize the progress bar values
        setProgressBarValues();
        // showing the reset icon
        reset.setVisibility(View.VISIBLE);

        //holoCircleSeekBar.setVisibility(View.INVISIBLE);
        //textViewTime.setVisibility(View.VISIBLE);

        startCountDownTimer();
        startState();

        timerStatus = TimerStatus.STARTED;
    }
    private void stop(){

        stopCountDownTimer();

        stopState();
        timerStatus = TimerStatus.STOPPED;
    }
    private void startState(){

        reset.setEnabled(true);
        reset.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        start.setEnabled(false);
        start.setColorFilter(ContextCompat.getColor(getContext(), R.color.fbutton_color_asbestos), PorterDuff.Mode.SRC_IN);

        stop.setEnabled(true);
        stop.setColorFilter(ContextCompat.getColor(getContext(), R.color.fbutton_color_carrot), PorterDuff.Mode.SRC_IN);



        holoCircleSeekBar.animate()
                .alpha(2f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        holoCircleSeekBar.setVisibility(View.INVISIBLE);
                        textViewTime.setVisibility(View.VISIBLE);
                    }
                });
        //holoCircleSeekBar.setVisibility(View.INVISIBLE);
        //textViewTime.setVisibility(View.VISIBLE);

    }
    private void stopState(){

        reset.setEnabled(false);
        reset.setColorFilter(ContextCompat.getColor(getContext(), R.color.fbutton_color_asbestos), PorterDuff.Mode.SRC_IN);

        start.setEnabled(true);
        start.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        stop.setEnabled(false);
        stop.setColorFilter(ContextCompat.getColor(getContext(), R.color.fbutton_color_asbestos), PorterDuff.Mode.SRC_IN);


        textViewTime.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        holoCircleSeekBar.setVisibility(View.VISIBLE);
                        textViewTime.setVisibility(View.INVISIBLE);
                    }
                });
        //holoCircleSeekBar.setVisibility(View.VISIBLE);
        //textViewTime.setVisibility(View.INVISIBLE);

    }
    private void resumeState(){

        if(timerStatus == TimerStatus.STARTED){
            startState();
        }else {

            stopState();
        }
    }
    private void setTimerValues() {

        int time = 0;
        time = holoCircleSeekBar.getValue();
        if(time > 0) {
            // assigning values after converting to milliseconds
            timeCountInMilliSeconds = time * 60 * 1000;
            Log.i(TAG, "setTimerValues: " + timeCountInMilliSeconds);
        }
        else
            toast.showMessage("Please set timer and start it.");


    }
    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {


                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));

                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                Log.i(TAG, "onTick: " + millisUntilFinished);

            }

            @Override
            public void onFinish() {

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                setProgressBarValues();
                stopCountDownTimer();

                // start animation
                ProgressBarAnimation anim = new ProgressBarAnimation(progressBarCircle, 0, timeCountInMilliSeconds / 1000);
                anim.setDuration(3000);
                progressBarCircle.startAnimation(anim);


                // listener to activity
                finishTimer.onTimerTimeOut();
                timerStatus = TimerStatus.STOPPED;

            }

        }.start();
        countDownTimer.start();
    }
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }
    private String hmsTimeFormatter(long milliSeconds) {

        @SuppressLint("DefaultLocale") String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }

}
