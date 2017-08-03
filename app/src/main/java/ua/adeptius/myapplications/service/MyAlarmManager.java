package ua.adeptius.myapplications.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;

public class MyAlarmManager {

    void setUpAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, AutoRun.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getTimer(), pendingIntent);
    }

    private long getTimer(){
        return System.currentTimeMillis() + 600000;
    }
}
