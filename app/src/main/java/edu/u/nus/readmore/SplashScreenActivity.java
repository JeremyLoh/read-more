package edu.u.nus.readmore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_SCREEN_TIMEOUT = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent boardScreenIntent =
                        new Intent(SplashScreenActivity.this, OnBoardScreenActivity.class);
                startActivity(boardScreenIntent);
                finish();
            }
        }, SPLASH_SCREEN_TIMEOUT);

    }
}
