package edu.u.nus.readmore.Intermediate.Login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import edu.u.nus.readmore.R;
import edu.u.nus.readmore.User;

public class RegisterFragment extends Fragment {
    private TextView alreadyMember;
    private Button createAccBtn;
    private TextInputLayout textInputEmail, textInputPassword, textInputConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EmailPassword";
    private ProgressBar mProgressBar;
    private RelativeLayout registerRelativeLayout;

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
        textInputEmail = getActivity().findViewById(R.id.text_input_email);
        textInputPassword = getActivity().findViewById(R.id.text_input_password);
        textInputConfirmPassword = getActivity().findViewById(R.id.text_input_confirm_password);
        mProgressBar = getActivity().findViewById(R.id.register_progress);
        registerRelativeLayout = getActivity().findViewById(R.id.register_account_relative_layout);

        // create account and redirecting back to login fragment
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                // disable touch until progress finish
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                hideKeyBoardFrom(getActivity().getApplicationContext(), getView().getRootView());
                registerNewUser();
            }
        });

        alreadyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoardFrom(getActivity().getApplicationContext(), getView().getRootView());
                backToLogin();
            }
        });

        // Hide keyboard when user clicks on any part of relative layout
        registerRelativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Check if view is being clicked
                if (hasFocus) {
                    hideSoftKeyBoard(getActivity().getApplicationContext(), v);
                }
            }
        });
    }

    private void hideSoftKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean validateForm() {
        boolean validRegistration = true;
        // check for valid email
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String givenEmail = textInputEmail.getEditText().getText().toString();
        String givenPassword = textInputPassword.getEditText().getText().toString();
        String givenConfirmPassword = textInputConfirmPassword.getEditText().getText().toString();
        // check for valid email given
        if (TextUtils.isEmpty(givenEmail) || !givenEmail.matches(emailRegex)) {
            textInputEmail.setError("Please enter a valid Email");
            validRegistration = false;
        } else {
            textInputEmail.setError(null);
        }

        if (!givenPassword.equals(givenConfirmPassword)) {
            textInputConfirmPassword.setError("Password given does not match");
            validRegistration = false;
        } else {
            textInputConfirmPassword.setError(null);
        }

        // check password length is at least 8
        if (givenPassword.isEmpty()) {
            textInputPassword.setError("Password can't be empty!");
            validRegistration = false;
        } else if (givenPassword.length() < 8) {
            textInputPassword.setError("Password needs to be at least 8 characters");
            validRegistration = false;
        } else {
            textInputPassword.setError(null);
        }
        return validRegistration;
    }

    // creating new user account
    private void registerNewUser() {
        if (validateForm()) {
            // Create a new user account
            final String givenEmail = textInputEmail.getEditText().getText().toString();
            String givenPassword = textInputPassword.getEditText().getText().toString();

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
                                dismissProgressBar();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT)
                                        .show();
                                updateUI(null);
                                dismissProgressBar();
                            }
                        }
                    });
        } else {
            // failed input
            dismissProgressBar();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Go back to MainActivity (Homepage)
            getActivity().finish();
        } else {
            textInputEmail.getEditText().setText(null);
            textInputPassword.getEditText().setText(null);
            textInputConfirmPassword.getEditText().setText(null);
        }
    }

    // direct back to login page, previous fragment
    private void backToLogin() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    // hide soft keyboard
    private void hideKeyBoardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void dismissProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
}