package edu.u.nus.readmore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private EditText userID, userPassword;
    private TextView forgotPW;
    private ImageButton passwordVisibilityBtn;
    private Button signUpBtn, loginBtn;
    private SignInButton googleBtn;

    // flag for changing passwordVisibility
    private boolean passwordFlag = false;

    // RC_SIGN_IN is the request code you will assign for starting the new activity.
    // this can be any number. When the user is done with the subsequent activity and returns,
    // the system calls your activity's onActivityResult() method.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleLoginActivity";
    private FirebaseAuth mAuth;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Login");

        // Initialize buttons and TextViews
        userID = getActivity().findViewById(R.id.user_id);
        userPassword = getActivity().findViewById(R.id.user_pwd);
        forgotPW = getActivity().findViewById(R.id.tv_forgot_pwd);
        passwordVisibilityBtn = getActivity().findViewById(R.id.toggle_password_btn);
        signUpBtn = getActivity().findViewById(R.id.signup_btn);
        loginBtn = getActivity().findViewById(R.id.login_btn);
        googleBtn = getActivity().findViewById(R.id.google_btn);
        mAuth = FirebaseAuth.getInstance();

        //setting login button activity
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

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
                toRegFt.commit();
            }
        });

        // setup of passwordVisibility button
        passwordVisibilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordFlag) {
                    // set to hide password
                    passwordFlag = false;
                    passwordVisibilityBtn.setImageResource(R.drawable.ic_hide_password_24dp);
                    // set input type to textPassword
                    userPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    // set to show password
                    passwordFlag = true;
                    passwordVisibilityBtn.setImageResource(R.drawable.ic_show_password_24dp);
                    // set input type to textVisiblePassword
                    userPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                // set cursor at end of EditText
                userID.setSelection(userID.getText().length());
                userPassword.setSelection(userPassword.getText().length());
            }
        });

        //setting google-link button
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.google_btn:
                        signIn();
                        break;
                    // ...
                }
            }
        });
    }

    // Method for logging in Users
    private boolean verifyLoginInput(String ID, String Password) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Boolean checker = true;

        if (TextUtils.isEmpty(ID) || !ID.matches(emailRegex)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Please enter a valid Email",
                    Toast.LENGTH_SHORT)
                    .show();
            checker = false;
        }
        if (TextUtils.isEmpty(Password)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Please enter a Password",
                    Toast.LENGTH_SHORT)
                    .show();
            checker = false;
        }
        return checker;
    }

    private void loginUser() {
        String ID, Password;
        ID = userID.getText().toString();
        Password = userPassword.getText().toString();

        if (verifyLoginInput(ID, Password)) {
            mAuth.signInWithEmailAndPassword(ID, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Login Successful!",
                                        Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Login unsuccessful, please try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // Google Account Sign-in
    private void signIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            // load default UI
            // TEMP SET TO LOGIN PAGE
            Intent loadLoginPage = new Intent(getActivity().getApplicationContext(),
                    IntermediateActivity.class);
            loadLoginPage.putExtra(getString(R.string.login_key), "login");
            startActivity(loadLoginPage);
        } else {
            // load google sign-in UI
            // TEMP SET TO LOGIN PAGE
            Intent loadLoginPage = new Intent(getActivity().getApplicationContext(),
                    IntermediateActivity.class);
            loadLoginPage.putExtra(getString(R.string.login_key), "login");
            startActivity(loadLoginPage);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);

    }
}
