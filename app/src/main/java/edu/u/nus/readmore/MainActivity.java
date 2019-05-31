package edu.u.nus.readmore;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            NavigationView logoutView = findViewById(R.id.nav_view);
            logoutView.setVisibility(View.VISIBLE);
            logoutView.setNavigationItemSelectedListener(this);
            NavigationView loginView = findViewById(R.id.nav_view_login);
            loginView.setVisibility(View.GONE);
        } else {
            NavigationView loginView = findViewById(R.id.nav_view_login);
            loginView.setVisibility(View.VISIBLE);
            loginView.setNavigationItemSelectedListener(this);
            NavigationView logoutView = findViewById(R.id.nav_view);
            logoutView.setVisibility(View.GONE);
        }

//      A method for the hamburger icon together with animations
//      Can be created separately but would not have rotating animations
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
    }

    /**
     * Starts a new Intent, with extra information given based on key and value passed to
     * Function.
     *
     * @param key   Used to identify intent
     * @param value Used to identify intent
     */
    private void startIntermediateActivity(String key, String value) {
        Intent startIntent = new Intent(getApplicationContext(), IntermediateActivity.class);
        startIntent.putExtra(key, value);
        startActivity(startIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_login:
                String loginKey = getString(R.string.login_key);
                String loginValue = "login";
                startIntermediateActivity(loginKey, loginValue);
                break;
            case R.id.nav_settings:
                String settingsKey = getString(R.string.settings_key);
                String settingsValue = "settings";
                startIntermediateActivity(settingsKey, settingsValue);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //closing activity
            new AlertDialog.Builder(this)
                    .setTitle("Quit")
                    .setMessage("Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

}
