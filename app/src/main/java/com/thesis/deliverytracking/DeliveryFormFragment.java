package com.thesis.deliverytracking;

import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.deliverytracking.adapters.CustomSpinnerAdapter;
import com.thesis.deliverytracking.adapters.VehicleSpinnerAdapter;
import com.thesis.deliverytracking.misc.CustomUnderlineSpan;
import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.models.UserInfo;
import com.thesis.deliverytracking.models.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DeliveryFormFragment extends Fragment {

    Map<String, UserInfo> drivers = new Hashtable<>();
    Map<String, Vehicle> vehicles = new Hashtable<>();
    private Spinner driverSpinner, vehicleSpinner, locationSpinner, noDeliverySpinner;
    private Map<String, Location> locations = new Hashtable<>();;
    private Button btnProceed;
    private FirebaseFirestore firebaseFirestore;
    private GeoPoint defaultDeliveryPosition = new GeoPoint(14.2999129, 120.9528338);
    private Vehicle selectedVehicle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_form, container, false);
        noDeliverySpinner = view.findViewById(R.id.spn_no_delivery);
        driverSpinner = view.findViewById(R.id.spn_driver);
        vehicleSpinner = view.findViewById(R.id.spn_vehicle);
        locationSpinner = view.findViewById(R.id.spn_location);
        btnProceed = view.findViewById(R.id.btn_proceed);
        firebaseFirestore = FirebaseFirestore.getInstance();

        int color = ContextCompat.getColor(getContext(), R.color.orange_2);
        CustomUnderlineSpan customUnderlineSpan = new CustomUnderlineSpan(color);

        TextView driverLabel = view.findViewById(R.id.txt_driver);
        SpannableString contentDriver = new SpannableString(driverLabel.getText());
        contentDriver.setSpan(customUnderlineSpan, 0, contentDriver.length(), 0);
        driverLabel.setText(contentDriver);

        TextView vehicleLabel = view.findViewById(R.id.txt_vehicle);
        SpannableString contentVehicle = new SpannableString(vehicleLabel.getText());
        contentVehicle.setSpan(customUnderlineSpan, 0, contentVehicle.length(), 0);
        vehicleLabel.setText(contentVehicle);

        TextView locationLabel = view.findViewById(R.id.txt_location);
        SpannableString contentLocation = new SpannableString(locationLabel.getText());
        contentLocation.setSpan(customUnderlineSpan, 0, contentLocation.length(), 0);
        locationLabel.setText(contentLocation);

        vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedVehicle = (Vehicle)view.getTag();
                Log.d("myLogTag", selectedVehicle.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getDriverList();

        return view;
    }

    private void populateDriverSpinner(){
        List<String> driverNames = new ArrayList<>();
        for (Map.Entry<String, UserInfo> entry : drivers.entrySet()) {
            String key = entry.getKey();
            UserInfo val = entry.getValue();

            driverNames.add(val.username);
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), R.layout.custom_spinner_dropdown_item, driverNames);
        driverSpinner.setAdapter(adapter);
        getVehicleList();
    }

    private void getVehicleList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("vehicles")
        .whereEqualTo("status", "available")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        vehicles.put(document.getId(), document.toObject(Vehicle.class));//                                    Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    populateVehicleSpinner();
                } else {
                    Toast.makeText(getContext(), "Error fetching vehicle list", Toast.LENGTH_LONG).show();
//                                Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void populateVehicleSpinner(){
        List<Vehicle> vehicleNames = new ArrayList<>();
        for (Map.Entry<String, Vehicle> entry : vehicles.entrySet()) {
            String key = entry.getKey();
            Vehicle val = entry.getValue();
            val.setId(key);
            vehicleNames.add(val);

            if(selectedVehicle == null){
                selectedVehicle = val;
            }
        }

        if(getActivity() != null) {
            VehicleSpinnerAdapter adapter = new VehicleSpinnerAdapter(getContext(), R.layout.custom_spinner_dropdown_item, vehicleNames);
            vehicleSpinner.setAdapter(adapter);
            getLocationList();
        }
    }

    private void getLocationList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("locations")
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    locations.put(document.getId(), document.toObject(Location.class));//                                    Log.d(TAG, document.getId() + " => " + document.getData());
                }
                populateLocationSpinner();
            } else {
                Toast.makeText(getContext(), "Error fetching vehicle list", Toast.LENGTH_LONG).show();
//                                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    private void populateLocationSpinner() {
        List<String> locationNames = new ArrayList<>();
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            String key = entry.getKey();
            Location val = entry.getValue();

            locationNames.add(val.locationName);
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), R.layout.custom_spinner_dropdown_item, locationNames);
        locationSpinner.setAdapter(adapter);
        btnProceed.setEnabled(true);
        btnProceed.setOnClickListener(proceedClickListener);
    }


    private final View.OnClickListener proceedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!validForm()){
                return;
            }

            CollectionReference itemsCollection = firebaseFirestore.collection("deliveries");


// Get the current primary key value from the database
            AtomicInteger currentPrimaryKey = new AtomicInteger(0);

            itemsCollection
            .orderBy("primaryKey", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Integer lastPrimaryKey = document.getLong("primaryKey").intValue();
                    currentPrimaryKey.set(lastPrimaryKey);
                }

                // Create a new batch
//                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                // Increment the primary key value
                int newPrimaryKey = currentPrimaryKey.incrementAndGet();
                Map<String,Object> deliveryData = new HashMap<>();
                deliveryData.put("number", 1);
                deliveryData.put("driver", driverSpinner.getSelectedItem().toString());
                deliveryData.put("vehicle", vehicleSpinner.getSelectedItem().toString());
                deliveryData.put("location", locationSpinner.getSelectedItem().toString());
                deliveryData.put("status", "Pending");
                deliveryData.put("creationDate", FieldValue.serverTimestamp());
                deliveryData.put("currentLocation", defaultDeliveryPosition);
                deliveryData.put("primaryKey", newPrimaryKey);
                float gas = 0;
                if (selectedVehicle != null){
                    gas = selectedVehicle.gas;
                }
                deliveryData.put("gasConsumption",gas);

                itemsCollection
                .add(deliveryData).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Delivery was created successfully"
                                , Toast.LENGTH_LONG).show();

                        DeliveryListFragment fragment = new DeliveryListFragment();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.body_container, fragment).commit();
                    }
                    else{
                        Toast.makeText(getContext(), "Error creating delivery: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            });
        }
    };

    private boolean validForm() {
        if(driverSpinner.getSelectedItem().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select a valid driver", Toast.LENGTH_LONG).show();
            return false;
        }
//        if(noDeliverySpinner.getSelectedItem().toString().isEmpty()){
//            Toast.makeText(getContext(), "Please select a valid no of delivery", Toast.LENGTH_LONG).show();
//            return false;
//        }
        if(vehicleSpinner.getSelectedItem().toString().isEmpty()){
            Toast.makeText(getContext(), "Please select a valid vehicle", Toast.LENGTH_LONG).show();
            return false;
        }
        if(locationSpinner.getSelectedItem().toString().isEmpty()){
            Toast.makeText(getContext(), "Please select a valid location", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void getDriverList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
        .whereEqualTo("role", "Delivery")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        drivers.put(document.getId(), document.toObject(UserInfo.class));//                                    Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    populateDriverSpinner();
                } else {
                    Toast.makeText(getContext(), "Error fetching drivers list", Toast.LENGTH_LONG).show();
//                                Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("New Delivery");
    }
}