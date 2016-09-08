package ua.adeptius.myapplications.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.Ping;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;

public class PingActivity extends AppCompatActivity {

    public LinearLayout pingScrollView;
    public ProgressBar proBar;
    public String ip;
    private Ping ping;
    public TextView statisticView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pingScrollView = (LinearLayout) findViewById(R.id.ping_scroll_view);
        proBar = (ProgressBar) findViewById(R.id.progressBar2);
        Intent intent = getIntent();
        ip = intent.getStringExtra("ip");

        TextView pingView = new TextView(PingActivity.this);
        pingView.setPadding(20, 0, 0, 0);
        pingScrollView.addView(pingView, Visual.MATCH_WRAP);
        statisticView = (TextView) findViewById(R.id.textViewForStatistics);

        ping = new Ping(this);
        EXECUTOR.submit(ping);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ping.interrupt();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ping.interrupt();
    }
}
