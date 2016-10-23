package ua.adeptius.myapplications.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.Ping;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;

public class PingActivity extends AppCompatActivity implements View.OnClickListener{

    public LinearLayout pingScrollView;
    public ProgressBar proBar;
    public String ip;
    private Ping ping;
    public TextView statisticView;
    private Button startButton;
    private Button stopButton;
    private EditText editText;
    private boolean pingInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pingScrollView = (LinearLayout) findViewById(R.id.ping_scroll_view);
        proBar = (ProgressBar) findViewById(R.id.ping_progress_bar);
        proBar.setVisibility(View.INVISIBLE);
        startButton = (Button) findViewById(R.id.ping_start_button);
        stopButton = (Button) findViewById(R.id.ping_stop_button);
        editText = (EditText) findViewById(R.id.ping_edit_text);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        Intent intent = getIntent();
        ip = intent.getStringExtra("ip");
        editText.setText(ip);
        if(!"".equals(ip))
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView pingView = new TextView(PingActivity.this);
        pingView.setPadding(20, 0, 0, 0);
        pingScrollView.addView(pingView, Visual.MATCH_WRAP);
        statisticView = (TextView) findViewById(R.id.textViewForStatistics);

        startPing();
    }

    private void startPing(){
        stopPing();
        ip = editText.getText().toString();
        if (!"".equals(ip)) {
            proBar.setVisibility(View.VISIBLE);
            ping = new Ping(this);
            EXECUTOR.submit(ping);
            pingInProgress = true;
            startButton.setClickable(false);
            stopButton.setClickable(true);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void stopPing(){
        if (pingInProgress)
            ping.interrupt();
        proBar.setVisibility(View.INVISIBLE);
        pingInProgress = false;
        statisticView.setText("");
        startButton.setClickable(true);
        stopButton.setClickable(false);

        for (int i = 0; i < pingScrollView.getChildCount(); i++) {
            pingScrollView.getChildAt(i).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(startButton)){
            startPing();
        }else  if (v.equals(stopButton)){
            stopPing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPing();
    }
}
