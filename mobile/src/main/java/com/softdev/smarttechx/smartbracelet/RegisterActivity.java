package com.softdev.smarttechx.smartbracelet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.util.Constants;
import com.softdev.smarttechx.smartbracelet.util.RequestInterfaceObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.softdev.smarttechx.smartbracelet.util.Constants.CUSTOM_LOGIN;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
    EditText InputName, InputLastname, InputPassword;
    Button register;
    String datetime, login_type;
    private AutoCompleteTextView InputEmail;
    private ProgressDialog progress;
    private CoordinatorLayout mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        InputName = findViewById(R.id.editResetCode);
        InputLastname = findViewById(R.id.editTextLastname);
        InputEmail = findViewById(R.id.editTextEmail);
        InputPassword = findViewById(R.id.editTextPassword);
        mRoot = findViewById(R.id.parentRoot);
        register = findViewById(R.id.buttonSignup);
        register.setOnClickListener(this);
        addAdapterToViews();
    }

    private String getTime() {
        Calendar getCal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd:MM:yy::");
        datetime = df.format(getCal.getTime());
        return datetime;
    }

    private void addAdapterToViews() {
        Account[] accounts = AccountManager.get(this).getAccounts();
        Set<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            if (EMAIL_PATTERN.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }
        InputEmail.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(emailSet)));

    }

    public void onClick(View v) {

        if (v == register) {
            String name = InputName.getText().toString();
            String lastname = InputLastname.getText().toString();
            String email = InputEmail.getText().toString();
            String password = InputPassword.getText().toString();

            if (!name.isEmpty() && !lastname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

                progress = new ProgressDialog(this);
                progress.setIndeterminate(true);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage("Registering...");
                progress.setCancelable(false);
                progress.show();
                registerProcess(name, lastname, email, CUSTOM_LOGIN, password);

            } else {

                Snackbar.make(mRoot, "Fields are empty !", Snackbar.LENGTH_LONG).show();
            }

        }
    }

    private void registerProcess(String name, String lastname, String email, String logintype, String password) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterfaceObject requestInterfaceObject = retrofit.create(RequestInterfaceObject.class);

            UserDetails user = new UserDetails();
            user.setName(name);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setLoginType(logintype);
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
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage(), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        Intent main = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(main);
                        finish();

                    } else {
                        Snackbar snackbar = Snackbar.make(mRoot, resp.getMessage() + "!!!", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                    if (progress != null) {
                        progress.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Log.d(Constants.TAG, "failed");
                    Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progress != null) {
                        progress.dismiss();
                    }

                }
            });
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login);
        finish();
    }

}
