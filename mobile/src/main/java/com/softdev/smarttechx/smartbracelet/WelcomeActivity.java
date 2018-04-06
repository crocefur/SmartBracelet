package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;
import com.softdev.smarttechx.smartbracelet.util.Stopwatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private final int delay = 1000;
    SaveData dataDB;
    UserDetails userdetails;
    Stopwatch stop_watch;
    Timer timer;
    long last_stoptime;
    Calendar dateTime = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    boolean gpsStatus;
    private Button mStart;
    private CoordinatorLayout mRoot;
    private TextView mStopwatch;
    private String sync_time, last_sync = null;
    private ProgressDialog progress, progress_save;
    private String user_email = null, steps = null, bikes = null, user_lastname = null, user_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mStart = findViewById(R.id.butBike);
        mStopwatch = findViewById(R.id.stopWatch);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        mStart.setOnClickListener(this);
        dataDB = new SaveData(this);
        mRoot = findViewById(R.id.parentRoot);
    }

    @Override
    public void onResume() {
        super.onResume();
        mStart.setEnabled(false);
        last_stoptime = 0;
        userdetails = new UserDetails();
        userdetails = dataDB.loadUserInfo();
        sync_time = df.format(dateTime.getTime());
        if (userdetails != null) {
            user_email = userdetails.getEmail();
            last_sync = userdetails.getSynctime();
            user_name = userdetails.getName();
            user_lastname = userdetails.getLastname();
            bikes = userdetails.getBike();
            steps = userdetails.getStep();
            if (steps == "0") {
                steps = "1";
            }
        }
        if (sync_time.equals(last_sync)) {
            get_User_Data(user_email, last_sync);
        } else {
            mStart.setEnabled(true);
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mStart) {
            if (mStart.getText().toString().equals("Start")) {
                stop_watch = new Stopwatch();
                mStart.setText(R.string.stop);
                Snackbar snackbar = Snackbar.make(mRoot, "Bike start in less than 2 seconds", Snackbar.LENGTH_LONG);
                snackbar.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop_watch.start();
                    }
                }, delay);
                final Handler handler = new Handler();
                timer = new Timer();
                TimerTask doAsynchronousTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setTime(stop_watch);
                            }
                        });
                    }
                };
                timer.schedule(doAsynchronousTask, 0, 1000);

            } else {
                mStart.setText(R.string.start);
                stop_watch.stop();
                timer.cancel();
                setTime(stop_watch);
                getBike(stop_watch);

            }

        }
    }

    private void SaveData(String email, String name, String lastname, String step, String bike, String date) {
        try {
            progress_save = new ProgressDialog(this);
            progress_save.setIndeterminate(true);
            progress_save.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress_save.setMessage("Saving data...");
            progress_save.setCancelable(true);
            progress_save.show();
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
                        progress_save.setMessage("Data saved successfully...");
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.show();

                    } else {
                        progress_save.setMessage("Data saving not  successfully...");
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                    if (progress_save != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress_save.dismiss();
                            }
                        }, 500);
                    }

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Log.d(Constants.TAG, "failed");
                    if (progress_save != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress_save.dismiss();
                            }
                        }, 500);
                    }
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            });
        } catch (Exception e) {

        }

    }

    public void getBike(Stopwatch stopwatch) {
        long milliseconds = stopwatch.getElapsedTime();
        long total_millis = last_stoptime + milliseconds;
        bikes = String.valueOf(total_millis);
        if (steps.equals("0")) {
            steps = "1";
        }
        SaveData(user_email, user_name, user_lastname, steps, bikes, last_sync);
        userdetails.setBike(bikes);
        dataDB.saveUserInfo(userdetails);
    }

    public void setTime(Stopwatch stopwatch) {
        String bikeTime;
        long milliseconds = stopwatch.getElapsedTime();
        int seconds = (int) ((milliseconds / 1000) % 60);
        int minutes = (int) (((milliseconds / 1000) / 60) % 60);
        int hours = (int) (((milliseconds / 1000) / 3600) % 24);
        bikeTime = String.valueOf(hours < 10 ? "0" + hours : hours) + ":" + String.valueOf("" + (minutes < 10 ? "0" + minutes : minutes) +
                ":" + String.valueOf("" + (seconds < 10 ? "0" + seconds : seconds)));
        mStopwatch.setText(bikeTime);

    }

    private void get_User_Data(String email, String date) {
        try {
            last_stoptime = 0;
            progress = new ProgressDialog(this);
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage("Loading bike data...");
            progress.setCancelable(true);
            progress.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            UserDetails user = new UserDetails();
            user.setEmail(email);
            user.setDate(date);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.GET_USER_DATA);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        last_stoptime = Long.valueOf(resp.getUser().getBike());
                    } else {

                        last_stoptime = Long.valueOf(bikes);

                    }
                    progress.setMessage("Done..");
                    mStart.setEnabled(true);
                    if (progress != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        }, 100);
                    }

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Log.d(Constants.TAG, "failed");
                    Snackbar snackbar = Snackbar.make(mRoot, t.getMessage() + " Please reload!!!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    if (progress != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        }, 100);
                    }
                    mStart.setEnabled(true);
                }
            });
        } catch (Exception e) {

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent main = new Intent(WelcomeActivity.this, ConnectActivity.class);
                startActivity(main);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Intent main = new Intent(WelcomeActivity.this, ConnectActivity.class);
        startActivity(main);
        finish();
        super.onBackPressed();

    }


}
