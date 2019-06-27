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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class LoginFragment extends Fragment {
    private EditText userID, userPassword;
    private TextView forgotPW;
    private ImageButton passwordVisibilityBtn;
    private Button signUpBtn, loginBtn;
    private SignInButton googleBtn;

    // flag for changing passwordVisibility
    private boolean passwordFlag = false;

    // Google sign in components
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    // RC_SIGN_IN is the request code you will assign for starting the new activity.
    // this can be any number. When the user is done with the subsequent activity and returns,
    // the system calls your activity's onActivityResult() method.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleLoginActivity";

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

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
                signIn();
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
        // Clear default Google Sign in account
        mGoogleSignInClient.signOut();
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
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
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
        // Check user last logged inbefore? if frst time create a User document in database
        // else success login redirect back to homepage by doing finish()
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // check user new or existing
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            // Create a User class and add user info into FireStore db
                            if (isNewUser) {
                                String ID = user.getUid();
                                User newUser = new User(ID);
                                db.collection("Users")
                                        .document(ID)
                                        .set(newUser, SetOptions.merge());
                            }
                            updateUI(user);
                        } else {
                            // Sign-in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser account) {
        if (account == null) {
            Toast
                    .makeText(getActivity(),
                            "Authentication failed, please try again",
                            Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast
                    .makeText(getActivity().getApplicationContext(),
                            "Login Successful!",
                            Toast.LENGTH_SHORT)
                    .show();
            // Finishes intermediate, redirect to MainActivity
            getActivity().finish();
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);

    }
}
