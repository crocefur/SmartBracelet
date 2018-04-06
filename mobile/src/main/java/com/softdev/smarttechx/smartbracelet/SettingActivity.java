package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.command.CommandManager;
import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.service.BluetoothLeService;
import com.softdev.smarttechx.smartbracelet.util.App;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.DataHandlerUtils;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    final Handler handler = new Handler();
    String activityFrom;
    SaveData dataDB;
    UserDetails userdetails;
    String mac_address, mEmail, mBandname, mBa3;
    Button mClearData, mResetBand, mUnbindBand, mExit, mShutD, mFind;
    SessionManager session;
    TextView bandBattery;
    private CommandManager manager;
    private boolean isBind = true;
    private ProgressDialog progressUnbind;
    private CoordinatorLayout mRoot;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ProgressDialog bindprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(R.string.title_activity_setting);

        mFind = findViewById(R.id.butFind);
        mShutD = findViewById(R.id.butShutdown);
        mClearData = findViewById(R.id.butClear);
        mResetBand = findViewById(R.id.butReset);
        mUnbindBand = findViewById(R.id.butUnbind);
        mExit = findViewById(R.id.butExit);
        mClearData.setOnClickListener(this);
        mResetBand.setOnClickListener(this);
        mUnbindBand.setOnClickListener(this);
        mExit.setOnClickListener(this);
        mFind.setOnClickListener(this);
        mShutD.setOnClickListener(this);
        manager = CommandManager.getInstance(this);
        isBind = true;
        Intent getFrom = getIntent();
        activityFrom = getFrom.getStringExtra("activity");
        dataDB = new SaveData(this);
        userdetails = new UserDetails();
        mRoot = findViewById(R.id.parentRoot);
        userdetails = dataDB.loadUserInfo();
        mac_address = userdetails.getMacaddress();
        mBandname = userdetails.getBandname();
        mEmail = userdetails.getEmail();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        session = new SessionManager(getApplicationContext());
    }

    public void onClick(View v) {
        if (v == mClearData) {
            clearData();
        } else if (v == mResetBand) {
            factoryReset();
        } else if (v == mFind) {
            manager.findBand();
        } else if (v == mShutD) {
            shutdownBand();
        } else if (v == mUnbindBand) {
            if (mUnbindBand.getText().toString().equals(R.string.unbind_device) || isBind) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to unbind bracelet from device?");
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Unbind(mEmail, mac_address);

                    }
                });
                builder.show();
            } else if (mUnbindBand.getText().toString().equals(R.string.bind_device) || !isBind) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to bind bracelet from device?");
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BindDevice(mEmail, mac_address);

                    }
                });
                builder.show();

            }


        } else if (v == mExit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("Are you sure you want exit application?");
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBluetoothAdapter.disable();
                    finish();
                    Intent exit = new Intent(Intent.ACTION_MAIN);
                    exit.addCategory(Intent.CATEGORY_HOME);
                    exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(exit);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);

                }
            });
            builder.show();

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            if (activityFrom.equals("mac_from_info")) {
                Intent main = new Intent(SettingActivity.this, InfoActivity.class);
                startActivity(main);
                finish();
                return true;

            } else if (activityFrom.equals("mac_from_connect")) {
                Intent main = new Intent(SettingActivity.this, ConnectActivity.class);
                main.putExtra("loader", true);
                startActivity(main);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("zgy", "onResume");
        // bandBattery.setText("Battery: "+ manager.getBatteryInfo(););

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (activityFrom.equals("mac_from_info")) {
                Intent main = new Intent(SettingActivity.this, InfoActivity.class);
                startActivity(main);
                finish();
                return true;

            } else if (activityFrom.equals("mac_from_connect")) {
                Intent main = new Intent(SettingActivity.this, ConnectActivity.class);
                main.putExtra("loader", true);
                startActivity(main);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void shutdownBand() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("Are you sure you want to shut down the band?");
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                manager.Shutdown();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRoot, "Band Shutdown", Snackbar.LENGTH_SHORT).show();
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent exit = new Intent(Intent.ACTION_MAIN);
                        exit.addCategory(Intent.CATEGORY_HOME);
                        exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(exit);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                }, 200);

            }
        });
        builder.show();


    }

    public void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("Are you sure you want to clear the data?");
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                manager.setClearData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRoot, "Bracelet data cleared", Snackbar.LENGTH_SHORT).show();
                    }
                }, 200);
            }
        });
        builder.show();


    }

    public void factoryReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("Are you sure you want to restore the factory settings?");
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // manager.setClearData();
                manager.setResetBand();
                // manager.Shutdown();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRoot, "Bracelet reset successfully", Snackbar.LENGTH_SHORT).show();
                    }
                }, 200);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent exit = new Intent(Intent.ACTION_MAIN);
                        exit.addCategory(Intent.CATEGORY_HOME);
                        exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        exit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(exit);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                }, 500);


            }
        });
        builder.show();

    }

    private void Unbind(String email, String mac_address) {
        progressUnbind = new ProgressDialog(this);
        progressUnbind.setIndeterminate(true);
        progressUnbind.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressUnbind.setMessage("Unbinding device ...");
        progressUnbind.setCancelable(false);
        progressUnbind.show();
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            final UserDetails user = new UserDetails();
            user.setEmail(email);
            user.setMacaddress(mac_address);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.UNBIND_DEVICE);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        Snackbar.make(mRoot, "Device unbind successfully", Snackbar.LENGTH_SHORT).show();
                        progressUnbind.setMessage("Device unbind successfully");
                        if (progressUnbind != null) {
                            progressUnbind.dismiss();

                        }
                        userdetails = new UserDetails();
                        userdetails = dataDB.loadUserInfo();
                        userdetails.setIsBind(false);
                        dataDB.saveUserInfo(userdetails);
                        mUnbindBand.setText(R.string.bind_device);
                        isBind = false;
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        progressUnbind.dismiss();
                    }
                    Toast.makeText(SettingActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    Log.d(Constants.TAG, t.getMessage());
                    progressUnbind.dismiss();
                }
            });
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
            snackbar.show();
            progressUnbind.dismiss();
        }

    }

    private void BindDevice(final String email, final String macaddress) {
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
                        Snackbar.make(mRoot, "Device bind to " + mBandname, Snackbar.LENGTH_SHORT).show();
                        bindprogress.setMessage("Bind successful...");
                        mUnbindBand.setText(R.string.unbind_device);
                        isBind = true;
                        userdetails = new UserDetails();
                        userdetails = dataDB.loadUserInfo();
                        userdetails.setBandname(mBandname);
                        dataDB.saveUserInfo(userdetails);
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
                                Snackbar.make(mRoot, "Device bind to " + mBandname, Snackbar.LENGTH_SHORT).show();
                                bindprogress.setMessage("Bind successful...");
                                mUnbindBand.setText(R.string.unbind_device);
                                userdetails = new UserDetails();
                                userdetails = dataDB.loadUserInfo();
                                userdetails.setIsBind(true);
                                userdetails.setBandname(mBandname);
                                dataDB.saveUserInfo(userdetails);
                                isBind = true;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bindprogress != null) {
                                            bindprogress.dismiss();
                                        }
                                    }
                                }, 500);
                            } else {
                                mUnbindBand.setText(R.string.bind_device);
                                isBind = false;
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

}
