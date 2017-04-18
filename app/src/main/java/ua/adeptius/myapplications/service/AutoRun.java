package ua.adeptius.myapplications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ua.adeptius.myapplications.util.Utilites;

import static ua.adeptius.myapplications.util.Utilites.myLog;


public class AutoRun extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent.toString().contains("BOOT_COMPLETED")){
            Utilites.myLog("Девайс загрузился. Запуск сервиса");
            context.startService(new Intent(context, BackgroundService.class));

        }else if (intent.toString().contains("AutoRun")){
            Utilites.myLog("Сработал таймер. Вызываю проверку и задаю новый");
            new Checker(context);
            new MyAlarmManager().setUpAlarm(context);
        }
    }
}


