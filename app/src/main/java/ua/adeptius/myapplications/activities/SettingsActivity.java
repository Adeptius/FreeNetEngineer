package ua.adeptius.myapplications.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.util.Settings;

public class SettingsActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static Switch notifyNewTasksSwitch, switchSound,switchVibro, switchSubbota, switchVoskresenye, switchPortrait;
    SeekBar seekBarFrom, seekBarTo;
    TextView seekBarTextViewFrom, seekBarTextViewTo;

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

        seekBarTextViewFrom.setText(String.format("Оповещать о заявках с %d часов", Settings.getHoursFrom()));
        seekBarTextViewTo.setText(String.format("И до %d часов", Settings.getHoursTo()));
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
}