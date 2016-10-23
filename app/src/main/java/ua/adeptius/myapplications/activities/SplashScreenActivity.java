package ua.adeptius.myapplications.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.Network;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class SplashScreenActivity extends AppCompatActivity {

    private TextView statusTextView;
    private ProgressBar  progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));

        statusTextView = (TextView) findViewById(R.id.status_text_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBarInSplashScreen);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    checkAuthorizationAndInternet();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkAuthorizationAndInternet() throws InterruptedException {
        Animation anim = AnimationUtils.loadAnimation(SplashScreenActivity.this,
                R.anim.splash_screen_animation);
        final ImageView imageView = (ImageView) findViewById(R.id.runnerView);
        imageView.startAnimation(anim);
        changeStatus(0, 0);
        Thread.sleep(400);

        if (isCurrentDeviceOnline()){
            if (isWeHaveNewVersion()) {
                myLog("Есть новая версия - перехожу на страницу логина");
                changeStatus(-1, 0);
                Thread.sleep(1500);
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                SplashScreenActivity.this.finish();
                startActivity(intent);
            } else {
                changeStatus(1, 0);
                if (!Network.isAuthorizationOk(Settings.getCurrentLogin(), Settings.getCurrentPassword())) {
                    myLog("не авторизировано - перехожу на страницу логина");
                    changeStatus(1, -1);
                    Thread.sleep(1500);
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    SplashScreenActivity.this.finish();
                    startActivity(intent);
                } else {
                    myLog("Авторизация ок и обновления нет");
                    changeStatus(1, 1);
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            Animation animGone = AnimationUtils.loadAnimation(SplashScreenActivity.this,
                                    R.anim.splash_screen_animation_gone);
                            imageView.startAnimation(animGone);
                        }
                    });
                    Thread.sleep(500);
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish();
                }
            }
        }else {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    statusTextView.setText("Интернет отсутствует..");
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public boolean isCurrentDeviceOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void changeStatus(int versionStatus, int authorizationStatus) throws InterruptedException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Обновления.. ");
        if (versionStatus == -1) stringBuilder.append("Есть!");
        if (versionStatus == 0) stringBuilder.append("Проверка");
        if (versionStatus == 1) stringBuilder.append("ОК");

        stringBuilder.append("\n Авторизация.. ");
        if (authorizationStatus == -1) stringBuilder.append("Неудача");
        if (authorizationStatus == 0) stringBuilder.append("Проверка");
        if (authorizationStatus == 1) stringBuilder.append("ОК");
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(stringBuilder.toString());
            }
        });
        Thread.sleep(200);
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
        try {
            LoginActivity.newVersionIs = Integer.parseInt(LoginActivity.fileNameOfNewVersion.substring(15, 17));
        } catch (Exception e) {
        }
        if (LoginActivity.newVersionIs > LoginActivity.CURRENT_VERSION)
            System.out.println("Есть обновления");
        else System.out.println("Нет обновлений");
        return LoginActivity.newVersionIs > LoginActivity.CURRENT_VERSION;
    }
}
