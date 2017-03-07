package ua.adeptius.myapplications.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.util.Settings;

import static ua.adeptius.myapplications.util.Utilites.myLog;

public class CableTestActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private EditText edTextSwitch, edTextPort;
    private TextView tvCableResult;
    private ProgressBar proBar;
    private Button btnTestCable, btnPortInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cable_test);
        if (Settings.isSwitchPortrait())
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        edTextSwitch = (EditText) findViewById(R.id.ed_text_switch);
        edTextPort = (EditText) findViewById(R.id.ed_text_port);
        tvCableResult = (TextView) findViewById(R.id.tv_cable_result);
        proBar = (ProgressBar) findViewById(R.id.progressBar3);
        btnTestCable = (Button) findViewById(R.id.btn_test_cable);
        btnPortInfo = (Button) findViewById(R.id.button_info);
        edTextPort.setOnEditorActionListener(this);

        Intent intent = getIntent();
        String switc = intent.getStringExtra("switch");
        String port = intent.getStringExtra("port");

        if (switc.equals("Неизвестно")) edTextSwitch.setText("");
        else edTextSwitch.setText(switc.substring(4));

        if (port.equals("Неизвестно")) edTextPort.setText("");
        else edTextPort.setText(port);

        if (!edTextSwitch.getText().toString().equals(""))
            portInfo(edTextSwitch.getText().toString(), edTextPort.getText().toString());


        tvCableResult.setGravity(Gravity.CENTER);

        btnTestCable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = edTextPort.getText().toString();
                if (port.equals("25") || port.equals("26") || port.equals("27") || port.equals("28")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CableTestActivity.this);
                    builder.setMessage("Осторожно!\nЭто гигабитный порт!\nТочно проверить?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cableTest(edTextSwitch.getText().toString(), edTextPort.getText().toString());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    cableTest(edTextSwitch.getText().toString(), edTextPort.getText().toString());
                }
            }
        });

        btnPortInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                portInfo(edTextSwitch.getText().toString(), edTextPort.getText().toString());
            }
        });

        if (edTextSwitch.getText().toString().equals("")) { //Если нет инфы и ввести надо вручную
            edTextSwitch.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { //если ввели порт и нажали ОК
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            portInfo(edTextSwitch.getText().toString(), edTextPort.getText().toString());
        }
        return false;
    }

    void cableTest(String ip, String port) {
        String[] request = new String[5];
        request[0] = "http://188.231.188.188/api/task_tdr_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "ip=172." + ip;
        request[4] = "port=" + port;
        new CableTest().execute(request);
}

    void portInfo(String ip, String port) {
        String[] request = new String[5];
        request[0] = "http://188.231.188.188/api/task_shport_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "ip=172." + ip;
        request[4] = "port=" + port;
        new PortInfo().execute(request);
    }

    class PortInfo extends AsyncTask<String, Void, Map<String, String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            proBar.setVisibility(View.VISIBLE);
            btnTestCable.setEnabled(false);
            btnPortInfo.setEnabled(false);
            tvCableResult.setText("");
        }

        @Override
        protected Map<String, String> doInBackground(String... params) {
            try {
                String url = params[0];
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                String urlParameters = "";
                for (int i = 1; i < params.length; i++) {
                    if (i != 1) urlParameters += "&"; // добавлять "&" в начале не нужно
                    urlParameters += params[i];
                }
                myLog("Передаю параметры: " + urlParameters);

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String response = in.readLine();
                myLog("Получил ответ: " + response);

                response = response.substring(2, response.length() - 2);
                System.out.println(response);


                String s = response.substring(1, response.length() - 2).replace("macs\":{\"", "");
                myLog("s = " + s);
                if (s.equals("t respondin")){
                    Map<String, String> map = new HashMap<>();
                    map.put("fiz", "");
                    map.put("adm", "");
                    return map;
                }
                Map<String, String> map = new HashMap<>();
                String[] pairs = s.split("\",\"");
                for (int j = 0; j < pairs.length; j++) {
                    String[] keys = pairs[j].split("\":\"");
                    if (keys.length == 1) {
                        map.put(keys[0], "нет данных"); // Если у ключа нет значения
                    } else {
                        map.put(keys[0], keys[1]);
                        myLog("Добавляю в мапу " + keys[0] + " " + keys[1] );
                    }
                }

                return map;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new HashMap<>();
        }

        @Override
        protected void onPostExecute(Map<String, String> arr) {
            super.onPostExecute(arr);
            StringBuilder sb = new StringBuilder("");


            if (arr.size() == 0) { // Если аррей оказался пустой
                tvCableResult.setText("Ошибка.\nВероятно нет интернета");
            } else { // если строки имеются
                if (arr.containsKey("error")) {
                    sb.append("Неправильный IP");
                }else{
                    String link = arr.get("fiz");
                    String adm = arr.get("adm");
                    arr.remove("fiz");
                    arr.remove("adm");
                    if (link.equals("") && adm.equals("")) {
                        sb.append("Либо свич лежит,\nлибо его не существует");
                    } else {
                        if (arr.get("mac1").equals("empty")) arr.remove("mac1");
                        myLog("ключей после удаления: " + arr.size());
                        if (link.equals("1")) {
                            sb.append("Линк есть!\n");
                            if (arr.size() == 0) sb.append("Мака на порту нет.\n");
                            else {
                                if (arr.size() == 1) {
                                    sb.append("Мак есть:\n");
                                    sb.append(arr.get("mac1"));
                                } else {
                                    sb.append("Маки на порту:\n");
                                    for (String s : arr.values()) {
                                        sb.append(s + "\n");
                                    }
                                }
                            }
                        }
                        if (link.equals("2")) {
                            if (adm.equals("1")) {
                                sb.append("Линка нет\n(порт не сложен)");
                            } else if (adm.equals("2")) sb.append("Порт сложен\n");
                        }
                    }
                }
                tvCableResult.setText(sb.toString());
            }
            proBar.setVisibility(View.INVISIBLE);
            btnTestCable.setEnabled(true);
            btnPortInfo.setEnabled(true);
        }
    }

    class CableTest extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            proBar.setVisibility(View.VISIBLE);
            btnTestCable.setEnabled(false);
            btnPortInfo.setEnabled(false);
            tvCableResult.setText("");
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> list = new ArrayList<>();
            try {
                String url = params[0];
                myLog("Подключаюсь по ссылке: " + url);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                String urlParameters = "";
                for (int i = 1; i < params.length; i++) {
                    if (i != 1) urlParameters += "&"; // добавлять "&" в начале не нужно
                    urlParameters += params[i];
                }
                myLog("Передаю параметры: " + urlParameters);

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String s;
                while ((s = in.readLine()) != null) {
                    list.add(s);
                }
                return list;
            } catch (IOException e) {
                return new ArrayList<>();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> arr) {
            super.onPostExecute(arr);
            StringBuilder sb = new StringBuilder("");
            for (String s : arr) {
                myLog("Пришел ответ: " + s);
            }

            if (arr.size() == 0) { // Если аррей оказался пустой
                tvCableResult.setText("Ошибка.\nВероятно нет интернета");
            } else { // если строки имеются

                if (arr.get(0).length() == 44 && arr.get(0).substring(17, 44).equals("Не верно введен IP адрес..."))
                    tvCableResult.setText("Не верно введен IP адрес...");
                if (arr.get(0).length() == 34 && arr.get(0).substring(17, 34).equals("not responding..."))
                    tvCableResult.setText("Ошибка. Возможно:\n1.Свич лежит.\n2. Его не существует.\n3.Тест проводится слишком часто.");
                if (arr.get(0).substring(17, 23).equals("Данная"))
                    tvCableResult.setText("Эта модель коммутатора не поддерживается.");
                if (arr.get(0).substring(17, 26).equals("Результат") && arr.size() == 3) {
                    tvCableResult.setText("Ошибка. Возможно порт оптический.");
                }

                /**
                 * Если это RaiseCom
                 */
                if (arr.get(0).substring(17, 26).equals("Результат") && arr.size() == 9) {
                    for (int i = 4; i < 6; i++) {
                        myLog(arr.get(i));
                        String para = i - 3 + " пара: ";
                        String s = para + arr.get(i).substring(20) + " ";
                        s = s + arr.get(i + 2).substring(17);
                        s = s.replace("Состояние ", "");
                        s = s.replace("Длинна ", "");
                        myLog("строка:" + s);
//                        if (!((s.length() == 29 || s.length() == 23) && s.substring(8, 18).equals("неизвестно"))) { // удаление гигабитных пар на 100мбит порту
                            s = s.replace("open(2)", "Обрыв на");
                            s = s.replace("short(3)", "Замыкание на");
                            s = s.replace("impMismatch (4)", "Проблема с жилами на ");
                            s = s.replace("ok(1)", "Кабель цел.");
                            s = s.replace("Кабель цел. 0 м.", "Кабель цел.");
                            s = s.replace("no-cable(7) неизвестно", "Нет кабеля.");
                            sb.append(s).append("\n");
//                        }
                    }
                    tvCableResult.setText(sb.toString());
                }

                /**
                 * Если это старый ZTE или д-линк DES-3526
                 */
                if (arr.get(0).substring(17, 26).equals("Результат") && arr.size() == 13) {
                    myLog("это старый зте или д-линк");
                    // Тут переделываются строки д-линка в строки зте, так как принцип одинаков, но строки разные
                    for (int i = 0; i < arr.size(); i++) {
                        String dlink = arr.get(i);
                        dlink = dlink.replace("open(1)", "open(2)");
                        dlink = dlink.replace("other(8)", "неизвестно");
                        dlink = dlink.replace("ok(0)", "ok(1)");
                        arr.set(i, dlink);
                    }

                    for (int i = 4; i < 8; i++) {
                        myLog(arr.get(i));
                        String para = i - 3 + " пара: ";
                        String s = para + arr.get(i).substring(20) + " ";
                        s = s + arr.get(i + 4).substring(17);
                        s = s.replace("Состояние ", "");
                        s = s.replace("Длинна ", "");
                        myLog("строка:" + s);
                        if (!((s.length() == 29 || s.length() == 23) && s.substring(8, 18).equals("неизвестно"))) { // удаление гигабитных пар на 100мбит порту
                            s = s.replace("open(2)", "Обрыв на");
                            s = s.replace("short(3)", "Замыкание на");
                            s = s.replace("impMismatch (4)", "Проблема с жилами на ");
                            s = s.replace("ok(1) неизвестно", "Кабель цел.");
                            s = s.replace("ok(1)  м.", "Кабель цел.");
                            s = s.replace("no-cable(7) неизвестно", "Нет кабеля.");
                            sb.append(s).append("\n");
                        }
                    }
                    tvCableResult.setText(sb.toString());
                }

                /**
                 * если это LINKSYS
                 */
                if (arr.get(0).substring(17, 26).equals("Connected")) {
                    myLog("это линксис");
                    String s = "";
                    for (int i = 0; i < arr.size(); i++) {
                        if (arr.get(i).length() > 12) {
                            if (arr.get(i).substring(0, 13).equals("Port test fai")) {
                                s = arr.get(i).substring(0, 13);
                            }
                            if (arr.get(i).substring(0, 13).equals("Cable on port")) {
                                s = arr.get(i).substring(14);
                            }
                        }
                    }
                    s = s.substring(s.indexOf(" ") + 1);
                    s = s.replace("is open at ", "Обрыв на ");
                    s = s.replace("has short circuit at ", "Замыкание на ");
                    s = s.replace("has impedance mismatch at", "Проблема с жилами на ");
                    s = s.replace("is good", "Кабель цел.");
                    s = s.replace("is not connected", "Кабель не подключен");
                    s = s.replace(" m", "м.");
                    s = s.replace("test fai", "Неудача. Возможно замыкание или порт неисправен.");
                    sb.append(s);
                    tvCableResult.setText(sb.toString());
                }
            }
            proBar.setVisibility(View.INVISIBLE);
            btnTestCable.setEnabled(true);
            btnPortInfo.setEnabled(true);
        }
    }
}
