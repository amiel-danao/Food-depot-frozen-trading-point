package com.thesis.deliverytracking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    TextInputLayout Email, Password;
    Button btnLogin;
    FirebaseAuth firebaseAuth;
    String UserEmail, UserPassword;
    private final View.OnClickListener forgotPasswordClickListener = view -> {
        Context context = view.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Input your email");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString().trim();
                // Call the resetPassword method with the entered email
                resetPassword(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    };

    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);

        Email = view.findViewById(R.id.txtemaillog);
        Password = view.findViewById(R.id.txtpasswordlog);
        btnLogin = view.findViewById(R.id.btnlogin);
        TextView forgotPassword = view.findViewById(R.id.forgotPassword);

        firebaseAuth = FirebaseAuth.getInstance();



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable(getContext())){
                    showToast(R.drawable.ic_network_check, "Please verify your network.");
                } else {
                    if (!emailValidation() | !passwordValidation()) {
                        return;
                    } else {
                        UserEmail = Email.getEditText().getText().toString();
                        UserPassword = Password.getEditText().getText().toString();
                        firebaseAuth.signInWithEmailAndPassword(UserEmail, UserPassword)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                startActivity(new Intent(getContext(), ContentActivity.class));
                                                ActivityCompat.finishAffinity(getActivity());
                                            } else {
                                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        showToast(R.drawable.ic_mail, "An email verification was sent. Please check your inbox/spam");
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e.getMessage().equals("The password is invalid or the user does not have a password.")) {
                                    showToast(R.drawable.ic_lock, "The password is incorrect. try again.");
                                } else if (e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                    showToast(R.drawable.ic_warning, "There is no user registered with that email.");
                                }
                            }
                        });
                    }
                }
            }
        });

        forgotPassword.setOnClickListener(forgotPasswordClickListener);

        return view;
    }

    boolean emailValidation(){
        String email = Email.getEditText().getText().toString();
        if(email.isEmpty()){
            Email.setError("Please enter the Email");
            return false;
        } else {
            String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);
            if(matcher.matches()){
                Email.setError(null);
                return true;
            } else {
                Email.setError("Please enter a valid Email");
                return false;
            }
        }
    }
    boolean passwordValidation(){
        String pass = Password.getEditText().getText().toString();
        if(pass.isEmpty()){
            Password.setError("Please enter the Password");
            return false;
        } else if (pass.length()<6 || pass.length()>12) {
            Password.setError("Password must have minimum 6 characters and maximum 12");
            return false;
        } else {
            Password.setError(null);
            return true;
        }
    }


    public void showToast(int icon, String text) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) getView().findViewById(R.id.toast_root));
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.TOP, 0, 0);
        ((ImageView) layout.findViewById(R.id.toast_image)).setImageDrawable(getActivity().getResources().getDrawable(icon));
        ((TextView) layout.findViewById(R.id.toast_text)).setText(text);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetPassword(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Password reset email sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Failed to send reset email", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}