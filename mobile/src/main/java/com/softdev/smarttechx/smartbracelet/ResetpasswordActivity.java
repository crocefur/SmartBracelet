package com.softdev.smarttechx.smartbracelet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResetpasswordActivity extends AppCompatActivity implements View.OnClickListener {
    UserDetails userDetails;
    private EditText passEdit, resetCode;
    private AutoCompleteTextView userEmail;
    private Button resetBut;
    private TextView tv_timer;
    private boolean isResetInitiated = false;
    private String email;
    private CoordinatorLayout mRoot;
    private CountDownTimer countDownTimer;
    private LinearLayout resetLayout;
    private TextInputLayout mailLayout;
    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        mailLayout = findViewById(R.id.emailLayout);
        resetLayout = findViewById(R.id.resetLayout);
        passEdit = findViewById(R.id.editTextPassword);
        resetCode = findViewById(R.id.editResetCode);
        userEmail = findViewById(R.id.editTextEmail);
        resetBut = findViewById(R.id.buttonReset);
        tv_timer = findViewById(R.id.timer);
        resetBut.setOnClickListener(this);
        resetBut.setText("Request reset code");
        mRoot = findViewById(R.id.parentRoot);
        userDetails = new UserDetails();
        resetLayout.setVisibility(View.GONE);
        tv_timer.setVisibility(View.GONE);
    }

    public void onClick(View v) {
        if (v == resetBut) {
            if (resetBut.getText().equals("Request reset code") || !isResetInitiated) {
                email = userEmail.getText().toString();
                if (!email.isEmpty()) {
                    initiateResetPasswordProcess(email);
                } else {

                    Snackbar.make(mRoot, "Fields are empty !", Snackbar.LENGTH_LONG).show();
                }
            } else if (resetBut.getText().equals("Change password") || isResetInitiated) {
                String code = resetCode.getText().toString();
                String password = passEdit.getText().toString();

                if (!code.isEmpty() && !password.isEmpty()) {

                    finishResetPasswordProcess(email, code, password);
                } else {

                    Snackbar.make(mRoot, "Fields are empty !", Snackbar.LENGTH_LONG).show();
                }
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent main = new Intent(ResetpasswordActivity.this, LoginActivity.class);
                main.putExtra("loader", true);
                startActivity(main);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent main = new Intent(ResetpasswordActivity.this, LoginActivity.class);
            main.putExtra("loader", true);
            startActivity(main);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initiateResetPasswordProcess(String email) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Processing...");
        progress.setCancelable(false);
        progress.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

        UserDetails user = new UserDetails();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.FORGET_PASSWORD_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterfaceObject.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if (resp.getResult().equals(Constants.SUCCESS)) {

                    Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    progress.setMessage(resp.getMessage());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRoot, "Check your email for reset code", Snackbar.LENGTH_LONG).show();
                        }
                    }, 200);
                    mailLayout.setVisibility(View.GONE);
                    resetLayout.setVisibility(View.VISIBLE);
                    tv_timer.setVisibility(View.VISIBLE);
                    resetBut.setText("Change Password");
                    isResetInitiated = true;
                    startCountdownTimer();

                } else {

                    Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    progress.setMessage(resp.getMessage());

                }
                progress.dismiss();


            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {


                Log.d(Constants.TAG, "failed");
                Snackbar.make(mRoot, t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                progress.setMessage(t.getLocalizedMessage());
                progress.dismiss();

            }
        });
    }

    private void finishResetPasswordProcess(String email, String code, String password) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Resetting password...");
        progress.setCancelable(false);
        progress.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

        UserDetails user = new UserDetails();
        user.setEmail(email);
        user.setCode(code);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterfaceObject.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    progress.setMessage(resp.getMessage());
                    countDownTimer.cancel();
                    isResetInitiated = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRoot, "Password reset successfully", Snackbar.LENGTH_LONG).show();
                        }
                    }, 200);
                    Intent main = new Intent(ResetpasswordActivity.this, LoginActivity.class);
                    main.putExtra("loader", true);
                    startActivity(main);
                    finish();

                } else {

                    Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    progress.setMessage(resp.getMessage());

                }

                progress.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {


                Log.d(Constants.TAG, "failed");
                Snackbar.make(mRoot, t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                progress.setMessage(t.getLocalizedMessage());
                progress.dismiss();
            }
        });
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                tv_timer.setText("Time remaining : " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Snackbar.make(mRoot, "Time Out ! Request again to reset password.", Snackbar.LENGTH_LONG).show();
                Intent main = new Intent(ResetpasswordActivity.this, LoginActivity.class);
                main.putExtra("loader", true);
                startActivity(main);
                finish();
            }
        }.start();
    }


}
