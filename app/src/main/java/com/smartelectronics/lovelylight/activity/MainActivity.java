package com.smartelectronics.lovelylight.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.smartelectronics.lovelylight.R;
import com.smartelectronics.lovelylight.fragments.AboutFragment;
import com.smartelectronics.lovelylight.fragments.DevicesFragment;
import com.smartelectronics.lovelylight.fragments.HomeFragment;
import com.smartelectronics.lovelylight.fragments.SettingsFragment;
import com.smartelectronics.lovelylight.fragments.TimerFragment;
import com.smartelectronics.lovelylight.interfaces.BluetoothStateChange;
import com.smartelectronics.lovelylight.interfaces.ColorPickerCallBack;
import com.smartelectronics.lovelylight.interfaces.DeviceClick;
import com.smartelectronics.lovelylight.interfaces.FinishTimer;
import com.smartelectronics.lovelylight.interfaces.LightSensorChange;
import com.smartelectronics.lovelylight.interfaces.PumpValue;
import com.smartelectronics.lovelylight.interfaces.SettingsClick;
import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothControl;
import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothObject;
import com.smartelectronics.lovelylight.utils.colorPicker.ColorConverter;
import com.smartelectronics.lovelylight.utils.CustomToast;
import com.smartelectronics.lovelylight.utils.MyAlertDialog;
import com.smartelectronics.lovelylight.utils.PrefUtils;
import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothSPP;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements BluetoothSPP.BluetoothConnectionListener,
        DeviceClick, SettingsClick, ColorPickerCallBack, SensorEventListener, FinishTimer, PumpValue, HomeFragment.ChangeTheme {


    // variables
    private int color;
    private int pumpPower;

    private BottomNavigationView navigationView;
    private CustomToast toast;
    private Menu menu;

    // fragments
    private HomeFragment     homeFragment;
    private DevicesFragment  devicesFragment;
    private AboutFragment    aboutFragment;
    private SettingsFragment settingsFragment;
    private TimerFragment    timerFragment;
    private Fragment active;
    private FragmentManager fm;
    private FrameLayout mainFrame;


    // bluetooth objects
    public static BluetoothSPP bluetoothSPP;
    private BluetoothControl btControl;
    private ColorConverter colorConverter;
    private String btName;
    private int btState;
    /*private BluetoothCommunication btCommunication;
    private Handler handler;*/

    private BluetoothStateChange bluetoothState;
    private final String TAG = "btAddress";


    // orientation, light sensors objects
    private SensorManager sensorManager = null;
    private Sensor gSensor , lSensor = null;
    private LightSensorChange lightSensorChange;


    // setting boolean flags
    private boolean timerFlag         = false;
    private boolean accelerometerFlag = false;
    private boolean lightFlag         = false;
    private boolean micFlag           = false;
    private String ledSwitch         = "F";
    private String pumpSwitch        = "F";


    // sound generator
    ToneGenerator toneG;

    @Override
    public void onBackPressed() {

        final MyAlertDialog alertDialog = new MyAlertDialog(this);
        alertDialog.setTitle("Exit");
        alertDialog.setMessage("Do you want to exit now?");
        alertDialog.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                btControl.turnOffBt();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.app_bar_switch) {

            invalidateOptionsMenu();
            ledSwitch = "F";

            if(bluetoothSPP.isServiceAvailable())
                bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch,"E"), false);
            return true;
        }

        if(id == R.id.app_bar_switch_off){

            invalidateOptionsMenu();
            ledSwitch = "N";

            if(bluetoothSPP.isServiceAvailable())
                bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch, "E"), false);
            return true;
        }

        if(id == R.id.pump_on) {

            invalidateOptionsMenu();
            pumpSwitch = "F";

            if(bluetoothSPP.isServiceAvailable())
                bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch, "E"), false);
            return true;
        }

        if(id == R.id.pump_off){

            invalidateOptionsMenu();
            pumpSwitch = "N";

            if(bluetoothSPP.isServiceAvailable())
                bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch,"E"), false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (ledSwitch.equals("N")) {
            menu.findItem(R.id.app_bar_switch_off).setVisible(false);
            menu.findItem(R.id.app_bar_switch).setVisible(true);
        } else {
            menu.findItem(R.id.app_bar_switch_off).setVisible(true);
            menu.findItem(R.id.app_bar_switch).setVisible(false);
        }

        if (pumpSwitch.equals("N")) {
            menu.findItem(R.id.pump_off).setVisible(false);
            menu.findItem(R.id.pump_on).setVisible(true);
        } else {
            menu.findItem(R.id.pump_off).setVisible(true);
            menu.findItem(R.id.pump_on).setVisible(false);
        }
        return true;
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // disable sensors
        sensorManager.unregisterListener(this);
        toneG.release();
    }

    /// DeviceClick interface
    @Override
    public void onDeviceClick(BluetoothDevice btDevice) {

        Log.i(TAG, "onDeviceClick: "+ btDevice.getAddress());
        bluetoothSPP.connect(btDevice.getAddress());
    }

    /// SettingsClick interface

    @Override
    public void onBtNameSend(String newBluetoothName) {

        if(bluetoothSPP.isServiceAvailable())
            Log.i(TAG, "onBtNameSend: " + newBluetoothName);

    }

    @Override
    public void onTimerModeClick(boolean timerFlag) {
        this.timerFlag = timerFlag;
    }

    @Override
    public void onAccelerometerModeClick(boolean sensorFlag) {
        this.accelerometerFlag = sensorFlag;

        if(this.accelerometerFlag)
            initOrientationSensor();
        if(!this.accelerometerFlag && !this.lightFlag)
            cleanup();
        //Log.i(TAG, "onSensorModeClick: " + accelerometerFlag);
    }

    @Override
    public void onLightModeClick(boolean lightFlag) {
        this.lightFlag = lightFlag;

        if(this.lightFlag)
            initLightSensor();
        if(!this.accelerometerFlag && !this.lightFlag)
            cleanup();

        Log.i(TAG, "onLightModeClick: " + lightFlag);
    }

    @Override
    public void onMicrophoneModeClick(boolean micFlag) {

    }


    /// BluetoothSpp interface
    @Override
    public void onDeviceConnected(String name, String address) {

        toast.showMessage("Connect to the device.");
        if(isFragmentShowing(homeFragment))
            bluetoothState.onBluetoothStateUpdate(2, name);

        sendDataToFragment(homeFragment, "btState", "btName",2,name);

        toneG.startTone(ToneGenerator.TONE_PROP_PROMPT, 800);

    }

    @Override
    public void onDeviceDisconnected() {

        toast.showMessage("Disconnected from the device.");
        if(isFragmentShowing(homeFragment))
            bluetoothState.onBluetoothStateUpdate(0, "NA");

        sendDataToFragment(homeFragment, "btState", "btName",0,"NA");

        toneG.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 800);
    }

    @Override
    public void onDeviceConnectionFailed() {

        toast.showMessage("Unable to connect.");
        if(isFragmentShowing(homeFragment))
            bluetoothState.onBluetoothStateUpdate(0, "NA");

        sendDataToFragment(homeFragment, "btState", "btName",0,"NA");
    }


    /// ColorPicker interface
    @Override
    public void onColorChanged(int color) {

        this.color = color;

        if(bluetoothSPP.isServiceAvailable())
            bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch, "E"), false);
    }


    /// SensorManager interface
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        switch (sensorEvent.sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                Random rnd = new Random();
                int color = Color.argb(0,
                        rnd.nextInt(256),
                        rnd.nextInt(256),
                        rnd.nextInt(256));

                String packet = colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch, "E");



                if(bluetoothSPP.isServiceAvailable() && sensorEvent.values[1] > 1 && this.accelerometerFlag) {
                    Log.i(TAG, "onSensorAccelerometerChanged: " + packet);
                    bluetoothSPP.send(packet, false);
                }

                break;

            case Sensor.TYPE_LIGHT:
                int dutyCycle = (int) sensorEvent.values[0];
                if(this.lightFlag)
                    lightSensorChange.onLightSensorUpdate(dutyCycle);
                Log.i(TAG, "onSensorLightChanged: " + dutyCycle);
                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /// Timer interface
    @Override
    public void onTimerTimeOut() {

        if(bluetoothSPP.isServiceAvailable() && timerFlag)
            bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch, "E"), false);

        toast.showMessage("Timer finished.");
        ledSwitch = "F";

    }


    /// Pump power interface
    @Override
    public void pumpUpdated(int pumpPower) {

        this.pumpPower = pumpPower;

        if(bluetoothSPP.isServiceAvailable())
            bluetoothSPP.send(colorConverter.getDataPacket("S", color, pumpPower, ledSwitch, pumpSwitch,"E"),false);
    }


    /// change theme interface
    @Override
    public void onThemeChanged(boolean nightFlag) {

        if(nightFlag) setDarkTheme();
        else setLightTheme();
    }

    // my methods >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void initToolBar(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Magic Light");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }
    private void initBottomNav(){

        navigationView = findViewById(R.id.bottom_nav);
        mainFrame      = findViewById(R.id.main_frame);

        navigationView.setSelectedItemId(R.id.nav_home);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.nav_home:
                        //navigationView.setItemBackgroundResource(R.color.colorPrimaryDark);
                        setFragment(homeFragment);
                        //fm.beginTransaction().hide(active).show(homeFragment).commit();
                        //active = homeFragment;
                        return true;

                    case R.id.nav_devices:
                        //navigationView.setItemBackgroundResource(R.color.colorAccent);
                        setFragment(devicesFragment);
                        //fm.beginTransaction().hide(active).show(devicesFragment).commit();
                        //active = devicesFragment;
                        return true;

                    case R.id.nav_about:
                        //navigationView.setItemBackgroundResource(R.color.colorPrimary);
                        setFragment(aboutFragment);
                        //fm.beginTransaction().hide(active).show(aboutFragment).commit();
                        //active = aboutFragment;
                        return true;

                    case R.id.nav_setting:
                        //fm.beginTransaction().hide(active).show(settingsFragment).commit();
                        //active = settingsFragment;
                        setFragment(settingsFragment);
                        return true;


                    case R.id.nav_stop_watch:
                        //fm.beginTransaction().hide(active).show(timerFragment).commit();
                        //active = timerFragment;
                        setFragment(timerFragment);
                        return true;

                    default:
                        return false;

                }
            }
        });
    }
    private void initFragments(){

        homeFragment     = new HomeFragment();
        devicesFragment  = new DevicesFragment();
        aboutFragment    = new AboutFragment();
        settingsFragment = new SettingsFragment();
        timerFragment    = new TimerFragment();
        /*active = homeFragment;

        fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.main_frame, devicesFragment, "2").hide(devicesFragment).commit();
        fm.beginTransaction().add(R.id.main_frame, settingsFragment, "3").hide(settingsFragment).commit();
        fm.beginTransaction().add(R.id.main_frame, timerFragment, "4").hide(timerFragment).commit();
        fm.beginTransaction().add(R.id.main_frame, aboutFragment, "5").hide(aboutFragment).commit();
        fm.beginTransaction().add(R.id.main_frame,homeFragment, "1").commit();*/

        // default fragment
        setFragment(homeFragment);
    }
    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.item_animation_fall_down, R.anim.jump_to_down);
        fragmentTransaction.replace(R.id.main_frame,fragment);
        fragmentTransaction.commit();
    }
    private boolean isFragmentShowing(Fragment fragment){

        if(fragment.isVisible())
            return true;
        else
            return false;
    }
    private void initBluetooth(){

        btControl    = new BluetoothControl(this);
        bluetoothSPP = BluetoothObject.get();
        //bluetoothSPP = new BluetoothSPP(this);
        bluetoothSPP.setBluetoothConnectionListener(this);

        //enable bluetooth module and setup it
        /*if(bluetoothSPP.isBluetoothEnabled()){

            if(!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }else {
            Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnable, BluetoothState.REQUEST_ENABLE_BT);
        }*/
        colorConverter = new ColorConverter();
    }
    private void cleanup(){
        // unregister with the orientation sensor before exiting:
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception ex) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    private void initOrientationSensor(){

        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // if we can't access the orientation sensor then exit:
        if (gSensor == null)
            cleanup();

        try {
            sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception ex) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    private void initLightSensor(){

        lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, lSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void readSettingsStatus(){

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometerFlag = PrefUtils.getFromPrefs(this, "accelerometerSensor", false);
        timerFlag         = PrefUtils.getFromPrefs(this, "timer", false);
        micFlag           = PrefUtils.getFromPrefs(this, "microphone", false);

        if(accelerometerFlag)
            initOrientationSensor();
        if(lightFlag)
            initLightSensor();
    }
    private void init(){

        initFragments();
        initToolBar();
        initBottomNav();
        initBluetooth();
        readSettingsStatus();

        // tone init
        toast = new CustomToast(this);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        // bluetooth connection check
        if(bluetoothSPP.getConnectedDeviceName() == null)
           sendDataToFragment(homeFragment,"btState","btName", 0, "NA");
        else
            sendDataToFragment(homeFragment,"btState","btName", 2,bluetoothSPP.getConnectedDeviceName());

        // theme change
        if(PrefUtils.getFromPrefs(this, "theme", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
    private void sendDataToFragment(Fragment fragment, String key1,String key2, int data1, String data2){

        Bundle bundle = new Bundle();
        bundle.putInt(key1, data1);
        bundle.putString(key2, data2);
        fragment.setArguments(bundle);
    }
    public void setBluetoothStateListener(BluetoothStateChange bluetoothState){

        this.bluetoothState = bluetoothState;
    }
    public void setLightSensorListener(LightSensorChange lightSensor){

        this.lightSensorChange = lightSensor;
    }
    private void setDarkTheme(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        //recreate();
    }
    private void setLightTheme(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //recreate();
    }
}
