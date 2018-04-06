package com.softdev.smarttechx.smartbracelet;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.adapter.DataAdapter;
import com.softdev.smarttechx.smartbracelet.adapter.ItemClickListener;
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

import android.support.v7.widget.RecyclerView;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.DataFormatException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity implements View.OnClickListener, ItemClickListener {
    private static DecimalFormat formatDis = new DecimalFormat("#.##");
    public TextView mTitle, mUnit;
    public ArrayList<Userdata> userStep;
    public ArrayList<Userdata> userBike;
    public ArrayList<Userdata> sort_userStep;
    public ArrayList<Userdata> sort_userBike;
    public ArrayList<String> user_emails;
    public ArrayList<String> admin_emails;
    SessionManager session;
    SaveData saveData;
    RecyclerView recList;
    DataAdapter adapter;
    int sno;
    Calendar dateTime = Calendar.getInstance();
    LinearLayoutManager llm;
    ArrayList<UserDetails> userList;
    ArrayList<UserDetails> sort_user;
    ArrayList<UserDetails> newUserList;
    Userdata user_data;
    String from_date, till_date;
    UserDetails details;
    TextView totalraw, totalcon, newUser, activeUser;
    SimpleDateFormat checkDate;
    int totRaw;
    double contotRaw;
    long totRawBike;
    int userStepData;
    long userBikeData;
    LinearLayout mCtrlLayout;
    LocationManager locationManager;
    SwipeRefreshLayout mRefreshLayout;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private Button fromBut, toBut;
    private ImageButton stepBut, bikeBut, exitBut;
    private SearchView searchUser;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private CoordinatorLayout mRoot;
    private ProgressDialog progress;
    private HashMap<String, ArrayList<UserDetails>> separate_list = new HashMap<String, ArrayList<UserDetails>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = findViewById(R.id.label);
        fromBut = findViewById(R.id.butFrom);
        toBut = findViewById(R.id.butTo);
        stepBut = findViewById(R.id.step);
        bikeBut = findViewById(R.id.bike);
        exitBut = findViewById(R.id.exitBut);
        totalraw = findViewById(R.id.datatotal);
        totalcon = findViewById(R.id.mco2Total);
        searchUser = findViewById(R.id.searchUser);
        newUser = findViewById(R.id.textNewuser);
        activeUser = findViewById(R.id.textActiveuser);
        mCtrlLayout = findViewById(R.id.ctrlLayout);
        mUnit = findViewById(R.id.textUnit);
        mRoot = findViewById(R.id.parentRoot);
        recList = findViewById(R.id.cardList);
        stepBut.setOnClickListener(this);
        bikeBut.setOnClickListener(this);
        exitBut.setOnClickListener(this);
        saveData = new SaveData(this);
        recList.setHasFixedSize(true);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        session = new SessionManager(getApplicationContext());
        searchUser.setQueryHint("Find user by name...");

        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        setDateTimeField();
        userList = new ArrayList<>();
        sort_user = new ArrayList<>();
        newUserList = new ArrayList<>();
        userBike = new ArrayList<Userdata>();
        userStep = new ArrayList<Userdata>();
        sort_userBike = new ArrayList<Userdata>();
        sort_userStep = new ArrayList<Userdata>();
        user_emails = new ArrayList<String>();
        checkDate = new SimpleDateFormat("yyyy-MM-dd");
        stepBut.setImageDrawable(getDrawable(R.drawable.ic_directions_walk_white_24dp));
        bikeBut.setImageDrawable(getDrawable(R.drawable.ic_directions_bike_black_24dp));
        stepBut.setBackground(getDrawable(R.drawable.roundcornerbsc));
        bikeBut.setBackground(getDrawable(R.drawable.roundcornerbs));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_All_Data(from_date, till_date);
            }
        });
    }

    private void setDateTimeField() {
        fromBut.setOnClickListener(this);
        toBut.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                try {
                    Date date1 = checkDate.parse(dateFormatter.format(newDate.getTime()));
                    Date date2 = checkDate.parse(till_date);
                    if (date1.compareTo(date2) > 0) {
                        from_date = till_date;
                    } else {
                        from_date = dateFormatter.format(newDate.getTime());
                    }
                } catch (Exception e) {

                }
                fromBut.setText("From " + from_date);
                get_All_Data(from_date, till_date);
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                try {
                    Date date1 = checkDate.parse(dateFormatter.format(newDate.getTime()));
                    Date date2 = checkDate.parse(dateFormatter.format(dateTime.getTime()));
                    Date fromdate = checkDate.parse(from_date);
                    Date tilldate = checkDate.parse(till_date);
                    if (fromdate.compareTo(tilldate) > 0) {
                        till_date = from_date;
                    } else if (date1.compareTo(date2) > 0) {
                        till_date = dateFormatter.format(dateTime.getTime());
                    } else {
                        till_date = dateFormatter.format(newDate.getTime());
                    }
                } catch (Exception e) {

                }
                toBut.setText("Till " + till_date);
                get_All_Data(from_date, till_date);
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onItemClick(View v, int pos) {

    }

    @Override
    public void onResume() {
        super.onResume();
        userList.clear();
        userBike.clear();
        userStep.clear();
        from_date = df.format(dateTime.getTime());
        till_date = df.format(dateTime.getTime());
        fromBut.setText("From " + from_date);
        toBut.setText("Till " + till_date);
        get_All_Data(from_date, till_date);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {
        if (v == fromBut) {
            fromDatePickerDialog.show();
        } else if (v == toBut) {
            toDatePickerDialog.show();
        } else if (v == exitBut) {
            saveData.clear_data();
            session.logoutUser();
        } else if (v == stepBut) {
            stepDisplay(sort_userStep);
        } else if (v == bikeBut) {
            bikeDisplay(sort_userBike);
        }
    }

    public void stepDisplay(ArrayList<Userdata> data) {
        stepBut.setImageDrawable(getDrawable(R.drawable.ic_directions_walk_white_24dp));
        bikeBut.setImageDrawable(getDrawable(R.drawable.ic_directions_bike_black_24dp));
        stepBut.setBackground(getDrawable(R.drawable.roundcornerbsc));
        bikeBut.setBackground(getDrawable(R.drawable.roundcornerbs));
        mTitle.setText("Users step details");
        totalraw.setText(String.valueOf(totRaw) + " steps");
        contotRaw = totRaw / 13;
        totalcon.setText(String.valueOf(formatDis.format(contotRaw)));
        mUnit.setText(Html.fromHtml("grCO2"));
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);
        adapter = new DataAdapter(this, data);
        recList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //FILTER AS YOU TYPE
                adapter.getFilter().filter(query);
                return false;
            }
        });
        searchUser.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mCtrlLayout.setVisibility(View.VISIBLE);
                return false;
            }
        });
        searchUser.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCtrlLayout.setVisibility(View.GONE);

            }
        });
    }

    public void bikeDisplay(ArrayList<Userdata> data) {
        stepBut.setImageDrawable(getDrawable(R.drawable.ic_directions_walk_black_24dp));
        bikeBut.setImageDrawable(getDrawable(R.drawable.ic_directions_bike_white_24dp));
        stepBut.setBackground(getDrawable(R.drawable.roundcornerbs));
        bikeBut.setBackground(getDrawable(R.drawable.roundcornerbsc));
        mTitle.setText("Users bike details");
        totalraw.setText(convert_millisec(String.valueOf(totRawBike)));
        contotRaw = totRawBike / 2400;
        totalcon.setText(String.valueOf(formatDis.format(contotRaw)));
        mUnit.setText(Html.fromHtml("grCO2"));
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);
        adapter = new DataAdapter(this, data);
        recList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCtrlLayout.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //FILTER AS YOU TYPE
                adapter.getFilter().filter(query);
                return false;
            }
        });
        searchUser.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mCtrlLayout.setVisibility(View.VISIBLE);
                return false;
            }
        });
        searchUser.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCtrlLayout.setVisibility(View.GONE);

            }
        });

    }


    private void get_All_Data(String from, String till) {
        userList.clear();
        //newUser.setText("");
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Loading  data...");
        progress.setCancelable(false);
        progress.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceArray requestInterface = retrofit.create(RequestInterfaceArray.class);
        UserDetails user = new UserDetails();
        user.setDateFrom(from);
        user.setDateTill(till);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_ALL_USER_DATA);
        request.setUser(user);
        Call<UserList> response = requestInterface.operation(request);
        response.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, retrofit2.Response<UserList> response) {
                UserList resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    userList = resp.getUsers();
                    // saveData.saveUserData(userList);
                    progress.setMessage("Synchronization done...");
                    Snackbar snackbar = Snackbar.make(mRoot, "Data retrieved...", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(mRoot, "No data within the range...", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                get_New_User(from_date, till_date);

            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                // handleSignInResult(null);
                Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                snackbar.show();

                Log.d(Constants.TAG, "failed");
                if (progress != null) {
                    progress.dismiss();
                }
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void get_New_User(String from, String till) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterfaceArray requestInterface = retrofit.create(RequestInterfaceArray.class);
        UserDetails user = new UserDetails();
        user.setDateFrom(from);
        user.setDateTill(till);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_NEW_USER);
        request.setUser(user);
        Call<UserList> response = requestInterface.operation(request);
        response.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, retrofit2.Response<UserList> response) {
                UserList resp = response.body();
                Log.d(Constants.TAG, resp.getMessage());
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    admin_emails = new ArrayList<String>();
                    newUserList = resp.getUsers();
                    for (UserDetails user : resp.getUsers()) {
                        admin_emails.add(user.getEmail());
                    }
                    if (admin_emails.contains("admin_ursa@pametnaura.si")) {
                        if (newUserList.size() == 2) {
                            //newUser.setText(String.valueOf(admin_emails.size() - 1) + " new user has been created in this time");
                        } else if (newUserList.size() == 1) {
                            // newUser.setText("No new user has been created in this time");
                        } else {
                            // newUser.setText(String.valueOf(admin_emails.size() - 1) + "  new users has been created in this time");
                        }
                    } else {
                        if (newUserList.size() == 1) {
                            // newUser.setText(String.valueOf(admin_emails.size()) + " new user has been created in this time");
                        } else {
                            // newUser.setText(String.valueOf(admin_emails.size()) + "  new users has been created in this time");
                        }
                    }
                    //newUser.setText(" ");
                } else {
                    // newUser.setText("No new user has been created in this time");
                }
                Snackbar snackbar = Snackbar.make(mRoot, "Synchronization  done...", Snackbar.LENGTH_SHORT);
                snackbar.show();
                loadData();
                if (progress != null) {
                    progress.dismiss();
                }
                mRefreshLayout.setRefreshing(false);
                // newUser.setText(" ");

            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                // handleSignInResult(null);
                if (progress != null) {
                    progress.dismiss();
                }
                mRefreshLayout.setRefreshing(false);

                Snackbar snackbar = Snackbar.make(mRoot, "Error occur!!!", Snackbar.LENGTH_SHORT);
                snackbar.show();

                Log.d(Constants.TAG, "failed");

            }
        });
    }

    public void loadData() {

        totRaw = 0;
        sno = 0;
        totRawBike = 0;
        userStepData = 0;
        userBikeData = 0;
        userBike.clear();
        userStep.clear();
        sort_userBike.clear();
        sort_userStep.clear();
        user_emails.clear();
        sort_user.clear();
        separate_list.clear();
        //  userList = saveData.loadUserdata();
        for (int i = 0; i < userList.size(); i++) {
            try {

                if (Integer.valueOf(userList.get(i).getStep()) == 1) {
                    totRaw = totRaw + 0;
                } else {
                    totRaw = totRaw + Integer.valueOf(userList.get(i).getStep());
                }
                if (Integer.valueOf(userList.get(i).getBike()) == 1) {
                    totRawBike = totRawBike + 0;
                } else {
                    totRawBike = totRawBike + Long.valueOf(userList.get(i).getBike());
                }
            } catch (Exception e) {

            }

        }
        for (UserDetails users : userList) {
            ArrayList<UserDetails> temp = separate_list.get(users.getEmail());
            if (!user_emails.contains(users.getEmail())) {
                user_emails.add(users.getEmail());
            }
            if (temp == null) {
                temp = new ArrayList<UserDetails>();
                separate_list.put(users.getEmail(), temp);
            }
            temp.add(users);
        }
        if (user_emails.size() == 1) {
            activeUser.setText(String.valueOf(user_emails.size()) + " user have been active in this time");
        } else {
            activeUser.setText(String.valueOf(user_emails.size()) + " users have been active in this time");
        }
        getBike();
        getStep();
        stepDisplay(sort_userStep);
    }

    public void getBike() {
        sno = 0;
        for (String mail : user_emails) {

            sort_user = separate_list.get(mail);
            userStepData = 0;
            userBikeData = 0;
            user_data = new Userdata();
            user_data.setName(sort_user.get(0).getName() + " " + sort_user.get(0).getLastname());
            user_data.setFirstname(sort_user.get(0).getName());
            user_data.setLastname(sort_user.get(0).getLastname());
            user_data.setEmail(sort_user.get(0).getEmail());
            for (int i = 0; i < sort_user.size(); i++) {

                try {
                    if (Integer.valueOf(sort_user.get(i).getStep()) == 1) {

                        userStepData = userStepData + 0;
                    } else {
                        userStepData = userStepData + Integer.valueOf(sort_user.get(i).getStep());
                    }
                    if (Long.valueOf(sort_user.get(i).getBike()) == 1) {
                        userBikeData = userBikeData + 0;
                    } else {
                        userBikeData = userBikeData + Long.valueOf(sort_user.get(i).getBike());
                    }
                } catch (Exception e) {

                }

            }
            user_data.setStepdata(String.valueOf(userStepData));
            user_data.setBikedata(String.valueOf(userBikeData));
            userBike.add(user_data);
        }

        try {
            Collections.sort(userBike, Userdata.bike_sort);
            for (Userdata user : userBike) {
                sno++;
                user_data = new Userdata();
                user_data.setNo(String.valueOf(sno));
                user_data.setName(upperCaseAllFirst(user.getName()));
                user_data.setFirstname(upperCaseAllFirst(user.getFirstname()));
                user_data.setLastname(upperCaseAllFirst(user.getLastname()));
                user_data.setEmail(user.getEmail());
                user_data.setData_type("bike");
                user_data.setStepdata(user.getStepdata());
                user_data.setBikedata(user.getBikedata());
                sort_userBike.add(user_data);
            }

        } catch (Exception e) {

        }

    }

    public void getStep() {
        sno = 0;
        for (String mail : user_emails) {
            sort_user = separate_list.get(mail);
            userStepData = 0;
            userBikeData = 0;
            user_data = new Userdata();
            user_data.setName(sort_user.get(0).getName() + " " + sort_user.get(0).getLastname());
            user_data.setFirstname(sort_user.get(0).getName());
            user_data.setLastname(sort_user.get(0).getLastname());
            user_data.setEmail(sort_user.get(0).getEmail());

            for (int i = 0; i < sort_user.size(); i++) {

                try {
                    if (Integer.valueOf(sort_user.get(i).getStep()) == 1) {
                        userStepData = userStepData + 0;
                    } else {
                        userStepData = userStepData + Integer.valueOf(sort_user.get(i).getStep());
                    }
                    if (Long.valueOf(sort_user.get(i).getBike()) == 1) {
                        userBikeData = userBikeData + 0;
                    } else {
                        userBikeData = userBikeData + Long.valueOf(sort_user.get(i).getBike());
                    }
                } catch (Exception e) {

                }

            }
            user_data.setStepdata(String.valueOf(userStepData));
            user_data.setBikedata(String.valueOf(userBikeData));
            userStep.add(user_data);
        }
        try {
            Collections.sort(userStep, Userdata.step_sort);
            for (Userdata user : userStep) {
                sno++;
                user_data = new Userdata();
                user_data.setNo(String.valueOf(sno));
                user_data.setName(upperCaseAllFirst(user.getName()));
                user_data.setFirstname(upperCaseAllFirst(user.getFirstname()));
                user_data.setLastname(upperCaseAllFirst(user.getLastname()));
                user_data.setEmail(user.getEmail());
                user_data.setData_type("step");
                user_data.setStepdata(user.getStepdata());
                user_data.setBikedata(user.getBikedata());
                sort_userStep.add(user_data);
            }
        } catch (Exception e) {

        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
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
