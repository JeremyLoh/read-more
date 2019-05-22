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

public class LoginFragment extends Fragment {
    TextView forgotPW;
    Button signUpBtn,loginBtn, googleBtn;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Login");
        forgotPW = getActivity().findViewById(R.id.tv_forgot_pwd);
        signUpBtn = getActivity().findViewById(R.id.signup_btn);
        loginBtn = getActivity().findViewById(R.id.login_btn);
        googleBtn = getActivity().findViewById(R.id.google_btn);

        //setting login button activity
//        loginBtn.setOnClickListener(new View.OnClickListener() {set
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        //setting sign-up button to RegisterFragment
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction toRegFt = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();
                toRegFt.replace(R.id.intermediate_frame_layout, new RegisterFragment());
                toRegFt.commit();
            }
        });

        //setting forgotPW button to ResetPasswordFragment
        forgotPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction toRegFt = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();
                toRegFt.replace(R.id.intermediate_frame_layout, new ResetPasswordFragment());
                toRegFt.commit();            }
        });

        //setting google-link button

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

}
