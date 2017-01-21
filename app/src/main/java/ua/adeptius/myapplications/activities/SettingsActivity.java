package ua.adeptius.myapplications.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Map;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.util.Settings;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;

public class SettingsActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static Switch notifyNewTasksSwitch, switchSound,switchVibro, switchSubbota, switchVoskresenye, switchPortrait;
    SeekBar seekBarFrom, seekBarTo;
    TextView seekBarTextViewFrom, seekBarTextViewTo;
    EditText editPhone, editPin;
    Button buttonPhone, buttonPin;
    ProgressDialog progressDialog;
    LinearLayout mainLayout;

    @Override
    public void onClick(View v) {
        if (v.equals(notifyNewTasksSwitch)) {
            Settings.setNotifyNewTasks(notifyNewTasksSwitch.isChecked());
        }
        if (v.equals(switchSound)){
            Settings.setSwitchSound(switchSound.isChecked());
        }
        if (v.equals(switchVibro)){
            Settings.setSwitchVibro(switchVibro.isChecked());
        }
        if (v.equals(switchSubbota)){
            Settings.setSwitchSubbota(switchSubbota.isChecked());
        }
        if (v.equals(switchVoskresenye)){
            Settings.setSwitchVoskresenye(switchVoskresenye.isChecked());
        }
        if (v.equals(switchPortrait)){
            Settings.setSwitchPortrait(switchPortrait.isChecked());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Настройки");

        seekBarFrom = (SeekBar) findViewById(R.id.seek_bar_from);
        seekBarTo = (SeekBar) findViewById(R.id.seek_bar_to);
        seekBarTextViewFrom = (TextView) findViewById(R.id.seek_bar_text_view_from);
        seekBarTextViewTo = (TextView) findViewById(R.id.seek_bar_text_view_to);
        notifyNewTasksSwitch = (Switch) findViewById(R.id.new_tasks_switch);
        switchSound = (Switch) findViewById(R.id.switchSound);
        switchVibro = (Switch) findViewById(R.id.switchVibro);
        switchSubbota = (Switch) findViewById(R.id.switchSubbota);
        switchVoskresenye = (Switch) findViewById(R.id.switchVoskresenye);
        switchPortrait = (Switch) findViewById(R.id.switchPortrait);

        notifyNewTasksSwitch.setOnClickListener(this);
        seekBarTextViewFrom.setOnClickListener(this);
        seekBarTextViewTo.setOnClickListener(this);
        seekBarFrom.setOnSeekBarChangeListener(this);
        seekBarTo.setOnSeekBarChangeListener(this);
        switchSound.setOnClickListener(this);
        switchVibro.setOnClickListener(this);
        switchSubbota.setOnClickListener(this);
        switchVoskresenye.setOnClickListener(this);
        switchPortrait.setOnClickListener(this);

        seekBarFrom.setProgress(Settings.getHoursFrom());
        seekBarTo.setProgress(Settings.getHoursTo());
        switchSound.setChecked(Settings.isSwitchSound());
        switchVibro.setChecked(Settings.isSwitchVibro());
        switchSubbota.setChecked(Settings.isSwitchSubbota());
        switchVoskresenye.setChecked(Settings.isSwitchVoskresenye());
        notifyNewTasksSwitch.setChecked(Settings.isNotifyNewTasks());
        switchPortrait.setChecked(Settings.isSwitchPortrait());

        mainLayout = (LinearLayout) findViewById(R.id.settings_layout);

        seekBarTextViewFrom.setText(String.format("Оповещать о заявках с %d часов", Settings.getHoursFrom()));
        seekBarTextViewTo.setText(String.format("И до %d часов", Settings.getHoursTo()));

        editPin = (EditText) findViewById(R.id.editText_pin);
        editPhone = (EditText) findViewById(R.id.editText_phone);

        if (!Settings.getPhone().equals("")){
            editPhone.setText(Settings.getPhone());
        }
        if (!Settings.getPin().equals("")){
            editPin.setText(Settings.getPin());
        }

        buttonPhone = (Button) findViewById(R.id.button_phone);
        buttonPin = (Button) findViewById(R.id.button_pin);

        editPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (editPhone.getText().toString().equals("Телефон")){
                    editPhone.setText("");
                }
            }
        });
        editPin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (editPin.getText().toString().equals("Пин")){
                    editPin.setText("");
                }
            }
        });

        buttonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              hideKeyboard();
                savePhone(editPhone.getText().toString());
            }
        });

        buttonPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                savePin(editPin.getText().toString());
            }
        });
        hideKeyboard();
    }

    private void savePin(final String pin) {
        progressDialogShow();
        hideKeyboard();

        String[] request = new String[5];
        request[0] = "http://188.231.188.188/api/gerkon_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "tel=" + Settings.getPhone();
        request[4] = "pin=" + pin;

        final DataBase dataBase = new DataBase(request);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Map<String, String> map = dataBase.call().get(0);
                    int code = Integer.parseInt(map.get("respin"));
                    if (code==1){
                        Settings.setPin(pin);
                        makeMySnackBar("Сохранено");
                    }else {
                        makeMySnackBar("Такого пин нет в базе, или неправильный телефон");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    makeMySnackBar("Ошибка. Вероятно нет интернета");
                }finally {
                    hideKeyboard();
                    hideProgressDialog();
                }
            }
        });
    }

    private void savePhone(final String phone) {
        progressDialogShow();
        hideKeyboard();

        String[] request = new String[4];
        request[0] = "http://188.231.188.188/api/gerkon_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "tel=" + phone;

        final DataBase dataBase = new DataBase(request);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Map<String, String> map = dataBase.call().get(0);
                    int code = Integer.parseInt(map.get("resphone"));
                    if (code==1){
                        Settings.setPhone(phone);
                        makeMySnackBar("Сохранено");
                    }else {
                        makeMySnackBar("Номер не зарегистрирован в базе");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    makeMySnackBar("Ошибка. Вероятно нет интернета");
                }finally {
                    hideKeyboard();
                    hideProgressDialog();
                }
            }
        });
    }

    private void makeMySnackBar(final String message){
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(seekBarFrom)){
            Settings.setHoursFrom(progress);
            seekBarTextViewFrom.setText(String.format("Оповещать о заявках с %d часов", Settings.getHoursFrom()));
        }
        if (seekBar.equals(seekBarTo)){
            Settings.setHoursTo(progress);
            seekBarTextViewTo.setText(String.format("И до %d часов", Settings.getHoursTo()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    protected void progressDialogShow() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(SettingsActivity.this, R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Подождите..");
                progressDialog.show();
            }
        });
    }

    protected void hideProgressDialog() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}