package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.adapter.DataAdapter;
import com.softdev.smarttechx.smartbracelet.adapter.ItemClickListener;
import com.softdev.smarttechx.smartbracelet.adapter.UserAdapter;
import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.UserList;
import com.softdev.smarttechx.smartbracelet.model.Userdata;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceArray;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener {
    UserDetails user;
    SessionManager session;
    SaveData userDB;
    ArrayList<UserDetails> userList;
    ArrayList<UserDetails> allUser;
    int totalSteps;
    long totalBikes;
    LinearLayoutManager llm;
    SwipeRefreshLayout mRefreshLayout;
    RecyclerView recList;
    UserAdapter adapter;
    private Button chgpwd, delUser, back, mUnbind;
    private TextView txtname, txtlastname, txtemail, txtlogin_type, txtlastStep, txtlastBike, txttotalStep, txttotalBike,
            txtlastconnect, txtnameDisplay, mMac;
    private String name, lastname, user_email, login_type, lastStep, lastBike, totalStep, totalBike,
            lastconnect, nameDisplay, mac_address;
    private LinearLayout account;
    private ProgressDialog progress, progressdata, progressDel, changeProgress, progressUnbind, bindprogress;
    private CoordinatorLayout mRoot;
    private SearchView searchUser;
    private boolean isBind = false;
    private boolean isDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        chgpwd = findViewById(R.id.butChgpwd);
        delUser = findViewById(R.id.butDelete);
        back = findViewById(R.id.butBack);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mUnbind = findViewById(R.id.butUnbind);
        recList = findViewById(R.id.cardList);
        chgpwd.setOnClickListener(this);
        back.setOnClickListener(this);
        delUser.setOnClickListener(this);
        mUnbind.setOnClickListener(this);
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
        mMac = findViewById(R.id.textMac);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        userList = new ArrayList<>();
        allUser = new ArrayList<>();
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                account.setVisibility(View.VISIBLE);
                login_type = user.getLoginType();
                get_User(user_email);
                //get_Band(user_email);
            }
        });

    }

    @Override
    public void onItemClick(View v, int pos) {
        mRefreshLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View v) {
        if (v == chgpwd) {
            Changepwd();
        } else if (v == delUser) {
            DeleteUser(user_email);
        } else if (v == back) {
            Intent main = new Intent(UserActivity.this, MainActivity.class);
            startActivity(main);
            finish();
        } else if (v == mUnbind) {
            if (mUnbind.getText().toString().equals(R.string.unbind_device) || isBind) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
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
                        Unbind(user_email, mac_address);

                    }
                });
                builder.show();
            } else if (mUnbind.getText().toString().equals(R.string.bind_device) || !isBind) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
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
                        BindDevice(user_email, mac_address);

                    }
                });
                builder.show();

            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        account.setVisibility(View.VISIBLE);
        Intent userdata = getIntent();
        user = new UserDetails();
        userDB = new SaveData(this);
        user = userDB.loadUserInfo();
        name = userdata.getStringExtra("firstname");
        lastname = userdata.getStringExtra("lastname");
        user_email = userdata.getStringExtra("email");
        nameDisplay = userdata.getStringExtra("name");
        get_User(user_email);
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
                        mUnbind.setText(R.string.bind_device);
                        isBind = false;

                        if (isDelete) {
                            Intent login = new Intent(UserActivity.this, MainActivity.class);
                            startActivity(login);
                            finish();
                        }
                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        progressUnbind.dismiss();
                    }

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

                        bindprogress.setMessage("Bind successful...");
                        mUnbind.setText(R.string.unbind_device);
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
                        if (resp.getMessage().contains("Already bind to another device")) {
                            Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
                            if (resp.getUser().getEmail().equals(email) && resp.getUser().getMacaddress().equals(macaddress)) {

                                bindprogress.setMessage("Bind successful...");

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
                                mUnbind.setText(R.string.bind_device);
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
                                    Toast.makeText(UserActivity.this, "Field cant be empty", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (new_pass.getText().toString().equals(new_passagain.getText().toString())) {
                                        Change_pwd(user_email, old_pass.getText().toString(), new_pass.getText().toString());
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

    private void get_All_User_Data(String email) {
        progressdata = new ProgressDialog(this);
        progressdata.setIndeterminate(true);
        progressdata.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressdata.setMessage("loading user data...");
        progressdata.setCancelable(true);
        progressdata.show();

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
                    progressdata.setMessage("User information retrieved...");
                    Snackbar snackbar = Snackbar.make(mRoot, "User information retrieved...", Snackbar.LENGTH_SHORT);
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
                Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                snackbar.show();

                Log.d(Constants.TAG, "failed");
                if (progressdata != null) {
                    progressdata.dismiss();
                }

            }
        });
    }

    private void get_Band(String email) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterface = retrofit.create(RequestInterfaceObject.class);
        UserDetails user = new UserDetails();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_BAND);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    mac_address = resp.getUser().getMacaddress();
                    progress.setMessage("User Info Retrieved...");
                    mMac.setText(mac_address);
                    Snackbar snackbar = Snackbar.make(mRoot, "Info Retrieved...", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    isBind = true;
                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, "Network occur!!!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

                if (progress != null) {
                    progress.dismiss();
                }

                get_All_User_Data(user_email);

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);
                if (progress != null) {
                    progress.dismiss();
                }
                Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void get_User(String email) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Retrieving user info...");
        progress.setCancelable(true);
        progress.show();

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
                    login_type = resp.getUser().getLoginType();
                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, "Network occur!!!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                get_Band(user_email);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);
                Snackbar snackbar = Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT);
                snackbar.show();

                Log.d(Constants.TAG, "failed");
                if (progress != null) {
                    progress.dismiss();
                }
            }
        });
    }

    private void DeleteUser(String email) {
        progressDel = new ProgressDialog(this);
        progressDel.setIndeterminate(true);
        progressDel.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDel.setMessage("Deleting user ...");
        progressDel.setCancelable(true);
        progressDel.show();
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

                Log.d(Constants.TAG, "failed");
            }
        });
    }

    private void DeleteUserData(String email) {
        progressDel.setMessage("Deleting user data...");
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
                    isDelete = true;
                } else {
                    progressDel.setMessage(resp.getMessage());
                    Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar.make(mRoot, "User delete successfully...", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        progressDel.setMessage("User delete successfully...");

                    }
                }, 200);

                if (progressDel != null) {
                    progressDel.dismiss();
                }
                Unbind(user_email, mac_address);

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
    }

    private void Change_pwd(String email, String old_password, final String new_password) {
        changeProgress = new ProgressDialog(this);
        changeProgress.setIndeterminate(true);
        changeProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        changeProgress.setMessage("Changing user password ...");
        changeProgress.setCancelable(true);
        changeProgress.show();
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
                //Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT).show();
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
                            Snackbar.make(mRoot, "Password change successfully Please check your email for confirmation ...", Snackbar.LENGTH_LONG).show();
                        }
                    }, 500);
                    Toast.makeText(UserActivity.this, "Password change successfully Please check your email for confirmation ", Toast.LENGTH_SHORT).show();

                    if (changeProgress != null) {
                        changeProgress.dismiss();
                    }
                    userDB.clear_data();
                    session.logoutUser();
                    finish();

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
                            if (changeProgress != null) {
                                changeProgress.dismiss();
                            }

                        }
                    }, 500);

                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);

                Snackbar.make(mRoot, "Error occur, please try again!!!", Snackbar.LENGTH_SHORT).show();
                Log.d(Constants.TAG, "failed");
                if (changeProgress != null) {
                    changeProgress.dismiss();
                }

            }
        });
    }

    public void loadData() {
        totalSteps = 0;
        totalBikes = 0;
        for (int i = 0; i < userList.size(); i++) {
            totalSteps = totalSteps + Integer.valueOf(userList.get(i).getStep());
            totalBikes = totalBikes + Long.valueOf(userList.get(i).getBike());
        }
        lastconnect = userList.get(0).getDate();
        lastStep = userList.get(0).getStep();
        String bike_in_mills = userList.get(0).getBike();
        lastBike = convert_millisec(bike_in_mills);
        totalStep = String.valueOf(totalSteps);
        totalBike = convert_millisec(String.valueOf(totalBikes));
        txtname.setText("Name: " + upperCaseAllFirst(name));
        txtnameDisplay.setText(upperCaseAllFirst(nameDisplay));
        txtlastname.setText("Surname: " + upperCaseAllFirst(lastname));
        txtemail.setText("Email: " + user_email);
        txtlogin_type.setText("Login with: " + login_type);
        txtlastStep.setText("Last update step: " + lastStep);
        txtlastBike.setText("Last update bike: " + lastBike);
        txtlastconnect.setText("Last connection: " + lastconnect);
        txttotalStep.setText("Total step: " + totalStep);
        txttotalBike.setText("Total bike: " + totalBike);
        mRefreshLayout.setRefreshing(false);
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

    public String convert_millisec(String millis) {
        long milliseconds = Long.valueOf(millis);
        String bike_data;
        int minutes = (int) (((milliseconds / 1000) / 60) % 60);
        int hours = (int) (((milliseconds / 1000) / 3600) % 24);
        bike_data = String.valueOf(hours < 10 ? "0" + hours : hours) + "hr:" + String.valueOf("" + (minutes < 10 ? "0" + minutes : minutes)) + "min";
        return bike_data;
    }


}
