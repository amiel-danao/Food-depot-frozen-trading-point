package com.thesis.deliverytracking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.deliverytracking.models.UserInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    TextInputLayout Email, Username, Role, Password, confirmPasswordInput, FullName;
    String selectedRole = "Delivery";
    Button btnRegister;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String UserID, UserEmail, UserUsername, UserRole, UserPassword, TxtFullName;
    UserInfo userToEdit;

    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_register, container, false);
        AutoCompleteTextView eT = (AutoCompleteTextView) view.findViewById(R.id.selectionrole);
        String[] rolesArray = new String[]{"Delivery", "Admin"};
        ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, rolesArray);
        eT.setAdapter(aAdapter);

        eT.setOnItemClickListener((parent, view1, position, id) -> selectedRole = (String) parent.getItemAtPosition(position));

        if(getArguments() != null) {
            userToEdit = getArguments().getParcelable("id");
        }

        Email = view.findViewById(R.id.txtEmail);
        Username = view.findViewById(R.id.txtUserName);
        Role = view.findViewById(R.id.txtrole);
        Password = view.findViewById(R.id.txtpassword);
        confirmPasswordInput = view.findViewById(R.id.txtConfirmPassword);
        FullName = view.findViewById(R.id.txtFullName);
        btnRegister = view.findViewById(R.id.btn_register);

        if(userToEdit != null){
            Email.getEditText().setText(userToEdit.email);
            Email.setEnabled(false);
            Username.getEditText().setText(userToEdit.username);
            Role.getEditText().setText(userToEdit.role);
            Password.setVisibility(View.GONE);
            FullName.getEditText().setText(userToEdit.fullName);
            confirmPasswordInput.setVisibility(View.GONE);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(addUserClickListener);

        return view;
    }

    private View.OnClickListener addUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isNetworkAvailable(getContext())) {
                showToast(R.drawable.ic_network_check, "Please verify your network.");
            } else {
                if (!emailValidation() | !usernameValidation() | !fullNameValidation() | !passwordValidation()) {
                    return;
                } else {
                    UserEmail = Email.getEditText().getText().toString();
                    UserUsername = Username.getEditText().getText().toString();
                    UserRole = selectedRole;
                    UserPassword = Password.getEditText().getText().toString();
                    TxtFullName = FullName.getEditText().getText().toString();
                    String confirmPassword = confirmPasswordInput.getEditText().getText().toString();

                    if (UserPassword.isEmpty() || confirmPassword.isEmpty()) {
//                            showToast(R.drawable.ic_lock,"blank password is not allowed!");
                        Toast.makeText(getContext(), "Password does not match!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!UserPassword.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "Password does not match!", Toast.LENGTH_LONG).show();
//                            showToast(R.drawable.ic_lock,"Password does not match!");
                        return;
                    }

                    firebaseAuth.createUserWithEmailAndPassword(UserEmail, UserPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseFirestore = FirebaseFirestore.getInstance();
                            UserID = firebaseAuth.getCurrentUser().getUid();
                            Map<String,Object> UserData = new HashMap<>();
                            UserData.put("email", UserEmail);
                            UserData.put("username", UserUsername);
                            UserData.put("role", "Delivery");
                            UserData.put("fullName", TxtFullName);
                            firebaseFirestore.collection("users")
                                    .document(UserID)
                                    .set(UserData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful()){
                                                Log.d("Storing",task.getException().getMessage());
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Storing",e.getMessage());
                                        }
                                    });

                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showToast(R.drawable.ic_info, "User is registered successfully.\nPlease check your inbox for verification email.");
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Registration", task.getException().getMessage());
                                        }
                                    });
                        }
                    }).addOnFailureListener(e -> Email.setError(e.getMessage()));
                }
            }
        }
    };

    private boolean fullNameValidation() {
        String fullName = FullName.getEditText().getText().toString();
        if (fullName.trim().isEmpty()) {
            FullName.setError("Full name is required!");
            return false;
        }

        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("Register a Driver");
    }

    boolean emailValidation() {
        String email = Email.getEditText().getText().toString();
        if (email.isEmpty()) {
            Email.setError("Please enter a Email");
            return false;
        } else {
            String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                Email.setError(null);
                return true;
            } else {
                Email.setError("Please enter a valid Email");
                return false;
            }
        }
    }

    boolean usernameValidation() {
        String username = Username.getEditText().getText().toString();
        if (username.isEmpty()) {
            Username.setError("Please enter a Username");
            return false;
        } else {
            Username.setError(null);
            return true;
        }
    }

    boolean roleValidation() {
        if (selectedRole.isEmpty()) {
            Role.setError("Please select a Role");
            return false;
        } else {
            Role.setError(null);
            return true;
        }
    }

    boolean passwordValidation() {
        String pass = Password.getEditText().getText().toString();
        if (pass.isEmpty()) {
            Password.setError("Please enter a Password");
            return false;
        } else if (pass.length() < 6 || pass.length() > 12) {
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
}