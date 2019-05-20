package edu.u.nus.readmore.Intermediate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.u.nus.readmore.LoginFragment;
import edu.u.nus.readmore.R;

public class IntermediateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        Intent currentIntent = this.getIntent();
        if (currentIntent.hasExtra("login")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.intermediate_fragment_container,
                    new LoginFragment()).commit();
        }
    }
}
