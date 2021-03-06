package ua.adeptius.myapplications.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.dao.GetInfo;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.service.BackgroundService;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static ArrayList<Task> tasks = null;
    public static LinearLayout mainScrollView;
    public static final int ONLY_MY_TASK = 1;
    public static final int ONLY_NOT_ASSIGNED_TASK = 3;
    public static int needToShow;
    public SwipeRefreshLayout refreshLayout;
    private ArrayList<View> currentViews;

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

        if (!isMyServiceRunning(BackgroundService.class))
            startService(new Intent(MainActivity.this, BackgroundService.class));

        if (BackgroundService.newTasksIds == null) {
            BackgroundService.newTasksIds = new ArrayList<>();
        }
        if (BackgroundService.wasTasksIds == null) {
            BackgroundService.wasTasksIds = new ArrayList<>();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try { // увеличение поля захвата закрытой панели
            Field mDragger = drawer.getClass().getDeclaredField("mLeftDragger");//mRightDragger for right obviously
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawer);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj, edge * 5); //optimal value as for me, you may set any constant in dp
        }catch (Exception ignored){}

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YandexActivity.tasks = tasks;
                Intent intent = new Intent(MainActivity.this, YandexActivity.class);
                startActivity(intent);
            }
        });

        needToShow = ONLY_MY_TASK;
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
        if (tasks.size() != 0) {
            ArrayList<View> views = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                LinearLayout horizontalVievForTask = Visual.getHeader(tasks.get(i), this);
                if (!BackgroundService.wasTasksIds.contains(tasks.get(i).getId()))
                    BackgroundService.wasTasksIds.add(tasks.get(i).getId());
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
                                    try{
                                        mainScrollView.addView(v);
                                        v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_screen_trans));
                                    }catch (IllegalStateException e){
                                        e.printStackTrace();
                                        interruptViewUpdate = true;
                                    }
                                }
                            }
                        });
                        if (!interruptViewUpdate && i % 2 != 0) {
                            try {
                                int interval = 80;
                                if(views.size()>20) interval = 40;
                                Thread.sleep(interval);
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
                tasks = GetInfo.getAssignedTask();
            }

            if (needToShow == ONLY_NOT_ASSIGNED_TASK) { // тут должно быть ONLY_NOT_ASSIGNED_TASK
                tasks = GetInfo.getNotAssignedTask();
            }

        } catch (Exception e) {
            tasks = new ArrayList<>();
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
    public void onClick(View v) {
        Intent intent = new Intent(this, TaskActivity.class);
        String a = "" + v.getId();
        intent.putExtra("position", a);

        String idOfShoosenTask = tasks.get(v.getId()).getId();
        if (BackgroundService.newTasksIds.contains(idOfShoosenTask)) {
            BackgroundService.newTasksIds.remove(idOfShoosenTask);
            v.setBackgroundColor(Visual.CORPORATE_COLOR);
        }// если id заявки на которую мы клацнули есть в списке id новых заявок -
        // то открывая - удаляем её из списка новых и делаем её цвет синим
        if (!BackgroundService.wasTasksIds.contains(idOfShoosenTask))
            BackgroundService.wasTasksIds.add(idOfShoosenTask);
        BackgroundService.wasNewTaskCountInLastTime = BackgroundService.newTasksIds.size();
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
            stopService(new Intent(this, BackgroundService.class));
            Settings.eraseLoginAndPassword();
            MainActivity.this.finish();

        }else if (id == R.id.nav_ping) {
            Intent intent = new Intent(this, PingActivity.class);
            intent.putExtra("ip", "");
            startActivity(intent);
        }else if (id == R.id.nav_gerkon) {
            Intent intent = new Intent(this, GerkonActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainScrollView.removeAllViews();
        animateInTasks(currentViews);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}
