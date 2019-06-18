package edu.u.nus.readmore;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class IntermediateActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private Map<String, Boolean> uFilterHashMap;

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
                } else {
                    // user is signed out
                }
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
            Bundle bundle = new Bundle();
            uFilterHashMap =
                    (HashMap<String, Boolean>) getIntent().getSerializableExtra("Filter");
            bundle.putSerializable("Filter", (HashMap<String, Boolean>) uFilterHashMap);
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.setArguments(bundle);
            getSupportActionBar().setTitle(getString(R.string.filter_key));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.intermediate_frame_layout, filterFragment)
                    .commit();
        } else if (currentIntent.hasExtra(getString(R.string.edit_profile_key)) && savedInstanceState == null) {
            // Set name of Action Bar
            getSupportActionBar().setTitle("Edit Profile");
            // Load EditProfileFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.intermediate_frame_layout, new EditProfileFragment())
                    .commit();
        }

        // Restore previous savedInstanceState
        if (savedInstanceState != null && mFirebaseAuth.getCurrentUser() != null) {
            uFilterHashMap = (HashMap<String, Boolean>) savedInstanceState.getSerializable("Filter");
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
        Log.d("pause", "called");
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (isFinishing() && firebaseUser != null && uFilterHashMap != null) {
            MainActivity.getActivityInstance().updateCurrentUserFilter(uFilterHashMap);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void updateUserFilterAtInter(Map<String, Boolean> updatedUserFilter) {
        uFilterHashMap = updatedUserFilter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFirebaseAuth.getCurrentUser() != null && uFilterHashMap != null) {
            outState.putSerializable("Filter", (HashMap<String, Boolean>) uFilterHashMap);
        }
    }
}
