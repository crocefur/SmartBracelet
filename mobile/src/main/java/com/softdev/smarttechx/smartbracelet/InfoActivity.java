package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.UserList;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceArray;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    final Handler handler = new Handler();
    UserDetails user;
    SessionManager session;
    SaveData userDB;
    ArrayList<UserDetails> userList;
    int totalSteps;
    long totalBikes;
    LinearLayoutManager llm;
    boolean isSuccess = false;
    SwipeRefreshLayout mRefreshLayout;
    Timer timer = new Timer();
    TimerTask doAsynchronousTask;
    String mDeviceAddress;
    private Button chgpwd, delUser, back, unBind;
    private TextView txtname, txtlastname, txtemail, txtlogin_type, txtlastStep, txtlastBike, txttotalStep, txttotalBike,
            txtlastconnect, txtnameDisplay;
    private String name, lastname, user_email, login_type, lastStep, lastBike, totalStep, totalBike,
            lastconnect, nameDisplay, password = null, macaddress;
    private LinearLayout account;
    private ProgressDialog progressdata, progressDel, progressUnbind, changeProgress, updateProgress;
    private CoordinatorLayout mRoot;
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        chgpwd = findViewById(R.id.butChgpwd);
        delUser = findViewById(R.id.butDelete);
        back = findViewById(R.id.butBack);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        chgpwd.setOnClickListener(this);
        back.setOnClickListener(this);
        delUser.setOnClickListener(this);
        account = findViewById(R.id.account);
        mRoot = findViewById(R.id.parentRoot);
        account.setVisibility(View.GONE);
        txtname = findViewById(R.id.textName);
        session = new SessionManager(getApplicationContext());
        txtlastname = findViewById(R.id.textLastname);
        txtemail = findViewById(R.id.textEmail);
        txtlogin_type = findViewById(R.id.textLogin);
        txtlastStep = findViewById(R.id.textLaststep);
        txtlastBike = findViewById(R.id.textLastbike);
        txttotalStep = findViewById(R.id.textTotalstep);
        txttotalBike = findViewById(R.id.textTotalbike);
        txtlastconnect = findViewById(R.id.textLastlogin);
        txtnameDisplay = findViewById(R.id.textUsername);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        userList = new ArrayList<>();
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                account.setVisibility(View.VISIBLE);
                login_type = user.getLoginType();
                get_User(user_email);

                password = user.getPassword();
            }
        });
        userDB = new SaveData(this);
        user = new UserDetails();
        user = userDB.loadUserInfo();
        user.setReload(true);
        userDB.saveUserInfo(user);
        if (!session.isLoggedIn()) {
            Intent login = new Intent(InfoActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }
        Intent infoMac = getIntent();
        mDeviceAddress = infoMac.getStringExtra("mac_from_connect");
    }

    @Override
    public void onClick(View v) {
        if (v == chgpwd) {
            Changepwd();
        } else if (v == delUser) {
            DeleteUser(user_email);
        } else if (v == back) {
            user.setReload(true);
            userDB.saveUserInfo(user);
            Intent main = new Intent(InfoActivity.this, ConnectActivity.class);
            main.putExtra("loader", true);
            startActivity(main);
            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!session.isLoggedIn()) {
            Intent login = new Intent(InfoActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }

        if (user != null) {
            name = user.getName();
            lastname = user.getLastname();
            user_email = user.getEmail();
            lastStep = user.getStep();
            macaddress = user.getMacaddress();
            if (lastStep.equals("1")) {
                lastStep = "0";
            }
            String bike_in_mills = user.getBike();
            lastBike = convert_millisec(bike_in_mills);
            nameDisplay = name + " " + lastname;
            account.setVisibility(View.VISIBLE);
            login_type = user.getLoginType();
            get_User(user_email);
            password = user.getPassword();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        user.setReload(true);
        userDB.saveUserInfo(user);
    }

    public void Changepwd() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.changepass, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText old_pass = promptsView
                .findViewById(R.id.old_password);
        final EditText new_pass = promptsView
                .findViewById(R.id.new_password);
        final EditText new_passagain = promptsView
                .findViewById(R.id.new_passwordagain);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (old_pass.getText().toString().isEmpty() || new_pass.getText().toString().isEmpty() || new_passagain.getText().toString().isEmpty()) {
                                    Toast.makeText(InfoActivity.this, "Field cant be empty", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (new_pass.getText().toString().equals(new_passagain.getText().toString())) {
                                        Change_pwd(user_email, old_pass.getText().toString().trim(), new_pass.getText().toString().trim());
                                        user.setPassword(new_pass.getText().toString());
                                        userDB.saveUserInfo(user);
                                    } else {
                                        Snackbar snackbar = Snackbar.make(mRoot, "New password not the same", Snackbar.LENGTH_SHORT);
                                        snackbar.show();
                                    }

                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void update_profile() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.updateprofile, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText first_name = promptsView
                .findViewById(R.id.first_name);
        final EditText last_name = promptsView
                .findViewById(R.id.last_name);

        first_name.setText(name);
        last_name.setText(lastname);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (first_name.getText().toString().isEmpty() || last_name.getText().toString().isEmpty()) {
                                    Toast.makeText(InfoActivity.this, "Field cant be empty", Toast.LENGTH_SHORT).show();
                                } else {
                                    updateprofile(user_email, first_name.getText().toString(), last_name.getText().toString());
                                    name = first_name.getText().toString();
                                    lastname = last_name.getText().toString();
                                    nameDisplay = name + " " + lastname;
                                    txtname.setText("Name: " + upperCaseAllFirst(name));
                                    txtnameDisplay.setText(upperCaseAllFirst(nameDisplay));
                                    txtlastname.setText("Surname: " + upperCaseAllFirst(lastname));
                                    user.setName(first_name.getText().toString());
                                    user.setLastname(last_name.getText().toString());
                                    userDB.saveUserInfo(user);
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void updateprofile(String email, String firstname, String lastname) {
        updateProgress = new ProgressDialog(this);
        updateProgress.setIndeterminate(true);
        updateProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        updateProgress.setMessage("updating profile ...");
        updateProgress.setCancelable(false);
        updateProgress.show();
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            final UserDetails user = new UserDetails();
            user.setEmail(email);
            user.setName(firstname);
            user.setLastname(lastname);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.UPDATE_PROFILE);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    if (resp.getResult().equals(Constants.SUCCESS)) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateProgress.setMessage("Profile update successfully ...");

                            }
                        }, 100);

                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        updateProgress.setMessage(resp.getMessage());
                    }
                    if (updateProgress != null) {
                        updateProgress.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    Log.d(Constants.TAG, "failed");
                    if (updateProgress != null) {
                        updateProgress.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            Log.d(Constants.TAG, "failed");
            if (updateProgress != null) {
                updateProgress.dismiss();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info_menu, menu);
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
            Intent main = new Intent(InfoActivity.this, SettingActivity.class);
            main.putExtra("activity", "mac_from_info");
            startActivity(main);
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.disable();
            }
            userDB.clear_data();
            session.logoutUser();
            this.finish();
            return true;
        } else if (id == android.R.id.home) {
            Intent main = new Intent(InfoActivity.this, ConnectActivity.class);
            main.putExtra("loader", true);
            startActivity(main);
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            update_profile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void get_All_User_Data(String email) {
        try {
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
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        userList = resp.getUsers();
                        progressdata.setMessage("Synchronization done...");
                        Snackbar snackbar = Snackbar.make(mRoot, "Synchronization  done...", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        loadData();
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, "Error loading data...", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    if (progressdata != null) {
                        progressdata.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<UserList> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                    Log.d(Constants.TAG, t.getMessage());

                    if (progressdata != null) {
                        progressdata.dismiss();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private void DeleteUser(String email) {
        progressDel = new ProgressDialog(this);
        progressDel.setIndeterminate(true);
        progressDel.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDel.setMessage("Deleting user ...");
        progressDel.setCancelable(false);
        progressDel.show();
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            UserDetails user = new UserDetails();
            user.setEmail(email);

            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.DELETE_USER);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        Snackbar snackbar = Snackbar.make(mRoot, "Account deleted", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                    DeleteUserData(user_email);

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    DeleteUserData(user_email);
                    Log.d(Constants.TAG, "failed");
                }
            });
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
            snackbar.show();
            DeleteUserData(user_email);
        }

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
            UserDetails user = new UserDetails();
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
                        Snackbar snackbar = Snackbar.make(mRoot, "Device unbind successfully", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        progressUnbind.setMessage("Device unbind successfully");
                        if (progressUnbind != null) {
                            progressUnbind.dismiss();

                        }
                        if (mBluetoothAdapter != null) {
                            mBluetoothAdapter.disable();
                        }
                        session.logoutUser();
                        userDB.clear_data();
                        Intent login = new Intent(InfoActivity.this, LoginActivity.class);
                        startActivity(login);
                        finish();
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        progressUnbind.dismiss();
                    }
                    Toast.makeText(InfoActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    Log.d(Constants.TAG, t.getMessage());
                    Toast.makeText(InfoActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    progressUnbind.dismiss();
                }
            });
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
            snackbar.show();
            progressUnbind.dismiss();
        }

    }

    private void DeleteUserData(String email) {
        progressDel.setMessage("Deleting user data...");
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            UserDetails user = new UserDetails();
            user.setEmail(email);

            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.DELETE_DATA);
            request.setUser(user);
            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    if (resp.getResult().equals(Constants.SUCCESS)) {
                        progressDel.setMessage("User data cleared successfully...");
                        Snackbar snackbar = Snackbar.make(mRoot, "User data deleted", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        progressDel.setMessage(resp.getMessage());
                        Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                    progressDel.setMessage("User delete successfully...");
                    if (progressDel != null) {
                        progressDel.dismiss();

                    }
                    Unbind(user_email, macaddress);

                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    Log.d(Constants.TAG, "failed");
                    if (progressDel != null) {
                        progressDel.dismiss();
                    }

                }
            });
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            Log.d(Constants.TAG, "failed");
            if (progressDel != null) {
                progressDel.dismiss();
            }
        }

    }

    private void Change_pwd(String email, String old_password, final String new_password) {
        changeProgress = new ProgressDialog(this);
        changeProgress.setIndeterminate(true);
        changeProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        changeProgress.setMessage("Changing user password ...");
        changeProgress.setCancelable(false);
        changeProgress.show();
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
            UserDetails user = new UserDetails();
            user.setEmail(email);
            user.setOldPassword(old_password);
            user.setNewPassword(new_password);
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.CHANGE_PASSWORD_OPERATION);
            request.setUser(user);

            Call<ServerResponse> response = requestInterface.operation(request);
            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                    ServerResponse resp = response.body();
                    Log.d(Constants.TAG, resp.getMessage());
                    // Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
                    if (resp.getResult().equals(Constants.SUCCESS)) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeProgress.setMessage("Password change successfully ...");

                            }
                        }, 200);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mRoot, "Password change successfully, Please check your email for confirmation ...", Snackbar.LENGTH_LONG).show();

                            }
                        }, 500);

                        Toast.makeText(InfoActivity.this, "Password change successfully Please check your email for confirmation ",
                                Toast.LENGTH_SHORT).show();
                        if (changeProgress != null) {
                            changeProgress.dismiss();
                        }
                        Unbind(user_email, macaddress);

                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeProgress.setMessage("Wrong Password");

                            }
                        }, 500);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mRoot, "Wrong Password entered", Snackbar.LENGTH_LONG).show();

                            }
                        }, 500);

                        if (changeProgress != null) {
                            changeProgress.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    // handleSignInResult(null);
                    Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT).show();
                    Log.d(Constants.TAG, t.getMessage());
                    if (changeProgress != null) {
                        changeProgress.dismiss();
                    }

                }
            });
        } catch (Exception e) {
            Snackbar.make(mRoot, "Error occur,, please try again!!!", Snackbar.LENGTH_SHORT).show();
            //Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
            Log.d(Constants.TAG, "failed");
            if (changeProgress != null) {
                changeProgress.dismiss();
            }
        }

    }

    private void get_User(String email) {
        progressdata = new ProgressDialog(this);
        progressdata.setIndeterminate(true);
        progressdata.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressdata.setMessage("Synchronizing user data...");
        progressdata.setCancelable(false);
        progressdata.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
        UserDetails user = new UserDetails();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_USER);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    name = (resp.getUser().getName());
                    lastname = (resp.getUser().getLastname());
                    nameDisplay = (resp.getUser().getName() + " " + resp.getUser().getLastname());
                    get_All_User_Data(user_email);
                } else {

                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                Toast.makeText(InfoActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d(Constants.TAG, t.getLocalizedMessage());
                if (progressdata != null) {
                    progressdata.dismiss();
                }

            }
        });
    }


    public void loadData() {
        try {
            totalSteps = 0;
            totalBikes = 0;
            for (int i = 0; i < userList.size(); i++) {
                totalSteps = totalSteps + Integer.valueOf(userList.get(i).getStep());
                totalBikes = totalBikes + Long.valueOf(userList.get(i).getBike());
            }
            lastStep = userList.get(0).getStep();
            lastBike = userList.get(0).getBike();
            if (lastStep.equals("00"))
                lastStep = "0";
            if (lastBike.equals("00"))
                lastBike = "0";
            lastconnect = userList.get(0).getDate();
            totalStep = String.valueOf(totalSteps);
            totalBike = convert_millisec(String.valueOf(totalBikes));
            txtname.setText("Name: " + upperCaseAllFirst(name));
            txtnameDisplay.setText(upperCaseAllFirst(nameDisplay));
            txtlastname.setText("Surname: " + upperCaseAllFirst(lastname));
            txtemail.setText("Email: " + user_email);
            txtlogin_type.setText("Login with: " + login_type);
            txtlastStep.setText("Last update step: " + lastStep);
            txtlastBike.setText("Last update bike: " + convert_millisec(lastBike));
            txtlastconnect.setText("Last connection: " + lastconnect);
            txttotalStep.setText("Total step: " + totalStep);
            txttotalBike.setText("Total bike: " + totalBike);
            mRefreshLayout.setRefreshing(false);
        } catch (Exception e) {

        }
    }

    public String convert_millisec(String millis) {
        long milliseconds = Long.valueOf(millis);
        String bike_data;
        int minutes = (int) (((milliseconds / 1000) / 60) % 60);
        int hours = (int) (((milliseconds / 1000) / 3600) % 24);
        bike_data = String.valueOf(hours < 10 ? "0" + hours : hours) + "hr:" + String.valueOf("" + (minutes < 10 ? "0" + minutes : minutes)) + "min";
        return bike_data;
    }

    public String upperCaseAllFirst(String value) {

        char[] array = value.toCharArray();
        // Uppercase first letter.
        array[0] = Character.toUpperCase(array[0]);

        // Uppercase all letters that follow a whitespace character.
        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }

        // Result.
        return new String(array);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            user.setReload(true);
            userDB.saveUserInfo(user);
            Intent main = new Intent(InfoActivity.this, ConnectActivity.class);
            main.putExtra("loader", true);
            startActivity(main);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
