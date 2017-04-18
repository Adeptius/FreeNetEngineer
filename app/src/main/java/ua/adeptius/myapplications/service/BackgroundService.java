package ua.adeptius.myapplications.service;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;

import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;


public class BackgroundService extends Service {

    public static NotificationManager mNotificationManager;
    public static Context context;
    public static String currentPassword;
    public static String currentLogin;
    public static ArrayList<String> newTasksIds;
    public static ArrayList<String> wasTasksIds;
    public static int wasNewTaskCountInLastTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Utilites.myLog("Сервис запущен");
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));
        new MyAlarmManager().setUpAlarm(this);
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));
        currentLogin = Settings.getCurrentLogin();
        currentPassword = Settings.getCurrentPassword();
        context = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (newTasksIds == null) newTasksIds = new ArrayList<>();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}