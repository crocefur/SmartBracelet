package com.softdev.smarttechx.smartbracelet.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.softdev.smarttechx.smartbracelet.model.DeviceDetails;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.Userdata;
import com.softdev.smarttechx.smartbracelet.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by SMARTTECHX on 9/18/2017.
 */

public class SaveData {
    private static final String SHARED_PREFS_FILE = "shared_prefs_file";
    private static final String BRACELET_LIST = "bracelet_data_list";
    private static final String USER = "user";
    private static final String USER_DETAILS = "user_details";
    private static final String DEVICE_DETAILS = "device_details";
    private static final String CONNECT_TIME = "connect_time";
    private static final String USER_DATA = "user_data";
    private static final String NOTIFY = "notify";


    private SharedPreferences prefBracelet;
    private SharedPreferences prefUser;
    private SharedPreferences prefUserDetails;
    private SharedPreferences prefDevice;
    private SharedPreferences prefTime;
    private SharedPreferences prefUserData;
    private SharedPreferences prefNotify;
    // Editor for Shared preferences

    private SharedPreferences.Editor editorBracelet;
    private SharedPreferences.Editor editorUser;
    private SharedPreferences.Editor editorUserDetails;
    private SharedPreferences.Editor editorDevice;
    private SharedPreferences.Editor editorTime;
    private SharedPreferences.Editor editorUserData;
    private SharedPreferences.Editor editorNotify;


    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    public SaveData(Context context) {
        this._context = context;
        prefUser = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorUser = prefUser.edit();
        prefUserDetails = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorUserDetails = prefUserDetails.edit();
        prefBracelet = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorBracelet = prefBracelet.edit();
        prefDevice = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorDevice = prefDevice.edit();
        prefTime = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorTime = prefTime.edit();

        prefUserData = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorUserData = prefUserData.edit();

        prefNotify = _context.getSharedPreferences(SHARED_PREFS_FILE, PRIVATE_MODE);
        editorNotify = prefNotify.edit();
    }


    public void clear_data() {
        editorBracelet.clear();
        editorUser.clear();
        editorUserDetails.clear();
        editorDevice.clear();
        editorTime.clear();
        editorUserData.clear();
        editorBracelet.commit();
        editorUser.commit();
        editorUserDetails.commit();
        editorDevice.commit();
        editorTime.commit();
        editorUserData.commit();

    }

    public void clear_sync() {
        editorNotify.clear();
        editorNotify.commit();
    }

    public void saveUser(ArrayList<String> userList) {

        try {
            editorUser.putString(USER, ObjectSerializer.serialize(userList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorUser.commit();

    }

    public ArrayList<String> loadUser() {
        ArrayList<String> userList = new ArrayList<>();
        if (prefUser != null) {
            try {
                userList = (ArrayList<String>) ObjectSerializer.deserialize(prefUser.getString(USER,
                        ObjectSerializer.serialize(new ArrayList<String>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            userList = new ArrayList<String>();
        }
        return userList;
    }

    public void saveBraceletData(ArrayList<String> BraceletList) {

        try {
            editorBracelet.putString(BRACELET_LIST, ObjectSerializer.serialize(BraceletList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorBracelet.commit();


    }

    public ArrayList<String> loadBracelet() {
        ArrayList<String> braceletList = new ArrayList<>();
        if (prefBracelet != null) {
            try {
                braceletList = (ArrayList<String>) ObjectSerializer.deserialize(prefBracelet.getString(BRACELET_LIST,
                        ObjectSerializer.serialize(new ArrayList<String>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            braceletList = new ArrayList<String>();
        }
        return braceletList;
    }

    public void saveUserData(ArrayList<UserDetails> userdata) {

        try {
            editorUserData.putString(USER_DATA, ObjectSerializer.serialize(userdata));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorUserData.commit();


    }

    public ArrayList<UserDetails> loadUserdata() {
        ArrayList<UserDetails> userDataList = new ArrayList<>();
        if (prefUserData != null) {
            try {
                userDataList = (ArrayList<UserDetails>) ObjectSerializer.deserialize(prefUserData.getString(USER_DATA,
                        ObjectSerializer.serialize(new ArrayList<UserDetails>())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            userDataList = new ArrayList<UserDetails>();
        }
        return userDataList;
    }

    public void saveDevice(DeviceDetails deviceDetail) {

        try {
            editorDevice.putString(DEVICE_DETAILS, ObjectSerializer.serialize(deviceDetail));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorDevice.commit();
    }

    public DeviceDetails loadDevice() {
        DeviceDetails device = new DeviceDetails();
        if (prefDevice != null) {
            try {
                device = (DeviceDetails) ObjectSerializer.deserialize(prefDevice.getString(DEVICE_DETAILS,
                        ObjectSerializer.serialize(new DeviceDetails())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            device = new DeviceDetails();
        }
        return device;
    }

    public void saveUserInfo(UserDetails userDetails) {

        try {
            editorUserDetails.putString(USER_DETAILS, ObjectSerializer.serialize(userDetails));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorUserDetails.commit();
    }


    public UserDetails loadUserInfo() {
        UserDetails userInfo = new UserDetails();
        if (prefUserDetails != null) {
            try {
                userInfo = (UserDetails) ObjectSerializer.deserialize(prefUserDetails.getString(USER_DETAILS,
                        ObjectSerializer.serialize(new UserDetails())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            userInfo = new UserDetails();
        }
        return userInfo;
    }

    public String loadTime() {
        String last_time = new String();
        if (prefTime != null) {
            try {
                last_time = (String) ObjectSerializer.deserialize(prefTime.getString(CONNECT_TIME,
                        ObjectSerializer.serialize(new String())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            last_time = new String();
        }
        return last_time;
    }


    public void saveLastTime(String last_time) {

        try {
            editorTime.putString(CONNECT_TIME, ObjectSerializer.serialize(last_time));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorTime.commit();
    }

    public String loadNotify() {
        String last_notify = new String();
        if (prefNotify != null) {
            try {
                last_notify = (String) ObjectSerializer.deserialize(prefNotify.getString(NOTIFY,
                        ObjectSerializer.serialize(new String())));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            last_notify = new String();
        }
        return last_notify;
    }


    public void saveNotify(String last_notify) {

        try {
            editorNotify.putString(NOTIFY, ObjectSerializer.serialize(last_notify));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorNotify.commit();
    }


}
