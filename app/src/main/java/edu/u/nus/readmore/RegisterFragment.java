package edu.u.nus.readmore;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RegisterFragment extends Fragment {
    TextView alreadyMember;
    Button createAccBtn;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Register");

        // instantiate buttons and textview
        alreadyMember = getActivity().findViewById(R.id.already_member);
        createAccBtn = getActivity().findViewById(R.id.register_btn);

        // create account and redirecting back to login fragment
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
                backToLogin();
            }
        });

        alreadyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLogin();
            }
        });
    }

    // creating new user account
    private void registerNewUser() {


    }

    // directing back to login page
    private void backToLogin() {
        FragmentTransaction toLoginFt = getActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        toLoginFt.replace(R.id.intermediate_frame_layout, new LoginFragment());
        toLoginFt.commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

// try to find ways to make back button go back to login fragment instead of home page
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            FragmentTransaction regFT = getActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction();
//            regFT.replace(R.id.intermediate_frame_layout, new LoginFragment());
//            regFT.commit();
//            return true;
//        }
//        return false;
//    }
}

