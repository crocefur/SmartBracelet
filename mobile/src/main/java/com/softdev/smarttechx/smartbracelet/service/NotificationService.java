package com.softdev.smarttechx.smartbracelet.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.softdev.smarttechx.smartbracelet.ConnectActivity;
import com.softdev.smarttechx.smartbracelet.R;
import com.softdev.smarttechx.smartbracelet.SplashActivity;
import com.softdev.smarttechx.smartbracelet.data.SaveData;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;

public class NotificationService extends BroadcastReceiver {

    final static int RQS_1 = 1;
    private static final int MY_NOTIFICATION_ID = 1;
    private final String myAPP = "SmartBracelet";
    NotificationManager notificationManager;
    Notification myNotification;
    int daycount;
    String count;
    SaveData dataDB;
    UserDetails userdetails;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, " You get a new notification from URSA", Toast.LENGTH_LONG).show();
        setAlarm(context, System.currentTimeMillis() + 24 * 3600 * 1000);
        //setAlarm(context,System.currentTimeMillis() +  60 * 1000);
        Intent notificationIntent = new Intent(context, SplashActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        myNotification = new NotificationCompat.Builder(context)
                .setContentTitle("It's day to show your performance")
                .setContentText("Please click to sync data")
                .setTicker("Notification!")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }


    private void setAlarm(Context context, Long TimeInMillis) {
        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, TimeInMillis, pendingIntent);

    }

}
