package ua.adeptius.myapplications.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.HashMap;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.Network;
import ua.adeptius.myapplications.dao.Web;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;

public class LoginActivity extends AppCompatActivity {

    public static String login;
    public static String password;
    private EditText loginView;
    private EditText passwordView;
    private ProgressBar progressBar;

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
                            check(login, passwordView.getText().toString());
                            goToMain(login, password);
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Snackbar.make(view, "Неправильный логин или пароль", Snackbar.LENGTH_LONG).show();
                            passwordView.setText("");
                        }
                    }
                }
            });
        }
    }

    private void showDialogInternetIsAbsent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Программа не работает\nбез интернета.");
        builder.setCancelable(false);
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

    private static void check(final String begun, final String drowssap){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("begun", begun);
                    map.put("drowssap", drowssap);
                    Web.sendPost("http://195.181.208.31/web/begun/checkPass", map);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
