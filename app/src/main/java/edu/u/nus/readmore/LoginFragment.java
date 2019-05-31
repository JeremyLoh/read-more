package edu.u.nus.readmore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


public class LoginFragment extends Fragment {
    private EditText userID, userPassword;
    private TextView forgotPW;
    private ImageButton passwordVisibilityBtn;
    private Button signUpBtn,loginBtn;
    private SignInButton googleBtn;

    // flag for changing passwordVisibility, starts with false
    private boolean passwordFlag = false;

    // RC_SIGN_IN is the request code you will assign for starting the new activity.
    // this can be any number. When the user is done with the subsequent activity and returns,
    // the system calls your activity's onActivityResult() method.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleLoginActivity";


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Login");
        userID = getActivity().findViewById(R.id.user_id);
        userPassword = getActivity().findViewById(R.id.user_pwd);
        forgotPW = getActivity().findViewById(R.id.tv_forgot_pwd);
        passwordVisibilityBtn = getActivity().findViewById(R.id.toggle_password_btn);
        signUpBtn = getActivity().findViewById(R.id.signup_btn);
        loginBtn = getActivity().findViewById(R.id.login_btn);
        googleBtn = getActivity().findViewById(R.id.google_btn);

        //setting login button activity


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
