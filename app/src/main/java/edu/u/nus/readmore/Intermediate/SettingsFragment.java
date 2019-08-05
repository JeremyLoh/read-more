package edu.u.nus.readmore.Intermediate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import edu.u.nus.readmore.MainActivity;
import edu.u.nus.readmore.R;

public class SettingsFragment extends Fragment {
    static int switchPressedAmount = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Boolean isNightTheme = restorePrefData();
        // Setting theme upon launching
        if (isNightTheme) {
            getActivity().setTheme(R.style.AppTheme);
        } else {
            getActivity().setTheme(R.style.AppThemeDark);
        }

        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Settings");

        if (savedInstanceState != null) {
            switchPressedAmount = savedInstanceState.getInt("switchPressedAmount");
        }

        Switch themeBtn = getActivity().findViewById(R.id.theme_btn);
        if (isNightTheme) {
            themeBtn.setChecked(true);
        }
        themeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchPressedAmount++;
                    saveThemeData(isChecked);
                    refreshFragment();
                } else {
                    switchPressedAmount++;
                    saveThemeData(isChecked);
                    refreshFragment();
                }
            }
        });
    }

    private void refreshFragment() {
        Fragment currentFrag = getActivity().getSupportFragmentManager().findFragmentById(R.id.intermediate_frame_layout);
        if (currentFrag instanceof SettingsFragment) {
            FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.detach(currentFrag);
            fragmentTransaction.attach(currentFrag);
            fragmentTransaction.commit();

        }
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("myTheme", Context.MODE_PRIVATE);
        Boolean isNightTheme = pref.getBoolean("isNightTheme", false);
        return isNightTheme;
    }

    private void saveThemeData(boolean onOff) {
            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("myTheme", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isNightTheme", onOff);
            editor.commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onDestroy() {
        switchPressedAmount = 0;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("switchPressedAmount", switchPressedAmount);
    }
}
