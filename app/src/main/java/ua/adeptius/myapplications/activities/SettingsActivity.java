package ua.adeptius.myapplications.activities;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import static ua.adeptius.myapplications.activities.LoginActivity.TAG;

public class SettingsActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static Switch notifyNewTasksSwitch, switchSound,switchVibro, switchSubbota, switchVoskresenye, switchPortrait;
    SharedPreferences sPref;
    SharedPreferences.Editor settingsEditor;
    SeekBar seekBarFrom, seekBarTo;
    TextView seekBarTextViewFrom, seekBarTextViewTo;

    @Override
    public void onClick(View v) {
        if (v.equals(notifyNewTasksSwitch)) {
            ServiceTaskChecker.notifyNewTasks = notifyNewTasksSwitch.isChecked();
            settingsEditor.putString("notifyNewTasks", ""+notifyNewTasksSwitch.isChecked());
            settingsEditor.commit();
        }
        if (v.equals(switchSound)){
            ServiceTaskChecker.switchSound = switchSound.isChecked();
            settingsEditor.putString("switchSound", ""+ switchSound.isChecked());
            settingsEditor.commit();
        }
        if (v.equals(switchVibro)){
            ServiceTaskChecker.switchVibro = switchVibro.isChecked();
            settingsEditor.putString("switchVibro", ""+ switchVibro.isChecked());
            settingsEditor.commit();
        }
        if (v.equals(switchSubbota)){
            ServiceTaskChecker.switchSubbota = switchSubbota.isChecked();
            settingsEditor.putString("switchSubbota", ""+ switchSubbota.isChecked());
            settingsEditor.commit();
        }
        if (v.equals(switchVoskresenye)){
            ServiceTaskChecker.switchVoskresenye = switchVoskresenye.isChecked();
            settingsEditor.putString("switchVoskresenye", ""+ switchVoskresenye.isChecked());
            settingsEditor.commit();
        }
        if (v.equals(switchPortrait)){
            ServiceTaskChecker.switchPortrait = switchPortrait.isChecked();
            settingsEditor.putString("switchPortrait", "" + switchPortrait.isChecked());
            settingsEditor.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (ServiceTaskChecker.switchPortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Настройки");
        sPref = getSharedPreferences("settings", MODE_PRIVATE);
        settingsEditor = sPref.edit();

        seekBarFrom = (SeekBar) findViewById(R.id.seek_bar_from);
        seekBarTo = (SeekBar) findViewById(R.id.seek_bar_to);
        seekBarTextViewFrom = (TextView) findViewById(R.id.seek_bar_text_view_from);
        seekBarTextViewTo = (TextView) findViewById(R.id.seek_bar_text_view_to);
        notifyNewTasksSwitch = (Switch) findViewById(R.id.new_tasks_switch);
        notifyNewTasksSwitch.setChecked(ServiceTaskChecker.notifyNewTasks);
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

        seekBarFrom.setProgress(ServiceTaskChecker.hoursFrom);
        seekBarTo.setProgress(ServiceTaskChecker.hoursTo);
        switchSound.setChecked(ServiceTaskChecker.switchSound);
        switchVibro.setChecked(ServiceTaskChecker.switchVibro);
        switchSubbota.setChecked(ServiceTaskChecker.switchSubbota);
        switchVoskresenye.setChecked(ServiceTaskChecker.switchVoskresenye);
        switchPortrait.setChecked(ServiceTaskChecker.switchPortrait);
        seekBarTextViewFrom.setText("Оповещать о заявках с " + ServiceTaskChecker.hoursFrom + " часов");
        seekBarTextViewTo.setText("И до " + ServiceTaskChecker.hoursTo + " часов");
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar.equals(seekBarFrom)){
            ServiceTaskChecker.hoursFrom = progress;
            seekBarTextViewFrom.setText("Оповещать о заявках с " + ServiceTaskChecker.hoursFrom + " часов");
            settingsEditor.putString("hoursFrom", ""+ ServiceTaskChecker.hoursFrom);
            settingsEditor.commit();
        }
        if (seekBar.equals(seekBarTo)){
            ServiceTaskChecker.hoursTo = progress;
            seekBarTextViewTo.setText("И до " + ServiceTaskChecker.hoursTo + " часов");
            settingsEditor.putString("hoursTo", ""+ ServiceTaskChecker.hoursTo);
            settingsEditor.commit();
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}