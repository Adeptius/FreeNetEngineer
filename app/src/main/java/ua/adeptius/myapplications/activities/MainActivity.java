package ua.adeptius.myapplications.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.graphics.Color;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.connection.Network;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    public static ArrayList<Task> tasks = null;
    public static LinearLayout mainScrollView;
    public static final int ONLY_MY_TASK = 1;
    public static final int ONLY_NOT_ASSIGNED_TASK = 3;
    public static int needToShow;
    public SwipeRefreshLayout refreshLayout;
    private ArrayList<View> currentViews;


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TaskActivity.class);
        String a = "" + v.getId();
        intent.putExtra("position", a);

        String idOfShoosenTask = tasks.get(v.getId()).getId();
        if (ServiceTaskChecker.newTasksIds.contains(idOfShoosenTask)) {
            ServiceTaskChecker.newTasksIds.remove(idOfShoosenTask);
            v.setBackgroundColor(Visual.CORPORATE_COLOR);
        }// если id заявки на которую мы клацнули есть в списке id новых заявок -
        // то открывая - удаляем её из списка новых и делаем её цвет синим
        if (!ServiceTaskChecker.wasTasksIds.contains(idOfShoosenTask))
            ServiceTaskChecker.wasTasksIds.add(idOfShoosenTask);
        ServiceTaskChecker.wasNewTaskCountInLastTime = ServiceTaskChecker.newTasksIds.size();
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        refresh();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLog("загружен мэйн активити");
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainScrollView = (LinearLayout) findViewById(R.id.main_scroll_view);//главный экран
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(Color.GREEN, Color.BLUE, Color.parseColor("#FF9900"));

        if (!isMyServiceRunning(ServiceTaskChecker.class))
            startService(new Intent(MainActivity.this, ServiceTaskChecker.class));

        if (ServiceTaskChecker.newTasksIds == null) {
            ServiceTaskChecker.newTasksIds = new ArrayList<>();
        }
        if (ServiceTaskChecker.wasTasksIds == null) {
            ServiceTaskChecker.wasTasksIds = new ArrayList<>();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        needToShow = ONLY_MY_TASK;

        if (!isCurrentDeviceOnline())
            showDialogInternetIsAbsent();

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                if (isWeHaveNewVersion()){
                    myLog("Есть новая версия - перехожу на страницу логина");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class );
                    MainActivity.this.finish();
                    startActivity(intent);
                }else if(!Network.isAuthorizationOk(Settings.getCurrentLogin(),Settings.getCurrentPassword())){
                    myLog("не авторизировано - перехожу на страницу логина");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class );
                    MainActivity.this.finish();
                    startActivity(intent);
                }
            }
        });

        refresh();
    }

    public void refresh() {
        interruptViewUpdate = true;
        if (needToShow == ONLY_MY_TASK) {
            Visual.CORPORATE_COLOR = Color.parseColor("#3f51b5");
            setTitle("Мои заявки");
        }
        if (needToShow == ONLY_NOT_ASSIGNED_TASK) {
            Visual.CORPORATE_COLOR = Color.parseColor("#757575");
            setTitle("Не назначенные");
        }
        mainScrollView.removeAllViews();
        refreshLayout.setRefreshing(true);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                loadAllTasks();
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        refreshMainScreen();

                    }
                });
            }
        });
    }

    public void refreshMainScreen() {
        // Добавляем превью каждой заявки в наш лейаут
        if (!(tasks.size() == 0)) {
            ArrayList<View> views = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                LinearLayout horizontalVievForTask = Visual.getHeader(tasks.get(i), this);
                if (!ServiceTaskChecker.wasTasksIds.contains(tasks.get(i).getId()))
                    ServiceTaskChecker.wasTasksIds.add(tasks.get(i).getId());
                horizontalVievForTask.setOnClickListener(this);
                horizontalVievForTask.setId(i);
                views.add(horizontalVievForTask);
                //Просто отступ между заявками
                TextView otstup = new TextView(this);
                otstup.setTextSize(7);
                otstup.setLayoutParams(Visual.MATCH_WRAP);
                views.add(otstup);
            }
            animateInTasks(views);
            currentViews = views;


        } else { // Сообщение: нет заявок
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText("Заявок нет.");
                    tv.setTextSize(30);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextColor(Color.BLUE);
                    mainScrollView.addView(tv, Visual.MATCH_WRAP);
                    refreshLayout.setRefreshing(false);
                    mainScrollView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_screen_trans));
                }
            });
        }
    }

    private volatile boolean interruptViewUpdate;

    void animateInTasks(final ArrayList<View> views) {
        interruptViewUpdate = false;
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < views.size(); i++) {
                    final View v = views.get(i);
                    if (!interruptViewUpdate) {

                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!interruptViewUpdate) {
                                    mainScrollView.addView(v);
                                    v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_screen_trans));
                                }
                            }
                        });
                        if (!interruptViewUpdate && i % 2 != 0) {
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        });
    }

    public void loadAllTasks() {
        try {
            tasks = null;
            String[] request = null;
            if (needToShow == ONLY_MY_TASK) { // Тут должно быть ONLY_MY_TASK
                myLog("Запрашиваю назначенные заявки");
                request = new String[3];
                request[0] = "http://188.231.188.188/api/task_api.php";
                request[1] = "begun=" + Settings.getCurrentLogin();
                request[2] = "drowssap=" + Settings.getCurrentPassword();
            }

            if (needToShow == ONLY_NOT_ASSIGNED_TASK) { // тут должно быть ONLY_NOT_ASSIGNED_TASK
                myLog("Запрашиваю не назначенные заявки");
                request = new String[4];
                request[0] = "http://188.231.188.188/api/notassigned_api.php";
                request[1] = "begun=" + Settings.getCurrentLogin();
                request[2] = "drowssap=" + Settings.getCurrentPassword();
                request[3] = "taccepted=notassigned";
            }

            ArrayList<Map<String, String>> arrayMap = EXECUTOR.submit(new DataBase(request)).get();
            tasks = new ArrayList<>(); // будем все таски пихать сюда
            if (!arrayMap.get(0).containsKey("error")) { // если нам не пришло [{"error":"No_tasks"}]
                myLog("Заявки есть. Создаю обьекты - заявки");
                for (int i = 0; i < arrayMap.size(); i++) { // Для каждого обьекта "заявка"
                    Map<String, String> temp = arrayMap.get(i);
                    tasks.add(Utilites.createTask(temp));
                }
            }
        } catch (Exception e) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText("Ошибка. Вероятно нет интернета");
                    tv.setTextSize(20);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextColor(Color.RED);
                    mainScrollView.addView(tv, Visual.MATCH_WRAP);
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCurrentDeviceOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void showDialogInternetIsAbsent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Программа не работает\nбез интернета.");
        builder.setCancelable(false);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isWeHaveNewVersion() {
        try {
            LoginActivity.fileNameOfNewVersion = EXECUTOR.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL("http://e404.ho.ua/FreeNetEngineer/");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        InputStream stream = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(stream));
                        String s;
                        String newVersionIs = null;
                        while ((s = reader.readLine()) != null) {
                            if (s.length() > 82) {
                                if (s.substring(72, 80).equals("<a href=")) {
                                    newVersionIs = s.substring(81, s.indexOf(".apk") + 4);
                                }
                            }
                        }
                        Log.d("====FreeNetEngineer====", "Имя файла последней версии:" + newVersionIs);
                        return newVersionIs;
                    } catch (MalformedURLException e) {
                        return "-1";
                    } catch (IOException e) {
                        return "-1";
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).get();
        } catch (Exception e) {
            String forToast = "Не найден файл обновлений";
            Visual.makeMyToast(forToast, this, getLayoutInflater(), findViewById(R.id.toast_layout_root));
        }


        try{
            LoginActivity.newVersionIs = Integer.parseInt(LoginActivity.fileNameOfNewVersion.substring(15, 17));
        }catch (Exception e){
        }
        return LoginActivity.newVersionIs > LoginActivity.CURRENT_VERSION;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_btn_my_tasks) {
            needToShow = ONLY_MY_TASK;
            refresh();
        }
        if (id == R.id.nav_port) {
            Intent intent = new Intent(this, CableTestActivity.class);
            intent.putExtra("switch", "Неизвестно");
            intent.putExtra("port", "Неизвестно");
            startActivity(intent);

        } else if (id == R.id.btn_not_assigned) {
            needToShow = ONLY_NOT_ASSIGNED_TASK;
            refresh();

        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_info) {
            startActivity(new Intent(this, AboutActivity.class));

        } else if (id == R.id.nav_exit) {
            stopService(new Intent(this, ServiceTaskChecker.class));
            Settings.eraseLoginAndPassword();
            MainActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (currentViews != null) {
            mainScrollView.removeAllViews();
            animateInTasks(currentViews);
        }
        if (ServiceTaskChecker.newTasksIds.size() > 0){
            refresh();
        }
    }
}
