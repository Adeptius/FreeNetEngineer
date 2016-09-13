package ua.adeptius.myapplications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static ua.adeptius.myapplications.util.Utilites.myLog;


public class AutoRun extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {
        myLog("onReceive " + intent.getAction());
        context.startService(new Intent(context, ServiceTaskChecker.class));
    }
}


