package com.softdev.smarttechx.smartbracelet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.UserList;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceArray;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;
import com.softdev.smarttechx.smartbracelet.util.SessionManager;

import android.support.design.widget.CoordinatorLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.softdev.smarttechx.smartbracelet.util.Constants.CUSTOM_LOGIN;
import static com.softdev.smarttechx.smartbracelet.util.Constants.FACEBOOK_LOGIN;
import static com.softdev.smarttechx.smartbracelet.util.Constants.GMAIL_LOGIN;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    //    private MyProfileTracker mprofileTracker;
    private static final int RC_SIGN_IN = 9001;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
    private final int Wait_delay = 50;
    LoginManager mFacebookLoginManager;
    String user_name;
    String user_lastname;
    String user_email;
    String user_password;
    String profilepic = "user_pic";
    SaveData savedata;
    ArrayList<String> user;
    SessionManager session;
    UserDetails userDetails;
    ArrayList<UserDetails> userList;
    String login_type;
    private Button loginButton, signupButton;
    private ImageButton mGoogleSignInButton, mFacebookSignInButton;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mFacebookCallbackManager;
    private ProgressDialog progress, progressReg, progPWD;
    private EditText passEdit;
    private AutoCompleteTextView userEdit;
    private CoordinatorLayout mRoot;
    private ProgressDialog progressdata;
    private TextView mforgetPWD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(null);
        setupFacebookStuff();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        loginButton = findViewById(R.id.buttonLogin);
        signupButton = findViewById(R.id.buttonSignup);
        userEdit = findViewById(R.id.editTextUsername);
        passEdit = findViewById(R.id.editTextPassword);
        mGoogleSignInButton = findViewById(R.id.googleLogin);
        mFacebookSignInButton = findViewById(R.id.faceBooklogin);
        mforgetPWD = findViewById(R.id.txtForgotPWD);
        signupButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        mRoot = findViewById(R.id.parentRoot);
        session = new SessionManager(this);
        mGoogleSignInButton.setOnClickListener(this);
        mFacebookSignInButton.setOnClickListener(this);
        savedata = new SaveData(this);
        userDetails = new UserDetails();
        user = new ArrayList<String>();
        userList = new ArrayList<>();
        mforgetPWD.setOnClickListener(this);
        mforgetPWD.setText(Html.fromHtml("<u>Forgot Password?</u>"));
        addAdapterToViews();
        //  C7:13:15:C7:DB:F5:2E:A9:62:ED:F8:87:68:41:5A:09:6A:C9:7A:25
        /*byte[] sha1 = {
                (byte)0xC7, 0x13, 0x15,(byte)0xC7, (byte)0xDB, (byte)0xF5, 0x2E, (byte)0xA9, 0x62, (byte)0xED,(byte)0xF8, 0x87, 0x68, 0x41, 0x5A, 0x09, 0x6A, (byte)0xC9, 0x7A, 0x25
        };
        Log.d("keyhash", Base64.encodeToString(sha1, Base64.NO_WRAP));*/
    }

    public void onClick(View v) {
        if (v == signupButton) {
            Intent main = new Intent(LoginActivity.this, RegisterActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
        } else if (v == mGoogleSignInButton) {
            login_type = GMAIL_LOGIN;
            signInWithGoogle();
        } else if (v == mFacebookSignInButton) {
            login_type = FACEBOOK_LOGIN;
            signInWithFacebook();
        } else if (v == loginButton) {
            user_email = userEdit.getText().toString();
            user_password = passEdit.getText().toString();
            login_type = CUSTOM_LOGIN;
            if (!user_email.isEmpty() && !user_password.isEmpty()) {
                loginProcess(user_email, user_password);
            } else {

                Snackbar.make(mRoot, "Fields are empty !", Snackbar.LENGTH_LONG).show();
            }

        } else if (v == mforgetPWD) {
            Intent main = new Intent(LoginActivity.this, ResetpasswordActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
            finish();
        }
    }

    private void addAdapterToViews() {
        Account[] accounts = AccountManager.get(this).getAccounts();
        Set<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            if (EMAIL_PATTERN.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }
        userEdit.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(emailSet)));

    }

    @Override
    public void onResume() {
        super.onResume();
        if (savedata.loadUserInfo() != null) {
            userDetails = savedata.loadUserInfo();
        }
    }

    private void handleSignInResult(Callable<Void> logout) {
        if (logout == null) {
            /* Login error */
            if (progress != null) {
                progress.dismiss();
            }
            Toast.makeText(LoginActivity.this, "Error while logging in", Toast.LENGTH_SHORT).show();
        } else {
            if (progress != null) {
                progress.dismiss();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleApiClient client = mGoogleApiClient;
                GoogleSignInAccount acct = result.getSignInAccount();
                String id = acct.getId();
                user_email = acct.getEmail();
                user_password = user_email.substring(0, user_email.indexOf("@"));
                user_name = acct.getDisplayName();
                user_lastname = acct.getFamilyName();
                login_type = GMAIL_LOGIN;
                if (user_name.contains(user_lastname)) {
                    user_name = user_name.substring(0, user_name.indexOf(user_lastname));
                }
                profilepic = String.valueOf(acct.getPhotoUrl());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        get_User(user_name, user_lastname, user_email, login_type, user_password);
                        // registerProcess(user_name,user_lastname,user_email,login_type,user_password);
                    }
                }, Wait_delay);
                handleSignInResult(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (client != null) {
                            Auth.GoogleSignInApi.signOut(client).setResultCallback(
                                    new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            Log.d(LoginActivity.class.getCanonicalName(),
                                                    status.getStatusMessage());
                                            Toast.makeText(LoginActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                        return null;
                    }
                });

            } else {
                handleSignInResult(null);


            }
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupFacebookStuff() {

        // This should normally be on your application class
        FacebookSdk.sdkInitialize(getApplicationContext());
        mFacebookLoginManager = LoginManager.getInstance();
        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("Main", response.toString());
                                        setProfileDetail(object);
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,first_name,last_name,email");
                        request.setParameters(parameters);
                        request.executeAsync();

                        handleSignInResult(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                LoginManager.getInstance().logOut();
                                return null;
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        handleSignInResult(null);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(LoginActivity.class.getCanonicalName(), error.getMessage());
                        handleSignInResult(null);
                    }
                }
        );

    }

    private void setProfileDetail(JSONObject jsonObject) {
        try {
            user_name = (jsonObject.getString("first_name"));
            user_lastname = (jsonObject.getString("last_name"));
            user_email = (jsonObject.getString("email"));
            String id = jsonObject.optString("id");
            login_type = FACEBOOK_LOGIN;
            user_password = user_email.substring(0, user_email.indexOf("@"));
            profilepic = "https://graph.facebook.com/" + id + "/picture?type=normal";
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    get_User(user_name, user_lastname, user_email, login_type, user_password);
//                    registerProcess(user_name,user_lastname,user_email,login_type,user_password);
                }
            }, Wait_delay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signInWithFacebook() {

        mFacebookLoginManager.logInWithReadPermissions(this, Arrays.asList("user_friends", "public_profile", "email"));
    }

    private void signInWithGoogle() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginProcess(String email, String password) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Logging in...");
        progress.setCancelable(false);
        progress.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

        UserDetails user = new UserDetails();
        user.setEmail(email);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOGIN_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterfaceObject.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    userDetails.setName(resp.getUser().getName());
                    userDetails.setLastname(resp.getUser().getLastname());
                    userDetails.setUsername(resp.getUser().getName() + " " + resp.getUser().getLastname());
                    userDetails.setEmail(resp.getUser().getEmail());
                    userDetails.setPassword(resp.getUser().getPassword());
                    userDetails.setSynctime("0000-00-00");
                    userDetails.setProfilepic(profilepic);
                    userDetails.setLoginType(login_type);
                    userDetails.setStep("00");
                    userDetails.setBike("00");
                    userDetails.setReload(false);
                    userDetails.setIsBind(false);
                    savedata.saveUserInfo(userDetails);
                    if (resp.getUser().getEmail().contains("admin")) {
                        session.createLoginSession(resp.getUser().getName(), resp.getUser().getLastname(), resp.getUser().getEmail(), "admin_pic", resp.getUser().getPassword());
                        Intent main = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(main);
                        finish();
                    } else {
                        Intent main = new Intent(LoginActivity.this, BindActivity.class);
                        main.putExtra("email", resp.getUser().getEmail());
                        main.putExtra("name", resp.getUser().getName());
                        main.putExtra("lastname", resp.getUser().getLastname());
                        main.putExtra("logintype", login_type);
                        main.putExtra("password", resp.getUser().getPassword());
                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(main);
                        finish();

                    }


                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
                if (progress != null) {
                    progress.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);
                if (progress != null) {
                    progress.dismiss();
                }
                Log.d(Constants.TAG, t.getLocalizedMessage());
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void registerProcess(final String name, final String lastname, final String email, final String login_type, final String password) {
        progressReg = new ProgressDialog(this);
        progressReg.setIndeterminate(true);
        progressReg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (login_type.equals(FACEBOOK_LOGIN)) {
            progressReg.setMessage("Connecting to Facebook...");
        } else {
            progressReg.setMessage("Connecting to Google...");
        }
        progressReg.setCancelable(false);
        progressReg.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(mRoot, "Checking database!!!", Snackbar.LENGTH_SHORT);
                snackbar.show();

            }
        }, Wait_delay);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressReg.setMessage("Logging in...");
            }
        }, 400);

        userDetails.setName(name);
        userDetails.setLastname(lastname);
        userDetails.setUsername(name + " " + lastname);
        userDetails.setEmail(email);
        userDetails.setPassword(password);
        userDetails.setSynctime("0000-00-00");
        userDetails.setProfilepic(profilepic);
        userDetails.setLoginType(login_type);
        userDetails.setStep("00");
        userDetails.setBike("00");
        userDetails.setReload(false);
        userDetails.setIsBind(false);
        savedata.saveUserInfo(userDetails);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

        UserDetails user = new UserDetails();
        user.setName(name);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setLoginType(login_type);
        user.setPassword(password);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.REGISTER_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterfaceObject.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    Snackbar.make(mRoot, "Please wait!!!", Snackbar.LENGTH_SHORT).show();
                    Intent main = new Intent(LoginActivity.this, BindActivity.class);
                    main.putExtra("email", email);
                    main.putExtra("name", name);
                    main.putExtra("lastname", lastname);
                    main.putExtra("logintype", login_type);
                    main.putExtra("password", password);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(main);
                    finish();
                } else {
                    Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                if (progressReg != null) {
                    progressReg.dismiss();
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.d(Constants.TAG, t.getMessage());
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                if (progressReg != null) {
                    progressReg.dismiss();
                }
            }
        });
    }

    private void get_User(final String name, final String lastname, final String email, final String login_type, final String password) {
        progressdata = new ProgressDialog(this);
        progressdata.setIndeterminate(true);
        progressdata.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressdata.setMessage("Checking server...");
        progressdata.setCancelable(true);
        progressdata.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressdata.setMessage("Logging in...");
            }
        }, 400);
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
                    userDetails.setName(resp.getUser().getName());
                    userDetails.setLastname(resp.getUser().getLastname());
                    userDetails.setUsername(resp.getUser().getName() + " " + resp.getUser().getLastname());
                    userDetails.setEmail(resp.getUser().getEmail());
                    userDetails.setPassword(resp.getUser().getPassword());
                    userDetails.setSynctime("0000-00-00");
                    userDetails.setProfilepic(profilepic);
                    userDetails.setLoginType(resp.getUser().getLoginType());
                    userDetails.setStep("00");
                    userDetails.setBike("00");
                    userDetails.setReload(false);
                    savedata.saveUserInfo(userDetails);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressdata.setMessage("Login successfully");
                            Snackbar.make(mRoot, "Login successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    }, 400);
                    if (progressdata != null) {
                        progressdata.dismiss();
                    }
                    Intent main = new Intent(LoginActivity.this, BindActivity.class);
                    main.putExtra("email", email);
                    main.putExtra("name", name);
                    main.putExtra("lastname", lastname);
                    main.putExtra("logintype", login_type);
                    main.putExtra("password", password);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(main);
                    finish();

                } else {
                    if (progressdata != null) {
                        progressdata.dismiss();
                    }
                    registerProcess(user_name, user_lastname, user_email, login_type, user_password);
                }


            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                // handleSignInResult(null);
                Snackbar.make(mRoot, "Connection error occur, please check internet and try again", Snackbar.LENGTH_SHORT).show();
                Log.d(Constants.TAG, "failed");
                if (progressdata != null) {
                    progressdata.dismiss();
                }

            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
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
