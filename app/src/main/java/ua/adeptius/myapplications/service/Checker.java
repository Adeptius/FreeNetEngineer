package ua.adeptius.myapplications.service;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.activities.MainActivity;
import ua.adeptius.myapplications.dao.GetInfo;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;

import static android.content.Context.MODE_PRIVATE;
import static ua.adeptius.myapplications.service.BackgroundService.mNotificationManager;
import static ua.adeptius.myapplications.service.BackgroundService.newTasksIds;
import static ua.adeptius.myapplications.service.BackgroundService.wasNewTaskCountInLastTime;
import static ua.adeptius.myapplications.service.BackgroundService.wasTasksIds;
import static ua.adeptius.myapplications.util.Utilites.myLog;

class Checker extends Thread {

    public static final int NOTIFICATION_ID = 1;
    private static Context context;

    Checker(Context context) {
        this.context = context;
        start();
    }

    @Override
    public void run() {
        if (wasTasksIds == null) {
            try {
                wasTasksIds = GetInfo.getAllTasksIds();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Settings.setsPref(context.getSharedPreferences("settings", MODE_PRIVATE));

        if (isNoticeAllowedNow()) {
//        if (true) {
            ArrayList<String> collected = getAllTasksIds();
            for (int i = 0; i < collected.size(); i++) {
                if (!wasTasksIds.contains(collected.get(i))) {
                    newTasksIds.add(collected.get(i));
                    wasTasksIds.add(collected.get(i));
                    myLog("Появилась новая заявка!!! id: " + collected.get(i));
                }
            }

            myLog("Новых заявок: " + newTasksIds.size());
            // if (newTasksIds.size() > 0) {
            if (wasNewTaskCountInLastTime != newTasksIds.size()) {
                showNotification(newTasksIds.size());
                wasNewTaskCountInLastTime = newTasksIds.size();
            }
            //}
        }
    }

    public static boolean isNoticeAllowedNow() {
        if (!Settings.isNotifyNewTasks()) return false;
        Calendar calendar = new GregorianCalendar();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if (!(Settings.getHoursFrom() <= hours && hours < Settings.getHoursTo())) return false;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (!Settings.isSwitchVoskresenye() && dayOfWeek == 1) return false;
        if (!Settings.isSwitchSubbota() && dayOfWeek == 7) return false;
        return true;
    }

    public static void showNotification(int countOfNewTasks) {
        String message = "";
        if (countOfNewTasks == 1) message = "Появилась новая заявка";
        if (countOfNewTasks >= 2) message = "Появились " + countOfNewTasks + " новые заявки";
        if (countOfNewTasks >= 5) message = "Появилось " + countOfNewTasks + " новых заявок";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo2)
                        .setContentTitle(message)
                        .setAutoCancel(true)
                        .setLights(Color.YELLOW, 3000, 3000)
                        .setContentText("Нажмите, что бы посмотреть");
        if (Settings.isSwitchSound())
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (Settings.isSwitchVibro()) mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        Intent resultIntent = new Intent(context, MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent;
        if (countOfNewTasks > 0) {
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public static ArrayList<String> getAllTasksIds() {// загрузка всех текущих айдишек
        ArrayList<String> ids = new ArrayList<>();
        try {
            ids = GetInfo.getAllTasksIds();
            Utilites.myLog("Служба получила id:");
            for (String id : ids) {
                Utilites.myLog(id);
            }

            for (int i = newTasksIds.size() - 1; i >= 0; i--) {
                myLog("Список новых заявок содержит id: " + newTasksIds.get(i));
                if (!ids.contains(newTasksIds.get(i))) {
                    myLog("Новая id уже не существует. Удаляю: " + newTasksIds.get(i));
                    newTasksIds.remove(i);
                }
            }
            for (int i = wasTasksIds.size() - 1; i >= 0; i--) {
                myLog("Список старых заявок содержит id: " + wasTasksIds.get(i));
                if (!ids.contains(wasTasksIds.get(i))) {
                    myLog("Старая id уже не существует. Удаляю: " + wasTasksIds.get(i));
                    wasTasksIds.remove(i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
}
