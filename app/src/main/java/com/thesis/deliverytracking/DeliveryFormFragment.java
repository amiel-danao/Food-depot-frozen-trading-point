package com.thesis.deliverytracking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.thesis.deliverytracking.models.UserInfo;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DeliveryFormFragment extends Fragment {

    Map<String, UserInfo> drivers = new Hashtable<>();
    private Spinner driverSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_form, container, false);
        driverSpinner = view.findViewById(R.id.spn_driver);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, driverNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSpinner.setAdapter(adapter);
    }

    private void getDriverList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<UserInfo> users = new ArrayList<>();

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
        toolbar.setTitle("");


    }
}