package edu.u.nus.readmore.Startup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
        requestForFullScreenActivity();

        if (hasViewedAppOnboardingTutorial()) {
            startMainActivity();
        } else {
            showOnboardingTutorial();
        }
    }

    private void requestForFullScreenActivity() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private boolean hasViewedAppOnboardingTutorial() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        return pref.getBoolean("isIntroOpened", false);
    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private void showOnboardingTutorial() {
        setupDeviceScreen();
        instantiateViews();
        setupScreenItems();
    }

    private void setupDeviceScreen() {
        setContentView(R.layout.activity_onboardscreen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    private void instantiateViews() {
        screenPager = findViewById(R.id.screen_viewpager);
        getStartedBtn = findViewById(R.id.btn_get_started);
        nextBtn = findViewById(R.id.btn_next);
        skipTV = findViewById(R.id.tv_skip);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
    }

    private void setupScreenItems() {
        final List<ScreenItem> screenItems = getScreenItems();
        setupViewPager(screenItems);
        setupTabLayoutWithViewPager();
        setupButtonClicks(screenItems);
        // TabLayout change according to listener
        setupTabIndicator(screenItems.size());
    }

    @NonNull
    private List<ScreenItem> getScreenItems() {
        final List<ScreenItem> screenItems = new ArrayList<>();
        screenItems.add(new ScreenItem("Users", "Our app allows you to read even if you wish to remain anonymous! However, you can enjoy a better experience while reading from logging in, so what are you waiting for?", R.drawable.guest_or_user));
        screenItems.add(new ScreenItem("Navigating through the app", "Fear of complex navigation through app? Fear not, navigating Read More is a piece of cake! Simply click on the burger icon circled in the image to navigate through our app!", R.drawable.tutorial_1));
        screenItems.add(new ScreenItem("Navigating through the app", "As an anonymous user you will only see 2 navigation items available. You can either change the theme of your app through the 'Settings' option or simply press 'Login' to have a better reading experience!", R.drawable.tutorial_2));
        screenItems.add(new ScreenItem("Further adventures!", "Once you are logged-in, you can edit your profile, indicate topics that are of your interest, browse your read history and even change the theme of the app!", R.drawable.tutorial_3));
        screenItems.add(new ScreenItem("What are you waiting for?", "Click on the 'Get Started' button to enjoy reading randomised article at your fingertip! Have fun!", R.drawable.read_more_logo));
        return screenItems;
    }

    private void setupViewPager(List<ScreenItem> screenItems) {
        onBoardScreenPagerAdapter = new OnBoardScreenPagerAdapter(this, screenItems);
        screenPager.setAdapter(onBoardScreenPagerAdapter);
    }

    private void setupTabLayoutWithViewPager() {
        tabIndicator.setupWithViewPager(screenPager);
    }

    private void setupButtonClicks(List<ScreenItem> screenItems) {
        nextBtn.setOnClickListener(getNextButtonClickListener(screenItems));
        skipTV.setOnClickListener(getSkipIntroClickListener());
        getStartedBtn.setOnClickListener(getSkipIntroClickListener());
    }

    private void setupTabIndicator(int screenItemsSize) {
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == screenItemsSize - 1) {
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

    @NonNull
    private View.OnClickListener getSkipIntroClickListener() {
        return (View v) -> {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            skipTV.setClickable(false);
            saveViewedOnboardingIntro();
            finish();
        };
    }

    private View.OnClickListener getNextButtonClickListener(List<ScreenItem> screenItems) {
        return (View v) -> {
            position = screenPager.getCurrentItem();
            if (position < screenItems.size()) {
                position++;
                screenPager.setCurrentItem(position);
            }
            if (position == screenItems.size() - 1) {
                loadLastScreen();
            }
        };
    }

    private void loadLastScreen() {
        nextBtn.setVisibility(View.INVISIBLE);
        getStartedBtn.setVisibility(View.VISIBLE);
        skipTV.setVisibility(View.INVISIBLE);
        getStartedBtn.setAnimation(btnAnimation);
    }

    private void saveViewedOnboardingIntro() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.apply();
    }
}