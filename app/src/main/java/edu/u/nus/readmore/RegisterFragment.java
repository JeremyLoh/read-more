package edu.u.nus.readmore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class RegisterFragment extends Fragment {
    private TextView alreadyMember;
    private Button createAccBtn;
    private EditText email, password, confirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EmailPassword";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

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

    private boolean validateForm() {
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

        if (!givenPassword.equals(givenConfirmPassword)) {
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
        return validRegistration;
    }

    // creating new user account
    private void registerNewUser() {
        if (validateForm()) {
            // Create a new user account
            final String givenEmail = email.getText().toString();
            String givenPassword = password.getText().toString();

            mAuth.createUserWithEmailAndPassword(givenEmail, givenPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                // If the new account was created, the user is also signed in,
                                // and the AuthStateListener runs the onAuthStateChanged callback.
                                // In the callback, you can manage the work of sending the
                                // verification email to the user
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Welcome!",
                                        Toast.LENGTH_SHORT)
                                        .show();

                                // add user to db
                                String ID = user.getUid();
                                User newUser = new User(ID);
                                db.collection("Users")
                                        .document(ID)
                                        .set(newUser, SetOptions.merge());

                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT)
                                        .show();
                                updateUI(null);
                            }
                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Go back to MainActivity (Homepage)
            getActivity().finish();
        } else {
            email.setText(null);
            password.setText(null);
            confirmPassword.setText(null);
        }
    }

    // direct back to login page
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
}