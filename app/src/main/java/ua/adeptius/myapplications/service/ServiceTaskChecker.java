package ua.adeptius.myapplications.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.activities.MainActivity;
import ua.adeptius.myapplications.R;
import static ua.adeptius.myapplications.activities.LoginActivity.TAG;

public class ServiceTaskChecker extends Service {

    public static ArrayList<String> newTasksIds;
    public static ArrayList<String> wasTasksIds;
    public static String currentLogin;
    public static String currentPassword;
    public static final int NOTIFICATION_ID = 1;
    public static int wasNewTaskCountInLastTime = 0;
    static Context context;
    static NotificationManager mNotificationManager;
    SharedPreferences sPref;
    SharedPreferences.Editor settingsEditor;

    public static boolean notifyNewTasks;
    public static boolean switchSound;
    public static boolean switchVibro;
    public static boolean switchSubbota;
    public static boolean switchVoskresenye;
    public static boolean switchPortrait;
    public static int hoursFrom;
    public static int hoursTo;


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
                                Log.d(TAG, "Появилась новая заявка!!! id: " + collected.get(i));
                            }
                        }

                        Log.d(TAG, "Новых заявок: " + newTasksIds.size());
                        if (newTasksIds.size() > 0) {
                            if (wasNewTaskCountInLastTime != newTasksIds.size()) {
                                showNotification(newTasksIds.size());
                                wasNewTaskCountInLastTime = newTasksIds.size();
                            }
                        }
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
        if (!notifyNewTasks) return false;
        Calendar calendar = new GregorianCalendar();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if (!(hoursFrom <= hours && hours < hoursTo)) return false;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (!switchVoskresenye && dayOfWeek == 1) return false;
        if (!switchSubbota && dayOfWeek == 7) return false;
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
        if (switchSound)
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (switchVibro) mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

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
        Log.d(TAG, "Запрашиваю айди заявок");
        String[] request = new String[3];
        request[0] = "http://188.231.188.188/api/task_api_id.php";
        request[1] = "begun=" + currentLogin;
        request[2] = "drowssap=" + currentPassword;

        try {
            ArrayList<Map<String, String>> arrayMap = new DataBase().execute(request).get();
            for (int i = 0; i < arrayMap.size(); i++) {
                Map<String, String> map = arrayMap.get(i);
                if(map.get("id") != null){
                    ids.add(map.get("id"));
                    Log.d(TAG, "Служба получила айдишку: " + map.get("id"));
                }
            }

            for (int i = newTasksIds.size() - 1; i >= 0; i--) {
                Log.d(TAG, "Список новых заявок содержит id: " + newTasksIds.get(i));
                if (!ids.contains(newTasksIds.get(i))) {
                    Log.d(TAG, "Новая id уже не существует. Удаляю: " + newTasksIds.get(i));
                    newTasksIds.remove(i);
                }
            }
            for (int i = wasTasksIds.size() - 1; i >= 0; i--) {
                Log.d(TAG, "Список старых заявок содержит id: " + wasTasksIds.get(i));
                if (!ids.contains(wasTasksIds.get(i))) {
                    Log.d(TAG, "Старая id уже не существует. Удаляю: " + wasTasksIds.get(i));
                    wasTasksIds.remove(i);
                }
            }
        } catch (Exception ignored) {
            Log.d(TAG, "Ошибка получения id из базы в фоне");
        }
        return ids;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Сервис запущен");
        sPref = getSharedPreferences("settings", MODE_PRIVATE);
        settingsEditor = sPref.edit();
        context = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        currentLogin = sPref.getString("login", "");
        currentPassword = sPref.getString("password", "");

        try {
            hoursFrom = Integer.parseInt(sPref.getString("hoursFrom", ""));
        } catch (NumberFormatException e) {
            hoursFrom = 8;
        }
        try {
            hoursTo = Integer.parseInt(sPref.getString("hoursTo", ""));
        } catch (NumberFormatException e) {
            hoursTo = 18;
        }

        switchSound = Boolean.parseBoolean(sPref.getString("switchSound", ""));
        if (sPref.getString("switchSound", "").equals("")) switchSound = true;

        switchPortrait = Boolean.parseBoolean(sPref.getString("switchPortrait", ""));
        if (sPref.getString("switchPortrait", "").equals("")) switchPortrait = true;
        switchVibro = Boolean.parseBoolean(sPref.getString("switchVibro", ""));

        switchVoskresenye = Boolean.parseBoolean(sPref.getString("switchVoskresenye", ""));

        switchSubbota = Boolean.parseBoolean(sPref.getString("switchSubbota", ""));
        if (sPref.getString("switchSubbota", "").equals("")) switchSubbota = true;

        if (sPref.getString("notifyNewTasks", "").equals("")) notifyNewTasks = true;
        else notifyNewTasks = Boolean.parseBoolean(sPref.getString("notifyNewTasks", ""));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Сервис уничтожен: " + stopSelfResult(1));
    }
}