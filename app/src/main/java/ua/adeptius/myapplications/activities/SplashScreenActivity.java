package ua.adeptius.myapplications.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


import ua.adeptius.myapplications.R;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                Animation anim = AnimationUtils.loadAnimation(SplashScreenActivity.this,
                        R.anim.splash_screen_animation);
                final ImageView imageView = (ImageView) findViewById(R.id.runnerView);
                imageView.startAnimation(anim);
                try {
                    Thread.sleep(1300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Animation animGone = AnimationUtils.loadAnimation(SplashScreenActivity.this,
                        R.anim.splash_screen_animation_gone);
                imageView.startAnimation(animGone);
            }
        });

        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, 1700);
    }
}
