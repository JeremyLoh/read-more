package edu.u.nus.readmore.Intermediate.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Objects;

import edu.u.nus.readmore.R;
import edu.u.nus.readmore.User;
import edu.u.nus.readmore.Util.Validation.AccountValidator;

public class LoginFragment extends Fragment {
    private TextInputLayout textInputEmail, textInputPassword;
    private TextView forgotPW;
    private ImageButton passwordVisibilityBtn;
    private Button signUpBtn, loginBtn;
    private SignInButton googleBtn;
    private ProgressBar mProgressBar;
    private RelativeLayout loginRelativeLayout;

    private boolean isPasswordVisible = false;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    // RC_SIGN_IN is the request code you will assign for starting the new activity.
    // this can be any number. When the user is done with the subsequent activity and returns,
    // the system calls your activity's onActivityResult() method.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleLoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Objects.requireNonNull(getActivity()).setTitle("Login");

        initializeFirebaseAuth();
        initializeScreenButtons();
        setupScreenViews();
        setupGoogleSignIn();
        hideKeyboardWhenFocusChanges();
        setupButtons();
    }

    private void initializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeScreenButtons() {
        textInputEmail = Objects.requireNonNull(getActivity()).findViewById(R.id.text_input_layout_email);
        textInputPassword = getActivity().findViewById(R.id.text_input_layout_password);
        forgotPW = getActivity().findViewById(R.id.tv_forgot_pwd);
        passwordVisibilityBtn = getActivity().findViewById(R.id.toggle_password_btn);
        signUpBtn = getActivity().findViewById(R.id.signup_btn);
        loginBtn = getActivity().findViewById(R.id.login_btn);
        googleBtn = getActivity().findViewById(R.id.google_btn);
    }

    private void setupScreenViews() {
        mProgressBar = Objects.requireNonNull(getActivity()).findViewById(R.id.login_progress);
        loginRelativeLayout = getActivity().findViewById(R.id.login_relative_layout);
    }

    private void setupGoogleSignIn() {
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    private void hideKeyboardWhenFocusChanges() {
        // Hide keyboard when user clicks on any part of relative layout
        loginRelativeLayout.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                hideSoftKeyBoard(requireContext(), view);
            }
        });
    }

    private void setupButtons() {
        final FragmentTransaction toRegFt = Objects.requireNonNull(getActivity())
                .getSupportFragmentManager()
                .beginTransaction();
        setupSignUpButton(toRegFt);
        setupForgotPasswordButton(toRegFt);
        setupLoginButton();
        setupPasswordVisibilityButton();
        setupGoogleLinkButton();
    }

    private void setupSignUpButton(FragmentTransaction toRegFt) {
        //setting sign-up button to RegisterFragment
        signUpBtn.setOnClickListener(view -> {
            hideSoftKeyBoard(requireContext(), Objects.requireNonNull(getView()).getRootView());
            toRegFt.replace(R.id.intermediate_frame_layout, new RegisterFragment());
            toRegFt.addToBackStack("Register");
            toRegFt.commit();
        });
    }

    private void setupForgotPasswordButton(FragmentTransaction toRegFt) {
        //setting forgotPW button to ResetPasswordFragment
        forgotPW.setOnClickListener(view -> {
            hideSoftKeyBoard(requireContext(), Objects.requireNonNull(getView()).getRootView());
            toRegFt.replace(R.id.intermediate_frame_layout, new ResetPasswordFragment());
            toRegFt.addToBackStack("ForgotPW");
            toRegFt.commit();
        });
    }

    private void setupLoginButton() {
        loginBtn.setOnClickListener(view -> {
            // Disable clicking upon login prevent crashing of app, will enable upon
            // log in successfully or failure
            mProgressBar.setVisibility(View.VISIBLE);
            Objects.requireNonNull(getActivity())
                    .getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            // hide soft keyboard for better UI
            hideSoftKeyBoard(requireContext(), Objects.requireNonNull(getView()).getRootView());
            loginUser();
        });
    }

    private void setupPasswordVisibilityButton() {
        passwordVisibilityBtn.setOnClickListener(view -> {
            if (isPasswordVisible) {
                setPasswordField(R.drawable.ic_hide_password_24dp, InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                setPasswordField(R.drawable.ic_show_password_24dp, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            isPasswordVisible = !isPasswordVisible;
            setCursorToTextEnd();
        });
    }

    private void setPasswordField(int buttonImageResource, int passwordTextVariationType) {
        passwordVisibilityBtn.setImageResource(buttonImageResource);
        Objects.requireNonNull(textInputPassword.getEditText())
                .setInputType(InputType.TYPE_CLASS_TEXT | passwordTextVariationType);
    }

    private void setCursorToTextEnd() {
        Objects.requireNonNull(textInputEmail.getEditText())
                .setSelection(textInputEmail.getEditText().getText().length());
        Objects.requireNonNull(textInputPassword.getEditText())
                .setSelection(textInputPassword.getEditText().getText().length());
    }

    private void setupGoogleLinkButton() {
        googleBtn.setOnClickListener(view -> {
            hideSoftKeyBoard(requireContext(), Objects.requireNonNull(getView()).getRootView());
            handleGoogleSignIn();
        });
    }

    private void hideSoftKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void loginUser() {
        String id = Objects.requireNonNull(textInputEmail.getEditText())
                .getText().toString();
        String password = Objects.requireNonNull(textInputPassword.getEditText())
                .getText().toString();
        if (isValidLoginInput(id, password)) {
            signInUsingEmailAndPassword(id, password);
        } else {
            displayLoginError(id, password);
            dismissProgressBar();
        }
    }

    private boolean isValidLoginInput(String id, String password) {
        return isValidId(id) && isValidPassword(password);
    }

    private void signInUsingEmailAndPassword(String id, String password) {
        mAuth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener(task -> {
                    dismissProgressBar();
                    if (task.isSuccessful()) {
                        displayShortToastMessage("Login Successful!");
                        finishActivity();
                    } else {
                        displayShortToastMessage("Login unsuccessful, please try again");
                    }
                });
    }

    private void finishActivity() {
        Objects.requireNonNull(getActivity()).finish();
    }

    private boolean isValidId(String id) {
        return !TextUtils.isEmpty(id) && AccountValidator.isValidEmail(id);
    }

    private boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password);
    }

    private void displayLoginError(String id, String password) {
        displayIdError(id);
        displayPasswordError(password);
    }

    private void displayIdError(String id) {
        String error = isValidId(id) ? null : "Please enter a valid Email";
        textInputEmail.setError(error);
    }

    private void displayPasswordError(String password) {
        String error = isValidPassword(password) ? null : "Please enter a Password";
        textInputPassword.setError(error);
    }

    private void displayShortToastMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                .show();
    }

    private void dismissProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        Objects.requireNonNull(getActivity())
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void handleGoogleSignIn() {
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
                authenticateGoogleSignInWithFirebase(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void authenticateGoogleSignInWithFirebase(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
        // Check user last logged in before: if first time create a User document in database
        // else success login redirect back to homepage by doing finish()
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), handleGoogleSignInWithFirebase());
    }

    @NonNull
    private OnCompleteListener<AuthResult> handleGoogleSignInWithFirebase() {
        return task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                handleGoogleUserLogin(task);
            } else {
                // Sign-in fails, display a message to the user.
                Log.d(TAG, "signInWithCredential:failure", task.getException());
                updateUI(null);
            }
        };
    }

    private void handleGoogleUserLogin(Task<AuthResult> task) {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isNewUser = Objects.requireNonNull(task.getResult().getAdditionalUserInfo())
                .isNewUser();
        if (isNewUser) {
            assert user != null;
            createNewFirebaseUser(user);
        }
        updateUI(user);
    }

    private void createNewFirebaseUser(FirebaseUser user) {
        assert user != null;
        String ID = user.getUid();
        User newUser = new User(ID);
        db.collection("Users")
                .document(ID)
                .set(newUser, SetOptions.merge());
    }

    private void updateUI(FirebaseUser account) {
        if (account == null) {
            displayShortToastMessage("Authentication failed, please try again");
        } else {
            displayShortToastMessage("Login Successful!");
            // Finishes intermediate, redirect to MainActivity
            finishActivity();
        }
    }
}
