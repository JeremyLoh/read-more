package edu.u.nus.readmore.Intermediate.Login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import edu.u.nus.readmore.R;

public class ResetPasswordFragment extends Fragment {
    private Button resetPwBtn;
    private EditText userEmailEditText;
    private ProgressBar mProgressBar;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Reset Password");
        resetPwBtn = getActivity().findViewById(R.id.reset_pw_btn);
        userEmailEditText = getActivity().findViewById(R.id.user_email_text);
        mProgressBar = getActivity().findViewById(R.id.reset_progress);

        resetPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                // disable touch until progress finish
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                hideKeyBoardFrom(getActivity().getApplicationContext(), getView().getRootView());
                // Verify email format
                String email = userEmailEditText.getText().toString();
                if (validEmailFormat(email)) {
                    // Send reset password email
                    mFirebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dismissProgressBar();
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                "Reset link have been sent to your e-mail",
                                                Toast.LENGTH_LONG)
                                                .show();
                                        // Go to previous fragment
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    } else {
                                        dismissProgressBar();
                                        makeToastMessage("Unable to send reset password email",
                                                Toast.LENGTH_LONG, 0, -80);
                                    }
                                }
                            });
                } else {
                    // failed reset, added else to make it look better
                    dismissProgressBar();
                }
            }
        });
    }

    private boolean validEmailFormat(String email) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if (TextUtils.isEmpty(email) || !email.matches(emailRegex)) {
            makeToastMessage("Please enter a valid Email",
                    Toast.LENGTH_SHORT, 0, -80);
            return false;
        }
        return true;
    }

    private void makeToastMessage(String message, int toastLength, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                message,
                toastLength);
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
        toast.show();
    }

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
        return inflater.inflate(R.layout.fragment_resetpassword, container, false);
    }
}
