package com.smartelectronics.lovelylight.fragments;


import android.animation.Animator;
import android.os.Bundle;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.airbnb.lottie.LottieAnimationView;
import com.smartelectronics.lovelylight.R;
import com.smartelectronics.lovelylight.interfaces.SettingsClick;
import com.smartelectronics.lovelylight.utils.CustomToast;
import com.smartelectronics.lovelylight.utils.PrefUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private View view;
    private Switch fadeSwitch, timerSwitch, accelerometerSwitch, lightSwitch, micSwitch;
    private RadioGroup speed;
    private RadioButton slowSpeed, mediumSpeed, fastSpeed;
    private EditText edtBtName;
    private SettingsClick settingsClick;
    private LottieAnimationView imgSend;

    private CustomToast toast;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        init();

        return view;
    }

    private void initViews(){

        settingsClick = (SettingsClick) getContext();

        edtBtName = view.findViewById(R.id.edt_bluetooth);
        imgSend   = view.findViewById(R.id.bt_send);
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String btName = edtBtName.getText().toString();
                if (btName.length() == 0)
                    toast.showMessage("Please insert your name.");

                if(btName.length() > 0)
                    imgSend.resumeAnimation();

                imgSend.addAnimatorListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        toast.showMessage("Magic Light module is renamed.");
                        settingsClick.onBtNameSend(btName);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });


        fadeSwitch = view.findViewById(R.id.fade_switch);
        fadeSwitch.setChecked(PrefUtils.getFromPrefs(getContext(), "fade", false));


        timerSwitch  = view.findViewById(R.id.timer_switch);
        timerSwitch.setChecked(PrefUtils.getFromPrefs(getContext(), "timer",false));


        accelerometerSwitch = view.findViewById(R.id.acc_sensor_switch);
        accelerometerSwitch.setChecked(PrefUtils.getFromPrefs(getContext(), "accelerometerSensor",false));


        lightSwitch = view.findViewById(R.id.light_sensor_switch);
        lightSwitch.setChecked(PrefUtils.getFromPrefs(getContext(), "lightSensor", false));


        micSwitch    = view.findViewById(R.id.mic_switch);
        micSwitch.setChecked(PrefUtils.getFromPrefs(getContext(), "microphone", false));


        fadeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                PrefUtils.saveToPrefs(getContext(), "fade", b);
            }
        });

        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsClick.onTimerModeClick(b);
                PrefUtils.saveToPrefs(getContext(), "timer", b);
            }
        });

        accelerometerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsClick.onAccelerometerModeClick(b);
                PrefUtils.saveToPrefs(getContext(), "accelerometerSensor", b);
            }
        });

        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsClick.onLightModeClick(b);
                PrefUtils.saveToPrefs(getContext(), "lightSensor", b);
            }
        });

        micSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsClick.onMicrophoneModeClick(b);
                PrefUtils.saveToPrefs(getContext(), "microphone", b);
            }
        });


        // init Radio buttons and Radio button group
        slowSpeed   = view.findViewById(R.id.rdo_slow_speed);
        mediumSpeed = view.findViewById(R.id.rdo_medium_speed);
        fastSpeed  = view.findViewById(R.id.rdo_fast_speed);
        speed = view.findViewById(R.id.rdo_speed);

        if(PrefUtils.getFromPrefs(getContext(), "slow speed", false))
            slowSpeed.setChecked(true);
        else if (PrefUtils.getFromPrefs(getContext(), "medium speed", false))
            mediumSpeed.setChecked(true);
        else
            fastSpeed.setChecked(true);


        speed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                // find which radio button is selected
                if(i == R.id.rdo_slow_speed) {
                    PrefUtils.saveToPrefs(getContext(),"slow speed", true);
                    PrefUtils.saveToPrefs(getContext(),"medium speed", false);
                    PrefUtils.saveToPrefs(getContext(),"fast speed", false);

                } else if(i == R.id.rdo_medium_speed) {
                    PrefUtils.saveToPrefs(getContext(),"slow speed", false);
                    PrefUtils.saveToPrefs(getContext(),"medium speed", true);
                    PrefUtils.saveToPrefs(getContext(),"fast speed", false);

                } else {
                    PrefUtils.saveToPrefs(getContext(),"slow speed", false);
                    PrefUtils.saveToPrefs(getContext(),"medium speed", false);
                    PrefUtils.saveToPrefs(getContext(),"fast speed", true);
                }
            }
        });

    }
    private void init(){

        toast = new CustomToast(getContext());
        initViews();
    }

}
