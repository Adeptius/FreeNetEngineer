package ua.adeptius.myapplications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import static ua.adeptius.myapplications.activities.LoginActivity.TAG;


public class AutoRun extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent.getAction());
        context.startService(new Intent(context, ServiceTaskChecker.class));
    }
}


