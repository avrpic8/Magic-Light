package com.smartelectronics.lovelylight.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;

import com.smartelectronics.lovelylight.R;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;


    //private ConstraintSet constraintSet;
    private ConstraintLayout constraint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideStatusBar();
        setContentView(R.layout.activity_splash);

        init();

        //showComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(runnable);
    }

    private void hideStatusBar(){

        this.getWindow().setFlags(WindowManager.LayoutParams.
                FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Hide Status bar
    }
    private void init(){

        handler  = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable,6000);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showComponents(){

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_splash);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        transition.setDuration(1200);

        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
    }
}
