package ua.adeptius.myapplications.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class LoginActivity extends AppCompatActivity {

    public static final int CURRENT_VERSION = 20;
    public static String login;
    public static String password;


    public static int newVersionIs;
    public static String fileNameOfNewVersion;
    private EditText loginView;
    private EditText passwordView;
    private ProgressBar progressBar;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Settings.setsPref(getSharedPreferences("settings", MODE_PRIVATE));

        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        loginView = (EditText) findViewById(R.id.login_view);
        passwordView = (EditText) findViewById(R.id.password_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final Button enterButton = (Button) findViewById(R.id.email_sign_in_button);

        if (!isCurrentDeviceOnline()) { // если инета нет
            showDialogInternetIsAbsent();
        } else { // если инет есть
            if (!isWeHaveNewVersion(CURRENT_VERSION)) { // если обновлений нет
                // Чтение логина и пароля из настроек
                login = Settings.getCurrentLogin();
                password = Settings.getCurrentPassword();
                if (login != null && password != null && !login.equals("") && !password.equals("")) { // Если логин и пароль уже введён
                    if (Network.isAuthorizationOk(login, password)) {
                        goToMain(login, password);
                    }
                }

                enterButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (loginView.getText().length() == 0 || getHashedPassFromView().length() == 0) {
                            Snackbar.make(view, "Нужно заполнить все поля", Snackbar.LENGTH_LONG).show();
                        } else {
                            boolean authOk = Network.isAuthorizationOk(
                                    loginView.getText().toString(),
                                    getHashedPassFromView());
                            if (authOk) {
                                login = loginView.getText().toString();
                                password = getHashedPassFromView();
                                goToMain(login, password);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Snackbar.make(view, "Неправильный логин или пароль", Snackbar.LENGTH_LONG).show();
                                passwordView.setText("");
                            }
                        }
                    }
                });

            } else { // если обновление есть
                showDialogThatWeHaveANewVersion();
            }
        }
    }

    private void showDialogInternetIsAbsent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Программа не работает\nбез интернета.");
        builder.setCancelable(true);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LoginActivity.this.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogThatWeHaveANewVersion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder("");
        sb.append("Найдена новая версия!\n");
        sb.append("Сейчас она скачается в папку Download в память телефона.\n");
        sb.append("Удалите вручную старую версию и установите новую.");
        builder.setMessage(sb.toString());
        builder.setCancelable(false);
        builder.setPositiveButton("Скачать!", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String newVersionUrl = "http://e404.ho.ua/FreeNetEngineer/" + fileNameOfNewVersion;
                downloadFile(newVersionUrl, context);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void downloadFile(final String adress, Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        final LoginActivity loginActivity = (LoginActivity) context;
        progressDialog.setMessage("Загрузка...");
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        Exception m_error = null;
        try {
            EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(adress);
                        HttpURLConnection urlConnection;
                        InputStream inputStream;
                        int totalSize;
                        int downloadedSize;
                        byte[] buffer;
                        int bufferLength;

                        File file = null;
                        FileOutputStream fos = null;
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(true);
                        urlConnection.connect();

                        String fileName = adress.substring(adress.lastIndexOf("/") + 1);
                        file = new File("/sdcard/Download/" + fileName);
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        inputStream = urlConnection.getInputStream();
                        totalSize = urlConnection.getContentLength();
                        downloadedSize = 0;

                        buffer = new byte[1024];

                        // читаем со входа и пишем в выход,
                        // с каждой итерацией публикуем прогресс
                        while ((bufferLength = inputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, bufferLength);
                            downloadedSize += bufferLength;
                            progressDialog.setProgress((int) ((downloadedSize / (float) totalSize) * 100));
                        }
                        fos.close();
                        inputStream.close();


                        Thread.sleep(1000);
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                                loginActivity.finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void goToMain(String login, String password) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Settings.setCurrentLogin(login);
        Settings.setCurrentPassword(password);
        LoginActivity.this.finish();
        startActivity(intent);
    }

    public boolean isCurrentDeviceOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getHashedPassFromView() {
        String orig = Utilites.createMd5(passwordView.getText().toString());
        return orig + "5b4eadaed0662599d2f1cae336757aa0";
    }


    /**
     * Проверка новых версий
     * Возвращает имя файла последней версии
     */
    private boolean isWeHaveNewVersion(int currentVersion) {
        try {
            fileNameOfNewVersion = EXECUTOR.submit(new Callable<String>() {
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
                        String s = "";
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
            newVersionIs = Integer.parseInt(fileNameOfNewVersion.substring(15, 17));
            myLog("Вычислил номер последней версии: " + newVersionIs + ", текущая: " + CURRENT_VERSION);
        }catch (Exception e){
            String forToast = "Не могу получить доступ к файлам обновлений.";
            Visual.makeMyToast(forToast, this, getLayoutInflater(), findViewById(R.id.toast_layout_root));
        }
        return newVersionIs > CURRENT_VERSION;
    }
}
