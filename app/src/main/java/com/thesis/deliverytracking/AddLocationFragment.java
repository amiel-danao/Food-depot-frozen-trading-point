package com.thesis.deliverytracking;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.thesis.deliverytracking.models.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddLocationFragment extends Fragment implements OnMapReadyCallback {

    TextInputLayout addressInput, locationNameInput;
    FloatingActionButton btnAddLocation;
    FirebaseFirestore firebaseFirestore;
    String address, locationName;
    float gas;
    Location locationToEdit;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private LatLng defaultLatLng;
    private Marker marker;
    private FloatingActionButton fabLocate;
    Geocoder geocoder;

    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_add_location, container, false);

        if(getArguments() != null) {
            locationToEdit = getArguments().getParcelable("id");
        }

        addressInput = view.findViewById(R.id.txtAddress);
        locationNameInput = view.findViewById(R.id.locationNameInput);
        btnAddLocation = view.findViewById(R.id.btn_add_location);
        fabLocate = view.findViewById(R.id.fabLocate);

        firebaseFirestore = firebaseFirestore.getInstance();

        if(locationToEdit != null){
            addressInput.getEditText().setText(locationToEdit.address);
            btnAddLocation.setOnClickListener(updateClickListener);
        }
        else{
            btnAddLocation.setOnClickListener(addClickListener);
        }

        fabLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (marker != null && mMap != null){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
            }
        });

        return view;
    }

    View.OnClickListener updateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), "Please verify your network.", Toast.LENGTH_LONG).show();
            } else {

                address = addressInput.getEditText().getText().toString();
                locationName = locationNameInput.getEditText().getText().toString();

                if (!addressValidation()  || !locationNameValidation()) {
                    return;
                } else {



                    firebaseFirestore.collection("locations").whereEqualTo("locationName", locationName).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                if(!task.getResult().getDocuments().get(0).getId().equals(locationToEdit.id)) {
                                    Toast.makeText(getContext(), "Location with the same name already exists!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            Map<String,Object> locationData = new HashMap<>();
                            locationData.put("locationName", locationName);
                            locationData.put("address", address);
                            locationData.put("position", marker.getPosition());
                            firebaseFirestore.collection("vehicles")
                            .document(locationToEdit.id)
                            .set(locationData)
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
                Toast.makeText(getContext(), "Please verify your network.", Toast.LENGTH_LONG).show();
            } else {
                if (!addressValidation() || !locationNameValidation()) {
                    return;
                } else {
                    address = addressInput.getEditText().getText().toString();
                    locationName = locationNameInput.getEditText().getText().toString();

                    firebaseFirestore.collection("locations").whereEqualTo("locationName", locationName).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if (!task.getResult().isEmpty()){
                                Toast.makeText(getContext(), "Location with the same name already exists!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<String,Object> locationData = new HashMap<>();
                            locationData.put("locationName", locationName);
                            locationData.put("address", address);
                            locationData.put("position", new GeoPoint(marker.getPosition().latitude, marker.getPosition().longitude));
                            firebaseFirestore.collection("locations")
                            .add(locationData)
                            .addOnCompleteListener(task1 -> {
                                if(!task1.isSuccessful()){
                                    Log.d("Storing", task1.getException().getMessage());
                                }
                                else{
                                    Toast.makeText(getContext(), "Location was added successfully" +
                                            "", Toast.LENGTH_LONG).show();

                                    getParentFragmentManager().popBackStackImmediate();
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
        toolbar.setTitle("Add new Location");

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    boolean addressValidation() {
        String address = addressInput.getEditText().getText().toString();
        if (address.isEmpty()) {
            addressInput.setError("Please select a valid point in map");
            return false;
        } else {
            return true;
        }
    }

    boolean locationNameValidation() {
        String locationName = locationNameInput.getEditText().getText().toString();
        if (locationName.isEmpty()) {
            locationNameInput.setError("Please enter a short name");
            return false;
        } else {
            return true;
        }
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

        mUiSettings.setMapToolbarEnabled(true);
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        // Add a marker in Sydney and move the camera
        defaultLatLng = new LatLng(14.2999129, 120.9528338);
        marker = mMap.addMarker(new MarkerOptions()
                .position(defaultLatLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLatLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));

        addressInput.getEditText().setText(getCity(marker));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                addressInput.getEditText().setText(getCity(marker));
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });

    }

    private String getCity(Marker marker){
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address = addresses.get(0);

        if (address != null) {
            return address.getLocality();
        }
        return "";
    }
}