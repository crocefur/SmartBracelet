package com.softdev.smarttechx.smartbracelet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.command.CommandManager;
import com.softdev.smarttechx.smartbracelet.constans.Constans;
import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.DeviceDetails;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.UserList;
import com.softdev.smarttechx.smartbracelet.model.Userdata;

import com.softdev.smarttechx.smartbracelet.service.BaseEvent;
import com.softdev.smarttechx.smartbracelet.service.BluetoothLeService;
import com.softdev.smarttechx.smartbracelet.service.NotificationService;
import com.softdev.smarttechx.smartbracelet.util.App;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.DataHandlerUtils;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceArray;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.softdev.smarttechx.smartbracelet.util.SessionManager.KEY_EMAIL;
import static java.lang.Math.abs;

public class ConnectActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "DEVICE_RSSI";
    final static int RQS_1 = 1;
    private final static String TAG = ConnectActivity.class.getSimpleName();
    private static DecimalFormat formatDis = new DecimalFormat("#.##");
    final Handler handler = new Handler();
    private final int Wait_delay = 50;
    SessionManager session;
    TextView mInstruct;
    boolean isHistory = false;
    int count = 0;
    boolean goTo;

    ArrayList<UserDetails> userList;
    ArrayList<String> historyDate;
    long last_stoptime;
    String new_date;
    Timer timer = new Timer();
    TimerTask doAsynchronousTask;
    Timer timerProg = new Timer();
    TimerTask doProgressTask;
    Timer timeSync = new Timer();
    TimerTask doProgressSync;
    Calendar dateTime = Calendar.getInstance();
    DeviceDetails deviceDetails;
    SaveData dataDB;
    UserDetails userdetails;
    Boolean data_load = false;
    int dataRecCt = 0;
    double stepVal = 0.00, old_stepVal = 0.00;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat con_time = new SimpleDateFormat("hh:mm:ss, dd/MM/yy");
    ArrayList<UserDetails> userData;
    Boolean isSuccess = false;
    Boolean loader = false;
    String cur_time, readDate;
    int stepInt, old_stepInt;
    int timecounter, timerSync;
    String user_email, steps, bikes,
            distance, user_lastname, user_name, user_password, user_pic,
            old_steps, old_bikes;
    private String sync_date, last_sync, read_date;
    private TextView lastConnect, connectStatus;
    private TextView stepView, bikeView, distanceView, mConnect;
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String contime, last_contime;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    // private BluetoothLeService mBluetoothLeService;
    private boolean mTransferStatus = false;
    private ProgressDialog progress, progressdata, bindprogress;
    private CommandManager manager;
    private LinearLayout dataView;
    private CoordinatorLayout mRoot;
    private HashMap<String, String> userSession = new HashMap<String, String>();
    private HashMap<String, String> userBike = new HashMap<String, String>();
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("UseValueOf")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                App.mConnected = true;
                App.isConnecting = false;
                mConnect.setText("Connected");
                dataRecCt = 0;
                mConnected = true;
                Snackbar.make(mRoot, "Synchronization start in less than 5sec, Please Wait!!!", Snackbar.LENGTH_SHORT).show();
                contime = con_time.format(dateTime.getTime());
                dataDB.saveLastTime(contime);
                try {
                    doAsynchronousTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (dataView.getVisibility() == View.GONE) {
                                        if (mConnected == true && data_load == true) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    manager.setSyncData(System.currentTimeMillis() - 7 * 24 * 3600 * 1000);
                                                    Log.e("BraceletData", "Called syncData");

                                                }
                                            }, 1000);

                                        }
                                    }
                                }
                            });
                        }
                    };
                    timer.schedule(doAsynchronousTask, 0, 1000);
                } catch (Exception e) {

                }

                invalidateOptionsMenu();
                //This notify the connection btw bracelt and phone when disconnect
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                App.mConnected = false;
                mConnect.setText("Disconnected");
                mConnected = false;
                App.mBluetoothLeService.close();
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // displayGattServices(BluetoothLeServiceApp.getSupportedGattServices())
                //This is when data is been collect if data is avaiable
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // try {

                last_sync = userdetails.getSynctime();
                final byte[] txValue = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("BluetoothLeService", "Received data：" + DataHandlerUtils.bytesToHexStr(txValue));

                List<Integer> datas = DataHandlerUtils.bytesToArrayList(txValue);
                ArrayList<Integer> data = new ArrayList<>();

                Log.i("zgy", datas.toString());

                for (int i = 0; i < datas.size(); i++) {
                    int ii = datas.get(i) & 0xff;
                    data.add(ii);
                }
                final StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Received data：");
                for (int i = 0; i < data.size(); i++) {
                    stringBuffer.append(data.get(i) + " ");
                }
                Log.e("BraceletData", stringBuffer.toString());
                // Toast.makeText(ConnectActivity.this, strData, Toast.LENGTH_SHORT).show();
                if (data.get(4) == 81 && data.get(5) == 8 && dataRecCt == 0) {
                    dataRecCt++;
                    sync_date = df.format(dateTime.getTime());
                    // Toast.makeText(ConnectActivity.this,strData, Toast.LENGTH_SHORT).show();
                    stepInt = (data.get(6) * 256) + (data.get(7) * 256) + data.get(8);
                    stepVal = Double.valueOf(stepInt);
                    steps = String.valueOf((int) stepVal);
                    if (steps == "0" || steps.equals("0")) {
                        steps = "00";
                    }
                    if (bikes == "0" || bikes.equals("0")) {
                        bikes = "00";
                    }
                    distance = String.valueOf(formatDis.format(stepVal * 0.0007));
                    if (steps != null && bikes != null && distance != null) {
                        UploadData(user_email, user_name, user_lastname, steps, bikes, sync_date);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dataView.setVisibility(View.VISIBLE);
                                if (steps == "00") {
                                    stepView.setText("0");
                                } else {
                                    stepView.setText(steps);
                                }
                                bikeView.setText(convert_millisec(bikes));
                                distanceView.setText(distance + " km");
                            }
                        });
                        manager.setTimeSync();
                        mTransferStatus = true;
                    }

                } else if (data.get(4) == 81 && data.get(5) == 32) {
                    try {
                        readDate = "20" + String.valueOf(data.get(6)) + "-" + String.valueOf((data.get(7) < 10 ? "0" + data.get(7) : data.get(7)))
                                + "-" + String.valueOf("" + (data.get(8) < 10 ? "0" + data.get(8) : data.get(8)));
                        new_date = readDate;
                        if (!(sync_date.equals(readDate))) {
                            isHistory = true;
                            old_stepInt = (data.get(10) * 256) + (data.get(11) * 256) + data.get(12);
                            old_stepVal = Double.valueOf(old_stepInt);
                            old_steps = String.valueOf((int) old_stepVal);
                            if (userBike.containsKey(readDate)) {
                                old_bikes = userBike.get(readDate);
                                UploadHistory(user_email, user_name, user_lastname, old_steps, old_bikes, readDate);
                            } else {
                                old_bikes = "00";
                                UploadHistory(user_email, user_name, user_lastname, old_steps, old_bikes, readDate);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        setupToolBar();
        connectStatus = findViewById(R.id.connectstaus);
        mConnect = findViewById(R.id.textConnect);
        lastConnect = findViewById(R.id.lastConnect);
        stepView = findViewById(R.id.textSteps);
        bikeView = findViewById(R.id.textTimeSpent);
        distanceView = findViewById(R.id.textDistance);
        dataView = findViewById(R.id.dataShow);
        mInstruct = findViewById(R.id.Instruction);
        mRoot = findViewById(R.id.parentRoot);
        dataView.setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        session = new SessionManager(getApplicationContext());
        dataDB = new SaveData(this);
        Intent getData = getIntent();
        goTo = false;
        isHistory = false;
        historyDate = new ArrayList<>();
        mDeviceAddress = getData.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        loader = getData.getBooleanExtra("loader", false);

        userList = new ArrayList<>();
        manager = CommandManager.getInstance(this);
        if (!session.isLoggedIn()) {
            Intent login = new Intent(ConnectActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        } else {
            userSession = session.getUserDetails();
            String log_type = userSession.get(KEY_EMAIL);
            if (log_type.contains("admin")) {
                Intent startActivityIntent = new Intent(ConnectActivity.this, MainActivity.class);
                startActivity(startActivityIntent);
                finish();
            }
        }

    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Toast.makeText(this, "on Pause", Toast.LENGTH_SHORT).show();
        if (goTo == false) {
            mBluetoothAdapter.disable();
            unregisterReceiver(mGattUpdateReceiver);
            dataView.setVisibility(View.INVISIBLE);
            mInstruct.setVisibility(View.VISIBLE);
            connectStatus.setText("");
            loader = false;
        }
        userdetails.setReload(false);
        dataDB.saveUserInfo(userdetails);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            userdetails.setReload(false);
            dataDB.saveUserInfo(userdetails);
            if (goTo == false) {
                unregisterReceiver(mGattUpdateReceiver);
            }
            if (progress != null) {
                progress.dismiss();
            }
            if (progressdata != null) {
                progressdata.dismiss();
            }
        } catch (Exception e) {

        }
    }


    private IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onResume() {
        super.onResume();
        bikes = "0";
        steps = "0";
        count = 0;
        if (loader == false) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        last_stoptime = 0;
        userBike.clear();
        sync_date = df.format(dateTime.getTime());
        cur_time = df.format(dateTime.getTime());
        read_date = df.format(dateTime.getTime());
        deviceDetails = new DeviceDetails();
        userdetails = new UserDetails();
        deviceDetails = dataDB.loadDevice();
        userdetails = dataDB.loadUserInfo();
        if (userdetails != null) {
            user_email = userdetails.getEmail();
            user_name = userdetails.getName();
            user_lastname = userdetails.getLastname();
            user_password = userdetails.getPassword();
            user_pic = userdetails.getProfilepic();
            session.createLoginSession(user_name, user_lastname, user_email, user_pic, user_password);
        }
        last_contime = dataDB.loadTime();
        if (last_contime != null && last_contime.length() > 0) {
            lastConnect.setText(last_contime);
        } else {
            lastConnect.setText("Connect to bracelet");
        }
        if (deviceDetails != null) {
            mDeviceAddress = deviceDetails.getMac();
        }
        if (userdetails.getReload() || loader) {
            Snackbar.make(mRoot, "To update sync data, please close app and open again for new sync", Snackbar.LENGTH_LONG).show();
            isSuccess = true;
            mConnect.setText("Connected");
            user_email = userdetails.getEmail();
            connectStatus.setText(getString(R.string.connected));
            dataView.setVisibility(View.VISIBLE);
            steps = userdetails.getStep();
            bikes = userdetails.getBike();
            distance = String.valueOf(formatDis.format(Double.valueOf(steps) * 0.0007));
            if (steps == "00" || steps.equals("00")) {
                stepView.setText("0");
            } else {
                stepView.setText(steps);
            }

            bikeView.setText(convert_millisec(bikes));
            distanceView.setText(distance + " km");
        } else {
            user_email = userdetails.getEmail();

            try {
                if (user_email.equals(null) || user_email.equals(" ") || user_email.length() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRoot, "user not logged in", Snackbar.LENGTH_LONG).show();
                        }
                    }, 500);
                    session.logoutUser();
                    dataDB.clear_data();
                    Intent login = new Intent(ConnectActivity.this, LoginActivity.class);
                    startActivity(login);
                    finish();
                } else {
                    get_All_User_Data(user_email);
                }
            } catch (Exception e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRoot, "user not logged in", Snackbar.LENGTH_LONG).show();
                    }
                }, 500);
                session.logoutUser();
                dataDB.clear_data();
                Intent login = new Intent(ConnectActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
            }

        }

    }

    private void setAlarm(Long TimeInMillis) {
        userdetails.setDaycount(5);
        dataDB.saveUserInfo(userdetails);
        Intent intent = new Intent(getBaseContext(), NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, TimeInMillis, pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (mConnected || loader) {
                goTo = true;
                Intent main = new Intent(ConnectActivity.this, SettingActivity.class);
                main.putExtra("activity", "mac_from_connect");
                startActivity(main);
                finish();
            } else {
                Snackbar.make(mRoot, "Device not connected, Please wait!!", Snackbar.LENGTH_LONG).show();

            }

            return true;
        } else if (id == R.id.action_account) {
            if (isSuccess == true) {
                goTo = true;
                Intent main = new Intent(ConnectActivity.this, InfoActivity.class);
                startActivity(main);
                finish();
            } else {
                if (dataView.getVisibility() == View.VISIBLE) {
                    UploadData(user_email, user_name, user_lastname, steps, bikes, sync_date);
                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, "Data sync from bracelet in progress, Please wait!!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            return true;
        } else if (id == R.id.action_bike) {
            if (isSuccess == true) {
                goTo = true;
                Intent main = new Intent(ConnectActivity.this, BikeActivity.class);
                startActivity(main);
                finish();
            } else {
                if (dataView.getVisibility() == View.VISIBLE) {
                    UploadData(user_email, user_name, user_lastname, steps, bikes, sync_date);
                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, "Data sync from bracelet in progress, Please wait!!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UploadData(String email, String name, String lastname, String step, String bike, String date) {
        mConnect.setText("Connected");
        if (doAsynchronousTask != null) {
            doAsynchronousTask.cancel();
            timer.cancel();
        }
        try {
            progress = new ProgressDialog(this);
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage("Synchronizing data...");
            progress.setCancelable(true);
            progress.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

            UserDetails user = new UserDetails();
            user.setEmail(email);
            user.setName(name);
            user.setLastname(lastname);
            user.setStep(step);
            user.setBike(bike);
            user.setDate(date);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.INSERT_DATA);
            request.setUser(user);
            Call<ServerResponse> response = requestInterfaceObject.operation(request);

            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                    ServerResponse resp = response.body();
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        isSuccess = true;
                        userdetails.setStepVal(stepVal);
                        userdetails.setStep(steps);
                        userdetails.setDistance(distance);
                        userdetails.setBike(bikes);
                        userdetails.setSynctime(sync_date);
                        userdetails.setDateFrom(sync_date);
                        userdetails.setDateTill(sync_date);
                        userdetails.setReload(true);
                        //userdetails.setDaycount(5);
                        dataDB.saveUserInfo(userdetails);
                        connectStatus.setText(getString(R.string.connected));
                        dataView.setVisibility(View.VISIBLE);
                        show_time();
                        // Snackbar.make(mRoot, "Please wait to synchronize history..", Snackbar.LENGTH_SHORT).show();
                        setAlarm(System.currentTimeMillis() + 6 * 24 * 3600 * 1000);
                        mInstruct.setVisibility(View.GONE);
                        //setAlarm(System.currentTimeMillis() + 120 * 1000);
                        doProgressSync = new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        timerSync++;
                                        if (timerSync >= 50) {
                                            if (!isHistory) {
                                                Snackbar.make(mRoot, "Today data sync successfully", Snackbar.LENGTH_SHORT).show();
                                                progress.setMessage("Today data sync successfully...");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progress.dismiss();
                                                    }
                                                }, 1000);
                                                timerSync = 0;
                                            } else {

                                            }
                                            doProgressSync.cancel();
                                            timeSync.cancel();

                                        }


                                    }
                                });
                            }
                        };
                        timeSync.schedule(doProgressSync, 0, 100);
                    } else {
                        isSuccess = false;
                        Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
                        doProgressSync = new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        timerSync++;
                                        if (timerSync >= 50) {
                                            if (!isHistory) {
                                                progress.setMessage("Synchronization is not  successfully...");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progress.dismiss();
                                                    }
                                                }, 1000);
                                                timerSync = 0;
                                            }
                                            doProgressSync.cancel();
                                            timeSync.cancel();

                                        }

                                    }
                                });
                            }
                        };
                        timeSync.schedule(doProgressSync, 0, 100);
                    }


                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Log.d(Constants.TAG, t.getMessage());
                    if (progress != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        }, Wait_delay);
                    }
                    Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(ConnectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {

        }

    }

    private void UploadHistory(String email, String name, String lastname, String step, String bike, String date) {
        timerSync = 0;
        if (isSuccess && count == 0) {
            count++;
            Snackbar.make(mRoot, "Today data sync successfully", Snackbar.LENGTH_SHORT).show();
            progress.setMessage("Today data sync successfully...");
        } else {
            if (count == 0) {
                count++;
                progress.setMessage("Synchronization is not  successfully...");
            }

        }
        progress.setMessage("Synchronizing history for " + date + "\nTaking too long? tap to cancel");
        doProgressTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timecounter++;
                        if (timecounter >= 200) {
                            progress.setMessage("History synchronization successful");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                }
                            }, 1000);
                            doProgressTask.cancel();
                            timerProg.cancel();
                        }
                    }
                });
            }
        };
        timerProg.schedule(doProgressTask, 0, 100);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);
        UserDetails user = new UserDetails();
        user.setEmail(email);
        user.setName(name);
        user.setLastname(lastname);
        user.setStep(step);
        user.setBike(bike);
        user.setDate(date);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.INSERT_DATA);
        request.setUser(user);
        Call<ServerResponse> response = requestInterfaceObject.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)) {
                }
                timecounter = 0;
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.d(Constants.TAG, t.getMessage());
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                progress.dismiss();
                timecounter = 0;
            }
        });

       /* } catch (Exception e) {

        }*/
    }

    public String convert_millisec(String millis) {
        long milliseconds = Long.valueOf(millis);
        String bike_data;
        int minutes = (int) (((milliseconds / 1000) / 60) % 60);
        int hours = (int) (((milliseconds / 1000) / 3600) % 24);
        bike_data = String.valueOf(hours < 10 ? "0" + hours : hours) + "hr:" + String.valueOf("" + (minutes < 10 ? "0" + minutes : minutes)) + "min";
        return bike_data;
    }

    public void show_time() {
        try {
            if (last_contime.equals(contime)) {
                lastConnect.setText("seconds ago");
            } else {
                if (last_contime != null) {
                    if (last_contime.substring(9).equals(contime.substring(9))) {
                        if (last_contime.substring(0, 1).equals(contime.substring(0, 1))) {
                            if (last_contime.substring(3, 4).equals(contime.substring(3, 4))) {
                                if (last_contime.substring(6, 7).equals(contime.substring(6, 7))) {
                                    lastConnect.setText("seconds ago");
                                } else {
                                    int secLast = Integer.valueOf(last_contime.substring(6, 7));
                                    int secCon = Integer.valueOf(contime.substring(6, 7));
                                    int diff = abs(secCon - secLast);
                                    lastConnect.setText(String.valueOf(diff) + " seconds ago");
                                }
                            } else {
                                int minLast = Integer.valueOf(last_contime.substring(3, 4));
                                int minCon = Integer.valueOf(contime.substring(3, 4));
                                int diff = abs(minCon - minLast);
                                lastConnect.setText(String.valueOf(diff) + " minute ago");
                            }
                        } else {
                            int hrLast = Integer.valueOf(last_contime.substring(0, 1));
                            int hrCon = Integer.valueOf(contime.substring(0, 1));
                            int diff = abs(hrCon - hrLast);
                            lastConnect.setText(String.valueOf(diff) + " hour ago");
                        }
                    } else {
                        // "hh:mm:ss, dd/MM/yyyy"
                        if (last_contime.substring(15).equals(contime.substring(15))) {
                            if (last_contime.substring(13, 14).equals(contime.substring(13, 14))) {
                                if (last_contime.substring(10, 11).equals(contime.substring(10, 11))) {

                                } else {
                                    int ddLast = Integer.valueOf(last_contime.substring(10, 11));
                                    int ddCon = Integer.valueOf(contime.substring(10, 11));
                                    int diff = abs(ddCon - ddLast);
                                    lastConnect.setText(String.valueOf(diff) + " Days ago");
                                }
                            }
                        } else {
                            lastConnect.setText("made " + last_contime);
                        }
                    }
                } else {
                    lastConnect.setText("seconds ago");
                }
            }

        } catch (Exception e) {
            lastConnect.setText("seconds ago");
        }
    }

    private void get_All_User_Data(final String email) {

        progressdata = new ProgressDialog(this);
        progressdata.setIndeterminate(true);
        progressdata.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressdata.setMessage("loading user data...");
        progressdata.setCancelable(false);
        progressdata.show();

        mConnect.setText("Connecting");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceArray requestInterface = retrofit.create(RequestInterfaceArray.class);
        UserDetails user = new UserDetails();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_TOTAL_USER);
        request.setUser(user);
        Call<UserList> response = requestInterface.operation(request);
        response.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, retrofit2.Response<UserList> response) {
                UserList resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                data_load = true;
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    userData = resp.getUsers();
                    for (int i = 0; i < userData.size(); i++) {
                        userBike.put(userData.get(i).getDate(), userData.get(i).getBike());
                    }
                    if (sync_date.equals(userData.get(0).getDate())) {
                        last_stoptime = last_stoptime + Long.valueOf(userData.get(0).getBike());
                    } else {
                        last_stoptime = 1;
                    }

                    progressdata.setMessage("User information retrieved...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRoot, "User information retrieved...", Snackbar.LENGTH_SHORT).show();
                        }
                    }, Wait_delay);
                    data_load = true;
                    bikes = String.valueOf(last_stoptime);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressdata != null) {
                                progressdata.dismiss();
                            }
                        }
                    }, Wait_delay);

                    checkBind(email, mDeviceAddress);

                } else {
                    if (resp.getMessage().contains("No data available")) {
                        Snackbar.make(mRoot, "No data available on database", Snackbar.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressdata != null) {
                                    progressdata.dismiss();
                                }
                            }
                        }, Wait_delay);
                        checkBind(email, mDeviceAddress);
                        data_load = true;
                    } else if (resp.getMessage().contains("User not registered")) {
                        Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressdata != null) {
                                    progressdata.dismiss();
                                }
                            }
                        }, Wait_delay);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mRoot, "user not logged in", Snackbar.LENGTH_LONG).show();
                            }
                        }, 500);
                        session.logoutUser();
                        dataDB.clear_data();
                        Intent login = new Intent(ConnectActivity.this, LoginActivity.class);
                        startActivity(login);
                        finish();

                    }
                }
            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                // handleSignInResult(null);
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(ConnectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(Constants.TAG, t.getMessage());
                Log.d(Constants.TAG, t.getLocalizedMessage());
                if (progressdata != null) {
                    progressdata.dismiss();
                }

            }
        });
    }

    private void checkBind(final String email, final String macaddress) {
        try {
            bindprogress = new ProgressDialog(this);
            bindprogress.setIndeterminate(true);
            bindprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            bindprogress.setMessage("Checking band device connection..");
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
            request.setOperation(Constants.CHECK_BIND_DEVICE);
            request.setUser(user);
            Call<ServerResponse> response = requestInterfaceObject.operation(request);

            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                    ServerResponse resp = response.body();
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        bindprogress.setMessage("Device band connection successful");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bindprogress != null) {
                                    bindprogress.dismiss();
                                }
                            }
                        }, 200);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mRoot, "Connect in less 10 sec please wait...", Snackbar.LENGTH_SHORT).show();
                            }
                        }, 300);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Toast.makeText(ConnectActivity.this, mDeviceAddress, Toast.LENGTH_SHORT).show();
                                App.mBluetoothLeService.connect(mDeviceAddress, false);
                                App.isConnecting = true;
                            }
                        }, 500);


                    } else if (userdetails.getIsBind()) {
                        bindprogress.setMessage("Device band connection successful");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bindprogress != null) {
                                    bindprogress.dismiss();
                                }
                            }
                        }, 200);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mRoot, "Connect in less 10 sec please wait...", Snackbar.LENGTH_SHORT).show();
                            }
                        }, 300);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Toast.makeText(ConnectActivity.this, mDeviceAddress, Toast.LENGTH_SHORT).show();
                                App.mBluetoothLeService.connect(mDeviceAddress, false);
                                App.isConnecting = true;
                                mConnect.setText("Connecting");
                            }
                        }, 500);


                    } else {
                        bindprogress.setMessage("Device not bind ");
                        Snackbar.make(mRoot, "Please bind device to bracelet", Snackbar.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bindprogress != null) {
                                    bindprogress.dismiss();
                                }

                            }
                        }, Wait_delay);
                        Intent bind = new Intent(ConnectActivity.this, BindActivity.class);
                        startActivity(bind);
                        finish();
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            loader = false;
            userdetails.setReload(false);
            dataDB.saveUserInfo(userdetails);
            mBluetoothAdapter.disable();
            Intent exit = new Intent(Intent.ACTION_MAIN);
            exit.addCategory(Intent.CATEGORY_HOME);
            exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exit);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

