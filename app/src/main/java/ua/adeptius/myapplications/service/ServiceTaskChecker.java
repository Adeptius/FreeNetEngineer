package ua.adeptius.myapplications.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.activities.MainActivity;
import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.util.Settings;
import static ua.adeptius.myapplications.util.Utilites.myLog;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;

public class ServiceTaskChecker extends Service {

    public static ArrayList<String> newTasksIds;
    public static ArrayList<String> wasTasksIds;
    public static String currentLogin;
    public static String currentPassword;
    public static final int NOTIFICATION_ID = 1;
    public static int wasNewTaskCountInLastTime = 0;
    static Context context;
    static NotificationManager mNotificationManager;

    public int onStartCommand(Intent intent, int flags, int startId) {
        wasTasksIds = getAllTasksIds();
        if (newTasksIds == null) newTasksIds = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isNoticeAllowedNow()) {
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
                    try {
                        Thread.sleep(600000);
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
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

    private ArrayList<String> getAllTasksIds() {// загрузка всех текущих айдишек
        ArrayList<String> ids = new ArrayList<>();
        myLog("Запрашиваю айди заявок");
        String[] request = new String[3];
        request[0] = "http://188.231.188.188/api/task_api_id.php";
        request[1] = "begun=" + currentLogin;
        request[2] = "drowssap=" + currentPassword;

        try {
            ArrayList<Map<String, String>> arrayMap = EXECUTOR.submit(new DataBase(request)).get();
            for (int i = 0; i < arrayMap.size(); i++) {
                Map<String, String> map = arrayMap.get(i);
                if(map.get("id") != null){
                    ids.add(map.get("id"));
                    myLog("Служба получила айдишку: " + map.get("id"));
                }
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
        } catch (Exception ignored) {
            myLog("Ошибка получения id из базы в фоне");
        }
        return ids;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myLog("Сервис запущен");
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));
        currentLogin = Settings.getCurrentLogin();
        currentPassword = Settings.getCurrentPassword();
        context = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLog("Сервис уничтожен: " + stopSelfResult(1));
    }
}