package com.thesis.deliverytracking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.deliverytracking.models.Vehicle;

import java.util.HashMap;
import java.util.Map;

public class AddVehicleFragment extends Fragment {

    TextInputLayout plateNumberInput, vehicleTypeInput, gasInput;
    Button btnAddVehicle;
    FirebaseFirestore firebaseFirestore;
    String plateNumber, vehicleType;
    float gas;
    Vehicle vehicleToEdit;

    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_add_vehicle, container, false);
        if(getArguments() != null) {
            vehicleToEdit = getArguments().getParcelable("id");
        }

        plateNumberInput = view.findViewById(R.id.txtPlateNumber);
        vehicleTypeInput = view.findViewById(R.id.txtvehicletype);
        gasInput = view.findViewById(R.id.txtGas);
        btnAddVehicle = view.findViewById(R.id.btn_add_vehicle);

        firebaseFirestore = firebaseFirestore.getInstance();

        if(vehicleToEdit != null){
            plateNumberInput.getEditText().setText(vehicleToEdit.plateNumber);
            vehicleTypeInput.getEditText().setText(vehicleToEdit.vehicleType);
            gasInput.getEditText().setText(String.valueOf(vehicleToEdit.gas));
            btnAddVehicle.setText("Update Vehicle");
            btnAddVehicle.setOnClickListener(updateClickListener);
        }
        else{
            btnAddVehicle.setOnClickListener(addClickListener);
        }


        return view;
    }

    View.OnClickListener updateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isNetworkAvailable(getContext())) {
                showToast(R.drawable.ic_network_check, "Please verify your network.");
            } else {
                if (!plateValidation() | !vehicleTypeValidation()) {
                    return;
                } else {

                    plateNumber = plateNumberInput.getEditText().getText().toString();
                    vehicleType = vehicleTypeInput.getEditText().getText().toString();
                    gas = Float.parseFloat(gasInput.getEditText().getText().toString());

                    firebaseFirestore.collection("vehicles").whereEqualTo("plateNumber", plateNumber).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                if(!task.getResult().getDocuments().get(0).getId().equals(vehicleToEdit.id)) {
                                    Toast.makeText(getContext(), "Vehicle with the same plate number already exists!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            Map<String,Object> vehicleData = new HashMap<>();
                            vehicleData.put("plateNumber", plateNumber);
                            vehicleData.put("vehicleType", vehicleType);
                            vehicleData.put("gas", gas);
                            firebaseFirestore.collection("vehicles")
                            .document(vehicleToEdit.id)
                            .set(vehicleData)
                            .addOnCompleteListener(task2 -> {
                                if(!task2.isSuccessful()){
                                    Log.d("Storing",task2.getException().getMessage());
                                }
                                else{
                                    Toast.makeText(getContext(), "Vehicle was updated successfully" +
                                            "", Toast.LENGTH_LONG).show();

                                    getParentFragmentManager().popBackStackImmediate();
                                }
                            }).addOnFailureListener(e -> Log.d("Storing",e.getMessage()));
                        }
                    });
                }
            }
        }
    };

    View.OnClickListener addClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isNetworkAvailable(getContext())) {
                showToast(R.drawable.ic_network_check, "Please verify your network.");
            } else {
                if (!plateValidation() | !vehicleTypeValidation()) {
                    return;
                } else {
                    plateNumber = plateNumberInput.getEditText().getText().toString();
                    vehicleType = vehicleTypeInput.getEditText().getText().toString();
                    gas = Float.parseFloat(gasInput.getEditText().getText().toString());

                    firebaseFirestore.collection("vehicles").whereEqualTo("plateNumber", plateNumber).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if (!task.getResult().isEmpty()){
                                Toast.makeText(getContext(), "Vehicle with the same plate number already exists!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<String,Object> vehicleData = new HashMap<>();
                            vehicleData.put("plateNumber", plateNumber);
                            vehicleData.put("vehicleType", vehicleType);
                            vehicleData.put("gas", gas);
                            vehicleData.put("status", "available");
                            firebaseFirestore.collection("vehicles")
                                    .add(vehicleData)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(!task.isSuccessful()){
                                                Log.d("Storing",task.getException().getMessage());
                                            }
                                            else{
                                                Toast.makeText(getContext(), "Vehicle was added successfully" +
                                                        "", Toast.LENGTH_LONG).show();

                                                getParentFragmentManager().popBackStackImmediate();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Storing",e.getMessage());
                                        }
                                    });
                        }
                        else{
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("Register a Driver");
    }

    boolean plateValidation() {
        String email = plateNumberInput.getEditText().getText().toString();
        if (email.isEmpty()) {
            plateNumberInput.setError("Please enter a plate number");
            return false;
        } else {
            return true;
//            String regex = "[A-Z]{1,3}-[A-Z]{1,2}-[0-9]{1,4}";
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(email);
//            if (matcher.matches()) {
//                plateNumberInput.setError(null);
//                return true;
//            } else {
//                plateNumberInput.setError("Please enter a valid plate number");
//                return false;
//            }
        }
    }

    boolean vehicleTypeValidation() {
        String username = vehicleTypeInput.getEditText().getText().toString();
        if (username.isEmpty()) {
            vehicleTypeInput.setError("Please enter a Username");
            return false;
        } else {
            vehicleTypeInput.setError(null);
            return true;
        }
    }


    public void showToast(int icon, String text) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) getView().findViewById(R.id.toast_root));
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.TOP, 0, 0);
        ((ImageView) layout.findViewById(R.id.toast_image)).setImageDrawable(getResources().getDrawable(icon));
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