package edu.u.nus.readmore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class IntermediateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        // Action bar is used instead of custom toolbar!
        // Adds back button for default action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Intent currentIntent = this.getIntent();

        if (currentIntent.hasExtra(getString(R.string.login_key))) {
            // Loads LoginFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.intermediate_frame_layout, new LoginFragment())
                    .commit();
        } else if (currentIntent.hasExtra(getString(R.string.settings_key))) {
            // Set name of Action Bar
            getSupportActionBar().setTitle("Settings");
            // Loads SettingsFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.intermediate_frame_layout, new SettingsFragment())
                    .commit();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
