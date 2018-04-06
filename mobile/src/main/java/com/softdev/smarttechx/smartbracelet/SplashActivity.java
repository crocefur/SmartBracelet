package com.softdev.smarttechx.smartbracelet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.service.TaskkillService;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static com.softdev.smarttechx.smartbracelet.util.SessionManager.KEY_EMAIL;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = SplashActivity.class.getSimpleName();
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    IntentFilter btIntent;
    Intent intent1;
    LocationManager locationManager;
    boolean GpsStatus;
    CoordinatorLayout mRoot;
    SessionManager session;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BTReceiver mBTReceiver;
    private HashMap<String, String> userDetails = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mRoot = findViewById(R.id.parentRoot);
        mBTReceiver = new BTReceiver();
        session = new SessionManager(getApplicationContext());
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mBTReceiver, new IntentFilter(btIntent));
        startService(new Intent(getBaseContext(), TaskkillService.class));
        if (session.isLoggedIn()) {
            userDetails = session.getUserDetails();
            String log_type = userDetails.get(KEY_EMAIL);
            if (log_type.contains("admin")) {
                Intent startActivityIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(startActivityIntent);
                finish();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckGpsStatus();
                        }
                    }, SPLASH_DISPLAY_LENGTH);
                }
            }

        } else {
            Intent login = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mBTReceiver);
    }

    public void CheckGpsStatus() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (GpsStatus == true) {
            Intent startActivityIntent = new Intent(SplashActivity.this, ConnectActivity.class);
            startActivity(startActivityIntent);
            finish();

        } else {
            intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }

    }

    private class BTReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckGpsStatus();
                        }
                    }, SPLASH_DISPLAY_LENGTH);
                }

            }
        }
    }

}