package com.thesis.deliverytracking;

import static androidx.fragment.app.FragmentManager.TAG;

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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.thesis.deliverytracking.models.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddLocationFragment extends Fragment implements OnMapReadyCallback {

    private AutocompleteSupportFragment searchField;
    private EditText placesField;
    private PlacesClient placesClient;
    TextInputLayout addressInput;
    FloatingActionButton btnAddLocation;
    FirebaseFirestore firebaseFirestore;
    String address, locationName;
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
        btnAddLocation = view.findViewById(R.id.btn_add_location);
        fabLocate = view.findViewById(R.id.fabLocate);
        Places.initialize(getActivity(), getActivity().getResources().getString(R.string.google_api_key));
        placesClient = Places.createClient(getActivity());


//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//                getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(@NonNull Place place) {
//                LatLng latLng = place.getLatLng();
//                if (latLng != null) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                }
//            }
//
//            @Override
//            public void onError(@NonNull Status status) {
//                // Handle the error
//            }
//        });

//        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                // Get selected place from autocomplete suggestions
//                AutocompletePrediction selectedPlace = (AutocompletePrediction) adapterView.getItemAtPosition(position);
//                String placeId = selectedPlace.getPlaceId();
//
//                // Fetch place details using the place ID
//                FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, Arrays.asList(Place.Field.LAT_LNG));
//                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
//                    @Override
//                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
//                        Place place = fetchPlaceResponse.getPlace();
//                        LatLng latLng = place.getLatLng();
//                        if (latLng != null) {
//                            // Move the map camera to the selected location
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Handle the error
//                    }
//                });
//            }
//        });

        firebaseFirestore = firebaseFirestore.getInstance();

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
                locationName = placesField.getText().toString();

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
                            locationData.put("position", new GeoPoint(marker.getPosition().latitude, marker.getPosition().longitude));
                            firebaseFirestore.collection("locations")
                            .document(locationToEdit.id)
                            .set(locationData)
                            .addOnCompleteListener(task2 -> {
                                if(!task2.isSuccessful()){
                                    Log.d("Storing",task2.getException().getMessage());
                                }
                                else{
                                    Toast.makeText(getContext(), "Location was updated successfully", Toast.LENGTH_LONG).show();

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
                    locationName = placesField.getText().toString();

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
        if(locationToEdit != null) {
            toolbar.setTitle("Edit Location");
        }
        else{
            toolbar.setTitle("Add new Location");
        }

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
        String locationName = placesField.getText().toString();
        if (locationName.isEmpty()) {
            placesField.setError("Please enter a short name");
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

        if(locationToEdit != null){
            defaultLatLng = new LatLng(locationToEdit.position.getLatitude(), locationToEdit.position.getLongitude());
        }
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


        searchField = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        searchField.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        searchField.setCountries("PH");
        placesField = searchField.getView().findViewById(R.id.places_autocomplete_search_input);
        // Set up a PlaceSelectionListener to handle the response.
        searchField.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("myLogTag", "Place: " + place.getName() + ", " + place.getId());
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    marker.setPosition(latLng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    addressInput.getEditText().setText(place.getAddress());
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("myLogTag", "An error occurred: " + status);
            }
        });

        if(locationToEdit != null){
            addressInput.getEditText().setText(locationToEdit.address);
            searchField.setText(locationToEdit.locationName);
            btnAddLocation.setOnClickListener(updateClickListener);
        }
        else{
            btnAddLocation.setOnClickListener(addClickListener);
        }

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