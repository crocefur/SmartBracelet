package com.softdev.smarttechx.smartbracelet.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;

public class TaskkillService extends Service {
    SaveData dataDB;
    UserDetails userdetails;
    private BluetoothAdapter mBluetoothAdapter = null;

    public TaskkillService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        dataDB = new SaveData(this);
        userdetails = new UserDetails();
        userdetails = dataDB.loadUserInfo();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        //Code here
        userdetails.setReload(false);
        dataDB.saveUserInfo(userdetails);
        mBluetoothAdapter.disable();

        stopSelf();
    }

}
