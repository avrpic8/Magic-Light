package com.smartelectronics.lovelylight.fragments;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.smartelectronics.lovelylight.activity.MainActivity;
import com.smartelectronics.lovelylight.R;
import com.smartelectronics.lovelylight.interfaces.BluetoothStateChange;
import com.smartelectronics.lovelylight.interfaces.ColorPickerCallBack;
import com.smartelectronics.lovelylight.interfaces.LightSensorChange;
import com.smartelectronics.lovelylight.interfaces.PumpValue;
import com.smartelectronics.lovelylight.utils.colorPicker.ColorPicker;
import com.smartelectronics.lovelylight.utils.PrefUtils;
import com.smartelectronics.lovelylight.utils.colorPicker.SaturationBar;
import com.smartelectronics.lovelylight.utils.colorPicker.ValueBar;
import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothState;
import com.smartelectronics.lovelylight.utils.service.CounterService;

import java.util.Timer;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements BluetoothStateChange, LightSensorChange{

    private final String TAG = "HOME";
    private static final int PUMP_MIN = 50;

    private boolean nightFlag = false;

    // view objects
    private TextView txtBtStatusLabel, txtBtLabel;
    private LinearLayout btStatusLayout, btnLayout;
    private RelativeLayout layoutBars;
    private Button btnStartRotation, btnStopRotation;
    private ImageView nightMode,btSwitch;
    private VerticalSeekBar pumpPower;


    private View view;

    // color pickers instance
    private ColorPicker colorPicker;
    private SaturationBar satBar;
    private ValueBar valueBar;
    private ColorPickerCallBack pickerCallBack;

    // image view instances
    private ImageView imgTimer, imgAccSensor, imgLightSensor, imgMic;

    // bluetooth state and name
    private int btStatus;
    private String btName;

    private int angel = 1, fadeSpeed;
    private Timer timer;
    private boolean timerFlag = false;

    // pump listener
    private PumpValue pumpValue;

    // listener
    private ChangeTheme changeTheme;

    // receiver
    private CounterReceiver receiver;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_home, container, false);

        init();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopColorPickerRotation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {

            if(resultCode == Activity.RESULT_OK) {
                MainActivity.bluetoothSPP.setupService();
                MainActivity.bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    @Override
    public void onBluetoothStateUpdate(int state, String deviceName) {
        showBluetoothStatus(state, deviceName);

        if(state == 2)
            btSwitch.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_bluetooth_on));
        else btSwitch.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_bluetooth_off));

    }

    @Override
    public void onLightSensorUpdate(int value) {

        valueBar.setValue(value);
    }


    // my methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void initColorPickers(View view){

        colorPicker = view.findViewById(R.id.color_picker);
        satBar       = view.findViewById(R.id.sat_bar);
        valueBar    = view.findViewById(R.id.value_bar);

        colorPicker.addSaturationBar(satBar);
        colorPicker.addValueBar(valueBar);
        colorPicker.setShowOldCenterColor(false);


        pickerCallBack = (ColorPickerCallBack) getActivity();
        colorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {

                pickerCallBack.onColorChanged(color);
            }
        });
    }
    private void initTextViews(View view){

        txtBtStatusLabel = view.findViewById(R.id.txt_bt_status);
        btStatusLayout = view.findViewById(R.id.ll_bt_status);

        txtBtLabel = view.findViewById(R.id.txt_bt_name);
    }
    private void initSeekBar(View view){

        pumpPower = view.findViewById(R.id.seek_motor);
        pumpPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(progress < PUMP_MIN) {
                    pumpPower.setProgress(PUMP_MIN);
                    pumpValue.pumpUpdated(PUMP_MIN);
                }else
                    pumpValue.pumpUpdated(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void initImageViews(){

        imgTimer = view.findViewById(R.id.img_timer);
        if(PrefUtils.getFromPrefs(getContext(), "timer", false))
            imgTimer.setImageResource(R.drawable.timer);
        else
            imgTimer.setImageResource(R.drawable.timer_off);


        imgAccSensor = view.findViewById(R.id.img_accSensor);
        if(PrefUtils.getFromPrefs(getContext(), "accelerometerSensor", false))
            imgAccSensor.setImageResource(R.drawable.acc_sensor);
        else
            imgAccSensor.setImageResource(R.drawable.acc_sensor_off);

        imgLightSensor = view.findViewById(R.id.img_light);
        if(PrefUtils.getFromPrefs(getContext(), "lightSensor", false))
            imgLightSensor.setImageResource(R.drawable.light_sensor);
        else
            imgLightSensor.setImageResource(R.drawable.light_sensor_off);

        imgMic = view.findViewById(R.id.img_mic);
        if(PrefUtils.getFromPrefs(getContext(), "microphone", false))
            imgMic.setImageResource(R.drawable.mic);
        else
            imgMic.setImageResource(R.drawable.mic_off);
    }
    private void initListener(){

        pumpValue   = (PumpValue) getContext();
        changeTheme = (ChangeTheme) getContext();

        ((MainActivity)getActivity()).setBluetoothStateListener(this);
        ((MainActivity)getActivity()).setLightSensorListener(this);
    }
    private void initTimer(){

        timer = new Timer();

        if(PrefUtils.getFromPrefs(getContext(), "slow speed", false))
            fadeSpeed = 500;
        else if(PrefUtils.getFromPrefs(getContext(), "medium speed", false))
            fadeSpeed = 100;
        else
            fadeSpeed = 50;
    }
    private void initButtons(View view){

        btnStartRotation = view.findViewById(R.id.btn_start_rotation);
        btnStopRotation  = view.findViewById(R.id.btn_stop_rotation);

        btnStartRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startColorPickerRotation();
            }
        });

        btnStopRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopColorPickerRotation();
            }
        });

        nightMode = view.findViewById(R.id.theme_switch);
        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PrefUtils.getFromPrefs(getContext(), "theme", false)){
                    nightFlag = false;
                    changeTheme.onThemeChanged(nightFlag);
                }else{
                    nightFlag = true;
                    changeTheme.onThemeChanged(nightFlag);
                }
                PrefUtils.saveToPrefs(getContext(), "theme", nightFlag);
            }
        });

        btSwitch = view.findViewById(R.id.bluetooth_switch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // enable bluetooth module and setup it
                if(MainActivity.bluetoothSPP.isBluetoothEnabled()){

                    if(!MainActivity.bluetoothSPP.isServiceAvailable()) {
                        MainActivity.bluetoothSPP.setupService();
                        MainActivity.bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                    }
                }else {
                    Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btEnable, BluetoothState.REQUEST_ENABLE_BT);
                }
            }
        });
    }
    private void initLayout(View view){

        btnLayout  = view.findViewById(R.id.layout_buttons);
        layoutBars = view.findViewById(R.id.layout_bar);

        boolean fadeAnimationFlag = PrefUtils.getFromPrefs(getContext(), "fade", false);
        if (fadeAnimationFlag) {
            btnLayout.setVisibility(View.VISIBLE);
            layoutBars.setVisibility(View.INVISIBLE);
        }
        else {
            btnLayout.setVisibility(View.INVISIBLE);
            layoutBars.setVisibility(View.VISIBLE);
        }

    }
    /*private void startColorPickerAutoRotation(){

        if(!timerFlag) {
            initTimer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    angel++;
                    if (angel == 360)
                        angel = 0;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            colorPicker.setColor(colorPicker.calculateColor((float) Math.toRadians(angel)));
                        }
                    });

                    //Log.i(TAG, "angel: " + angel);
                }
            };
            timer.schedule(task, 0, fadeSpeed);
            timerFlag = true;
        }
    }
    private void stopColorPickerAutoRotation(){

        if(timer != null && timerFlag) {
            timer.cancel();
            timerFlag = false;
        }
    }*/
    private void startColorPickerRotation(){
        if(PrefUtils.getFromPrefs(getContext(), "slow speed", false))
            fadeSpeed = 500;
        else if(PrefUtils.getFromPrefs(getContext(), "medium speed", false))
            fadeSpeed = 100;
        else
            fadeSpeed = 50;

        if(!timerFlag) {
            getContext().registerReceiver(receiver, new IntentFilter("COUNTER_RECEIVER"));
            Intent serviceIntent = new Intent(getContext(), CounterService.class);
            serviceIntent.putExtra("speed", fadeSpeed);
            serviceIntent.putExtra("angel", angel);
            getContext().startService(serviceIntent);
            timerFlag = true;
        }
    }
    private void stopColorPickerRotation(){
        if(timerFlag) {
            getContext().unregisterReceiver(receiver);
            Intent serviceIntent = new Intent(getContext(), CounterService.class);
            getContext().stopService(serviceIntent);
            timerFlag = false;
        }
    }
    private void showBluetoothStatus(int status, String deviceName){

        txtBtLabel.setText(deviceName);
        Log.i(TAG, "showBluetoothStatus: " + status + "  " + deviceName);
        if(status == 0) {

            txtBtStatusLabel.setText("Disconnected");
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                btStatusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bt_status_red));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btStatusLayout.setBackground(getContext().getDrawable(R.drawable.bt_status_red));
            }
        }

        if(status == 2) {

            txtBtStatusLabel.setText("Connected");
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                btStatusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bt_status_green));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btStatusLayout.setBackground(getContext().getDrawable(R.drawable.bt_status_green));
            }
        }
    }

    private void init(){

        initLayout(view);
        initButtons(view);
        initTextViews(view);
        initColorPickers(view);
        initSeekBar(view);
        initImageViews();
        initListener();
        Log.i(TAG, "init: ");

        btStatus = getArguments().getInt("btState");
        btName   = getArguments().getString("btName");
        showBluetoothStatus(btStatus, btName);

        // change color of moon icon
        if(PrefUtils.getFromPrefs(getContext(), "theme", false))
            nightMode.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_night_on));
        else nightMode.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_night_off));

        if(MainActivity.bluetoothSPP.getConnectedDeviceName() != null)
            btSwitch.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_bluetooth_on));
        else btSwitch.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_bluetooth_off));


        receiver = new CounterReceiver();
    }

    public interface ChangeTheme{

        void onThemeChanged(boolean nightFlag);
    }


    private class CounterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("COUNTER_RECEIVER")) {
                angel = intent.getIntExtra("counter",0);
                colorPicker.setColor(colorPicker.calculateColor((float) Math.toRadians(angel)));
            }
        }
    }
}
