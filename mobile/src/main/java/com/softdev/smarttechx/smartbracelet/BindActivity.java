package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.DeviceDetails;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.service.BluetoothLeService;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.softdev.smarttechx.smartbracelet.ConnectActivity.EXTRAS_DEVICE_ADDRESS;
import static com.softdev.smarttechx.smartbracelet.util.SessionManager.KEY_EMAIL;

public class BindActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private final int WAIT_DELAY = 500;
    TextView mStatus;
    Button mBind;
    SessionManager session;
    SaveData savedata;
    UserDetails userDetails;
    boolean gpsStatus;
    DeviceDetails deviceDetails;
    LocationManager locationManager;
    IntentFilter btIntent;
    Intent intent1;
    private String mDeviceName = null, mMac = null;
    private String mName, mLast, mEmail, mPic, mPass;
    private CoordinatorLayout mRoot;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BTReceiver mBTReceiver;
    private ProgressDialog bindprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        mBind = findViewById(R.id.bindBracelet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        mStatus = findViewById(R.id.bindStatus);
        mRoot = findViewById(R.id.parentRoot);
        session = new SessionManager(this);
        mBind.setOnClickListener(this);
        savedata = new SaveData(this);
        userDetails = new UserDetails();
        final Intent macInt = getIntent();
        mMac = macInt.getStringExtra(EXTRAS_DEVICE_ADDRESS);

       /* if (!session.isLoggedIn()) {
            Intent login = new Intent(BindActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBTReceiver = new BTReceiver();


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        checkPermission();

    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mBTReceiver, new IntentFilter(btIntent));
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CheckGPSstatus();
                }
            }, WAIT_DELAY);
        }
        mStatus.setText("If Bracelet not found disable bluetooth for 5 sec and enable it ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent mail = getIntent();

        deviceDetails = new DeviceDetails();
        userDetails = new UserDetails();
        userDetails = savedata.loadUserInfo();
        deviceDetails = savedata.loadDevice();
        if (userDetails != null) {
            mName = userDetails.getName();
            mLast = userDetails.getLastname();
            mEmail = userDetails.getEmail();
            mPic = userDetails.getProfilepic();
            mPass = userDetails.getPassword();

        }

        if (mEmail == null || mEmail.isEmpty() || mEmail.length() == 0) {
            mEmail = mail.getStringExtra("email");
        }
        if (mName == null || mName.isEmpty() || mName.length() == 0) {
            mName = mail.getStringExtra("name");
        }
        if (mLast == null || mLast.isEmpty() || mLast.length() == 0) {
            mLast = mail.getStringExtra("lastname");
        }
        if (deviceDetails != null) {
            mDeviceName = deviceDetails.getName();
            // mMac = deviceDetails.getMac();
            if (mDeviceName != null && mMac != null) {
                if (mDeviceName.equals("K1") || mDeviceName.equals("M2S")) {
                    BindDevice(mName, mLast, mEmail, mMac, mPic, mPass);
                } else {
                    Snackbar.make(mRoot, "Device cannot bind to " + mDeviceName, Snackbar.LENGTH_SHORT).show();
                }


            }
        }
    }

    public void CheckGPSstatus() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsStatus == true) {
        } else {
            intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mBTReceiver);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(BindActivity.this,
                                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                                1);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }

    }

    public void onClick(View v) {
        if (v == mBind) {
            if (mBind.getText().equals("Next")) {
                Intent connect = new Intent(BindActivity.this, ConnectActivity.class);
                session.createLoginSession(mName, mLast, mEmail, mPic, mPass);
                connect.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                connect.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                connect.putExtra(EXTRAS_DEVICE_ADDRESS, mMac);
                startActivity(connect);
                this.finish();

            } else {
                mStatus.setText("");
                Intent serverIntent = new Intent(this, DeviceScanActivity.class);
                serverIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                serverIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(serverIntent);
            }
        }
    }

    private void BindDevice(final String name, final String lastname, final String email, final String macaddress, final String profilePic, final String password) {
        try {
            bindprogress = new ProgressDialog(this);
            bindprogress.setIndeterminate(true);
            bindprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            bindprogress.setMessage("Binding in progress..");
            bindprogress.setCancelable(false);
            bindprogress.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);
            UserDetails user = new UserDetails();

            user.setEmail(email);
            user.setMacaddress(macaddress);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.BIND_DEVICE);
            request.setUser(user);
            Call<ServerResponse> response = requestInterfaceObject.operation(request);

            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                    ServerResponse resp = response.body();
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        Snackbar.make(mRoot, "Device bind to " + mDeviceName, Snackbar.LENGTH_SHORT).show();
                        mStatus.setText("Device Bind to " + mDeviceName);
                        session.createLoginSession(name, lastname, email, profilePic, password);
                        mBind.setText("Next");
                        userDetails = new UserDetails();
                        userDetails = savedata.loadUserInfo();
                        userDetails.setName(mName);
                        userDetails.setLastname(mLast);
                        userDetails.setEmail(mEmail);
                        userDetails.setProfilepic(mPic);
                        userDetails.setPassword(mPass);
                        userDetails.setMacaddress(mMac);
                        userDetails.setBandname(mDeviceName);
                        savedata.saveUserInfo(userDetails);
                        bindprogress.setMessage("Bind successful...");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bindprogress != null) {
                                    bindprogress.dismiss();
                                }
                            }
                        }, 500);
                    } else {
                        if (resp.getMessage().contains("Already bind to another device")) {
                            Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
                            if (resp.getUser().getEmail().equals(email) && resp.getUser().getMacaddress().equals(macaddress)) {
                                Snackbar.make(mRoot, "Device bind to " + mDeviceName, Snackbar.LENGTH_SHORT).show();
                                mStatus.setText("Device Bind to " + mDeviceName);
                                session.createLoginSession(name, lastname, email, profilePic, password);
                                mBind.setText("Next");
                                userDetails = new UserDetails();
                                userDetails = savedata.loadUserInfo();
                                userDetails.setName(mName);
                                userDetails.setLastname(mLast);
                                userDetails.setEmail(mEmail);
                                userDetails.setProfilepic(mPic);
                                userDetails.setPassword(mPass);
                                userDetails.setMacaddress(mMac);
                                userDetails.setBandname(mDeviceName);
                                userDetails.setIsBind(true);
                                savedata.saveUserInfo(userDetails);
                                bindprogress.setMessage("Bind successful...");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bindprogress != null) {
                                            bindprogress.dismiss();
                                        }
                                    }
                                }, 500);
                            } else {
                                bindprogress.setMessage("Bind unsuccessful...");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bindprogress != null) {
                                            bindprogress.dismiss();
                                        }
                                    }
                                }, 500);

                                Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Log.d(Constants.TAG, t.getMessage());
                    Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 1) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return true;
        } else {
            Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show();
        }

        return super.onKeyDown(keyCode, event);
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
                            CheckGPSstatus();
                        }
                    }, WAIT_DELAY);
                }

            }
        }
    }

}
