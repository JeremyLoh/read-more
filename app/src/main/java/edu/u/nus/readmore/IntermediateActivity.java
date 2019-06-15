package edu.u.nus.readmore;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntermediateActivity extends AppCompatActivity {
    private Menu optionsMenu;
    private MenuItem logoutItem;
    private boolean isLoggedIn;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;

    // onCreateOptionsMenu is called once
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Initialize logout_menu xml file (android:visible="false" at start)
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        optionsMenu = menu;
        logoutItem = optionsMenu.findItem(R.id.logout_item);
        if (isLoggedIn) {
            logoutItem.setVisible(true);
        } else {
            logoutItem.setVisible(false);
        }
        return true;
    }

    // For MenuItem selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_item:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mFirebaseAuth.signOut();
        Toast
                .makeText(this,
                        "You have successfully signed out",
                        Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        // Action bar is used instead of custom toolbar!
        // Adds back button for default action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Initialise Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        // Checking user status for displaying different menu options
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    isLoggedIn = true;
                } else {
                    // user is signed out
                    isLoggedIn = false;
                }
                // declare that the options menu has changed, so should be recreated.
                // calls onCreateOptionsMenu method when menu needs to be displayed again
                invalidateOptionsMenu();
            }
        };

        Intent currentIntent = this.getIntent();
        if (currentIntent.hasExtra(getString(R.string.login_key)) && savedInstanceState == null) {
            // Loads LoginFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.intermediate_frame_layout, new LoginFragment())
                    .commit();
        } else if (currentIntent.hasExtra(getString(R.string.settings_key)) && savedInstanceState == null) {
            // Set name of Action Bar
            getSupportActionBar().setTitle(R.string.settings_key);
            // Loads SettingsFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.intermediate_frame_layout, new SettingsFragment())
                    .commit();
        } else if (currentIntent.hasExtra(getString(R.string.filter_key)) && savedInstanceState == null) {
            getSupportActionBar().setTitle(getString(R.string.filter_key));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.intermediate_frame_layout, new FilterFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
