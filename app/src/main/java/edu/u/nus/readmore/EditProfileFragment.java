package edu.u.nus.readmore;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EditProfileFragment extends Fragment {
    private Button deleteAccountBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Initialize buttons
        deleteAccountBtn = getActivity().findViewById(R.id.delete_account_btn);

        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText passwordField = new EditText(getActivity());
                passwordField.setHint("Enter your password");
                // Convert dp to pixel for padding of password EditText
                int sizeInDp = 24;
                float scale = getResources().getDisplayMetrics().density;
                //  0.5 is used to get the closest integer when casting
                int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
                // padding set according to left, top, right, bottom
                passwordField.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
                // Set EditText to password type
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Account")
                        .setMessage("Once you delete your account, there is no going back. Please be certain.")
                        .setCancelable(true)
                        .setView(passwordField);
                confirmDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get password
                        final String password = passwordField.getText().toString();
                        final FirebaseUser user = mAuth.getCurrentUser();
                        final String userID = user.getUid();
                        final String email = user.getEmail();
                        // Get sign in method,
                        // email & password is "password" for getProviderID
                        List<? extends UserInfo> userInfo = user.getProviderData();
                        String loginType = userInfo.get(1).getProviderId();

                        if (loginType.equals("password")) {
                            // email & password account type
                            // Get auth credentials from the user for
                            // re-authentication
                            AuthCredential credential = EmailAuthProvider.
                                    getCredential(email, password);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            user.delete();
                                            // Delete user data in firestore
                                            db.collection("Users")
                                                    .document(userID)
                                                    .delete();
                                            // Redirect to MainActivity
                                            getActivity().finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            passwordField.setText("");
                                            Toast.makeText(getActivity().getApplicationContext(),
                                                    "Authentication failed. " +
                                                            "Please retype your " +
                                                            "password.",
                                                    Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                        }
                    }
                })
                        .setNegativeButton("Exit", null)
                        .show();
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }
}
