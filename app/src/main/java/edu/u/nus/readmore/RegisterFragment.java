package edu.u.nus.readmore;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterFragment extends Fragment {
    TextView alreadyMember;
    Button createAccBtn;
    EditText email, password, confirmPassword;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Register");

        // instantiate buttons and textview
        alreadyMember = getActivity().findViewById(R.id.already_member);
        createAccBtn = getActivity().findViewById(R.id.register_btn);
        email = getActivity().findViewById(R.id.register_id);
        password = getActivity().findViewById(R.id.register_pwd);
        confirmPassword = getActivity().findViewById(R.id.register_pwd_check);

        // create account and redirecting back to login fragment
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
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
        boolean validRegistration = true;
        // check for valid email
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String givenEmail = email.getText().toString();
        String givenPassword = password.getText().toString();
        String givenConfirmPassword = confirmPassword.getText().toString();
        // check for valid email given
        if (TextUtils.isEmpty(givenEmail) || !givenEmail.matches(emailRegex)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Please enter a valid Email",
                    Toast.LENGTH_SHORT)
                    .show();
            validRegistration = false;
        }

        if (TextUtils.isEmpty(givenPassword) || TextUtils.isEmpty(givenConfirmPassword)
            || !givenPassword.equals(givenConfirmPassword)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Password given does not match",
                    Toast.LENGTH_SHORT)
                    .show();
            validRegistration = false;
        }

        // check password length is at least 8
        if (givenPassword.length() < 8) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Password needs to be at least 8 characters",
                    Toast.LENGTH_SHORT)
                    .show();
            validRegistration = false;
        }

        if (validRegistration) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Welcome!",
                    Toast.LENGTH_SHORT)
                    .show();
            backToLogin();
        }
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

