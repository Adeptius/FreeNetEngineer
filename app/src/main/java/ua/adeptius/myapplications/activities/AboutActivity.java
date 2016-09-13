package ua.adeptius.myapplications.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("plain/text");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"adeptius@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "FreeNet Engineer V" + LoginActivity.CURRENT_VERSION);
        try {
            startActivity(i);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AboutActivity.this, "Почтовый клиент на телефоне не найден.", Toast.LENGTH_SHORT).show();
            String forToast = "Почтовый клиент на телефоне не найден.";
            Visual.makeMyToast(forToast, AboutActivity.this, getLayoutInflater(),findViewById(R.id.toast_layout_root));
        }
    }
}
