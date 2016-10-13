package ua.adeptius.myapplications.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.util.Utilites;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation);
        ImageView imageView = (ImageView) findViewById(R.id.runnerView);
        imageView.startAnimation(anim);

        Utilites.HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);
    }
}
