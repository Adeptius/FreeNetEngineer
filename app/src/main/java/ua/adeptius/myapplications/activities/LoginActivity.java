package ua.adeptius.myapplications.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.Network;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

public class LoginActivity extends AppCompatActivity {

    public static final int CURRENT_VERSION = 21;
    public static String login;
    public static String password;
    public static final String TAG = "myLog";
    SharedPreferences sPref;
    SharedPreferences.Editor settingsEditor;

    int newVersionIs;
    String fileNameOfNewVersion;
    private EditText loginView;
    private EditText passwordView;
    private ProgressBar progressBar;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (ServiceTaskChecker.switchPortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sPref = getSharedPreferences("settings", MODE_PRIVATE);
        settingsEditor = sPref.edit();

        loginView = (EditText) findViewById(R.id.login_view);
        passwordView = (EditText) findViewById(R.id.password_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final Button enterButton = (Button) findViewById(R.id.email_sign_in_button);

        if (!isOnline()) { // если инета нет
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Программа не работает\nбез интернета.");
            builder.setCancelable(true);
            builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() { // Кнопка ОК
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Отпускает диалоговое окно
                    LoginActivity.this.finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else { // если инет есть
            try {// Проверяем наличие новой версии
                fileNameOfNewVersion = new ReadNameOfNewVersion().execute("http://e404.ho.ua/FreeNetEngineer/").get();
                if (!(fileNameOfNewVersion == null)) {
                    newVersionIs = Integer.parseInt(fileNameOfNewVersion.substring(15, 17));
                    Log.d(TAG, "Вычислил номер последней версии: " + newVersionIs + ", текущая: " + CURRENT_VERSION);
                } else {
                    String forToast = "Не могу получить доступ к файлам обновлений.";
                    Visual.makeMyToast(forToast, this, getLayoutInflater(), findViewById(R.id.toast_layout_root));
                }
            } catch (Exception e) {
                String forToast = "Не найден файл обновлений";
                Visual.makeMyToast(forToast, this, getLayoutInflater(), findViewById(R.id.toast_layout_root));
            }

            if (newVersionIs == 0 || CURRENT_VERSION >= newVersionIs) { // если обновлений нет
                // Чтение логина и пароля из настроек
                login = sPref.getString("login", "");
                password = sPref.getString("password", "");
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
                showNewVersionDialog();
            }
        }
    }

    private void showNewVersionDialog() {
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
                dialog.dismiss(); // Отпускает диалоговое окно
                String newVersionUrl = "http://e404.ho.ua/FreeNetEngineer/" + fileNameOfNewVersion;
                Network.downloadFile(newVersionUrl, context);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goToMain(String login, String password) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        settingsEditor.putString("login", login);
        settingsEditor.putString("password", password);
        settingsEditor.commit();
        LoginActivity.this.finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Slide().setDuration(800));
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }
    }

    /**
     * Проверка наличия инета
     */
    public boolean isOnline() {
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
    public class ReadNameOfNewVersion extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
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
                Log.d(TAG, "Имя файла последней версии:" + newVersionIs);
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
    }
}
