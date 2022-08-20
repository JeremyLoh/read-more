package edu.u.nus.readmore.Startup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import edu.u.nus.readmore.MainActivity;
import edu.u.nus.readmore.R;

public class OnBoardScreenActivity extends AppCompatActivity {
    private ViewPager screenPager;
    private TabLayout tabIndicator;
    private OnBoardScreenPagerAdapter onBoardScreenPagerAdapter;
    private Button getStartedBtn, nextBtn;
    private Animation btnAnimation;
    private TextView skipTV;
    private int position = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // making activity full screen must declare before setting content view
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check user seen tutorial before
        if (restorePrefData()) {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        }

        setContentView(R.layout.activity_onboardscreen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        // List of Screen items to be displayed

        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Users", "Our app allows you to read even if you wish to remain anonymous! However, you can enjoy a better experience while reading from logging in, so what are you waiting for?", R.drawable.guest_or_user));
        mList.add(new ScreenItem("Navigating through the app", "Fear of complex navigation through app? Fear not, navigating Read More is a piece of cake! Simply click on the burger icon circled in the image to navigate through our app!", R.drawable.tutorial_1));
        mList.add(new ScreenItem("Navigating through the app", "As an anonymous user you will only see 2 navigation items available. You can either change the theme of your app through the 'Settings' option or simply press 'Login' to have a better reading experience!", R.drawable.tutorial_2));
        mList.add(new ScreenItem("Further adventures!", "Once you are logged-in, you can edit your profile, indicate topics that are of your interest, browse your read history and even change the theme of the app!", R.drawable.tutorial_3));
        mList.add(new ScreenItem("What are you waiting for?", "Click on the 'Get Started' button to enjoy reading randomised article at your fingertip! Have fun!", R.drawable.read_more_logo));

        // instantiate views
        screenPager = findViewById(R.id.screen_viewpager);
        getStartedBtn = findViewById(R.id.btn_get_started);
        nextBtn = findViewById(R.id.btn_next);
        skipTV = findViewById(R.id.tv_skip);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);

        // setup ViewPager
        onBoardScreenPagerAdapter = new OnBoardScreenPagerAdapter(this, mList);
        screenPager.setAdapter(onBoardScreenPagerAdapter);

        // setup TabLayout with ViewPager
        tabIndicator.setupWithViewPager(screenPager);

        // button click setup
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = screenPager.getCurrentItem();
                if (position < mList.size()) {
                    position++;
                    screenPager.setCurrentItem(position);
                }

                if (position == mList.size() - 1) {
                    loadLastScreen();
                }

            }
        });

        skipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                skipTV.setClickable(false);
                savePrefsData();
                finish();
            }
        });

        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                skipTV.setClickable(false);
                // Prevent user seeing tutorial again after clicking getStarted
                savePrefsData();
                finish();
            }
        });

        // TabLayout change according to listener
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size() - 1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not used
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not used
            }
        });
    }

    private void loadLastScreen() {
        nextBtn.setVisibility(View.INVISIBLE);
        getStartedBtn.setVisibility(View.VISIBLE);
        skipTV.setVisibility(View.INVISIBLE);
        getStartedBtn.setAnimation(btnAnimation);
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        Boolean isIntroActivityOpenedBefore = pref.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.commit();
    }
}