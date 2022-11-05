package edu.u.nus.readmore.Intermediate.Login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Objects;

import edu.u.nus.readmore.R;
import edu.u.nus.readmore.User;
import edu.u.nus.readmore.Util.Validation.AccountValidator;

public class RegisterFragment extends Fragment {
    private TextView alreadyMember;
    private Button createAccountBtn;
    private TextInputLayout textInputEmail, textInputPassword, textInputConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EmailPassword";
    private ProgressBar mProgressBar;
    private RelativeLayout registerRelativeLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivityTitle();
        instantiateViewElements();
        setupCreateAccountButton();
        setupAlreadyMemberText();
        setupHideKeyboardWhenFocusChanges();
    }

    private void setActivityTitle() {
        Objects.requireNonNull(getActivity()).setTitle("Register");
    }

    private void instantiateViewElements() {
        alreadyMember = Objects.requireNonNull(getActivity()).findViewById(R.id.already_member);
        createAccountBtn = getActivity().findViewById(R.id.register_btn);
        textInputEmail = getActivity().findViewById(R.id.text_input_email);
        textInputPassword = getActivity().findViewById(R.id.text_input_password);
        textInputConfirmPassword = getActivity().findViewById(R.id.text_input_confirm_password);
        mProgressBar = getActivity().findViewById(R.id.register_progress);
        registerRelativeLayout = getActivity().findViewById(R.id.register_account_relative_layout);
    }

    private void setupCreateAccountButton() {
        // create account and redirect back to login fragment
        createAccountBtn.setOnClickListener(view -> {
            showProgressBar();
            // disable touch until progress finish
            disableTouchInput();
            hideKeyBoardFrom(requireContext(), Objects.requireNonNull(getView()).getRootView());
            registerNewUser();
        });
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void disableTouchInput() {
        Objects.requireNonNull(getActivity())
                .getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void setupAlreadyMemberText() {
        alreadyMember.setOnClickListener(view -> {
            hideKeyBoardFrom(requireContext(), Objects.requireNonNull(getView()).getRootView());
            backToLogin();
        });
    }

    private void setupHideKeyboardWhenFocusChanges() {
        // Hide keyboard when user clicks on any part of relative layout
        registerRelativeLayout.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                hideSoftKeyBoard(requireContext(), view);
            }
        });
    }

    private void hideSoftKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void registerNewUser() {
        String email = getTextInput(textInputEmail);
        String password = getTextInput(textInputPassword);
        String confirmPassword = getTextInput(textInputConfirmPassword);
        if (isValidNewUser(email, password, confirmPassword)) {
            createNewUser(email, password);
        } else {
            displayErrorForInvalidNewUser(email, password, confirmPassword);
            dismissProgressBar();
        }
    }

    @NonNull
    private String getTextInput(TextInputLayout textInputConfirmPassword) {
        return Objects.requireNonNull(textInputConfirmPassword.getEditText())
                .getText()
                .toString();
    }

    private void createNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
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
                            displayShortToastMessage("Welcome!");
                            // add user to db
                            String ID = user.getUid();
                            User newUser = new User(ID);
                            db.collection("Users")
                                    .document(ID)
                                    .set(newUser, SetOptions.merge());
                            clearRegisterForm();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            displayShortToastMessage("Authentication failed.");
                            navigateToHomepage();
                        }
                        dismissProgressBar();
                    }
                });
    }

    private void displayErrorForInvalidNewUser(String email, String password, String confirmPassword) {
        String emailError = getErrorTextForInvalidEmail(email);
        String passwordError = getErrorTextForInvalidPassword(password);
        String confirmPasswordError = getErrorTextForInvalidConfirmPassword(password, confirmPassword);
        textInputEmail.setError(emailError);
        textInputPassword.setError(passwordError);
        textInputConfirmPassword.setError(confirmPasswordError);
    }

    private String getErrorTextForInvalidEmail(String email) {
        if (TextUtils.isEmpty(email) || !AccountValidator.isValidEmail(email)) {
            return "Please enter a valid Email";
        }
        return null;
    }

    private String getErrorTextForInvalidPassword(String password) {
        if (password.isEmpty()) {
            return "Password can't be empty!";
        }
        if (password.length() < 8) {
            return "Password needs to be at least 8 characters";
        }
        return null;
    }

    private String getErrorTextForInvalidConfirmPassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return "Password given does not match";
        }
        return null;
    }

    private boolean isValidNewUser(String email, String password, String confirmPassword) {
        return AccountValidator.isValidEmail(email) && isValidPassword(password, confirmPassword);
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword)
                && password.length() >= 8;
    }

    private void displayShortToastMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                .show();
    }

    private void clearRegisterForm() {
        Objects.requireNonNull(textInputEmail.getEditText()).setText(null);
        Objects.requireNonNull(textInputPassword.getEditText()).setText(null);
        Objects.requireNonNull(textInputConfirmPassword.getEditText()).setText(null);
    }

    private void navigateToHomepage() {
        Objects.requireNonNull(getActivity()).finish();
    }

    // direct back to login page, previous fragment
    private void backToLogin() {
        Objects.requireNonNull(getActivity())
                .getSupportFragmentManager()
                .popBackStack();
    }

    // hide soft keyboard
    private void hideKeyBoardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void dismissProgressBar() {
        hideProgressBar();
        Objects.requireNonNull(getActivity())
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }
}