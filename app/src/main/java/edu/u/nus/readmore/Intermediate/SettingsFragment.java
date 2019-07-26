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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import edu.u.nus.readmore.MainActivity;
import edu.u.nus.readmore.R;

public class SettingsFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Boolean isNightTheme = restorePrefData();
        // Setting theme upon launching
        if (isNightTheme) {
            getActivity().setTheme(R.style.AppThemeDark);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        super.onViewCreated(view, savedInstanceState);
        Switch themeBtn = getActivity().findViewById(R.id.theme_btn);
        if (isNightTheme) {
            themeBtn.setChecked(true);
        } else {
            themeBtn.setChecked(false);
        }
        themeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveThemeData(true);
//                    refreshFragment();
                } else {
                    saveThemeData(false);
//                    refreshFragment();
                }
            }
        });
    }

    private void refreshFragment() {
        Fragment currentFrag = getActivity().getSupportFragmentManager().findFragmentById(R.id.intermediate_frame_layout);
        if (currentFrag instanceof SettingsFragment) {
            FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
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
}
