package com.thesis.deliverytracking;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.Manifest;

public class TrackDeliveryFragment extends Fragment implements OnMapReadyCallback, RoutingListener {

    private GoogleMap map;
    ArrayList markerPoints = new ArrayList();
    private FirebaseFirestore firebaseFirestore;
    private View view;
    private LatLng start, end;
    private List<Polyline> polyLines = null;
    private Delivery deliveryToView;
    private UiSettings mUiSettings;
    private TextView distanceTxtView;
    private Geocoder geocoder;
    private TextView txtCurrentLocation;
    private TextView txtDestination;
    private UserInfo userData;
    private LocationManager locationManager;

    public TrackDeliveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_delivery, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            deliveryToView = getArguments().getParcelable("id");
            userData = getArguments().getParcelable("userData");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        distanceTxtView = view.findViewById(R.id.txtDistance);
        txtCurrentLocation = view.findViewById(R.id.txtCurrentLocation);
        txtDestination = view.findViewById(R.id.txtDestination);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }


        return view;
    }

    private void getDestination() {
        if (deliveryToView == null) {
            Toast.makeText(getContext(), "No data", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseFirestore.collection("locations").whereEqualTo("locationName", deliveryToView.location).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Location> locations = task.getResult().toObjects(Location.class);
                    if (!locations.isEmpty()) {
                        Location destination = locations.get(0);
                        end = new LatLng(destination.position.getLatitude(), destination.position.getLongitude());

                        start = new LatLng(deliveryToView.currentLocation.getLatitude(), deliveryToView.currentLocation.getLongitude());
                        //mapView.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 14));
                        findRoutes(start, end);
                        attachLocationChangeListener();
                    }
                } else {

                }
            }
        });
    }

    private void attachLocationChangeListener() {
        final DocumentReference docRef = firebaseFirestore.collection("deliveries").document(deliveryToView.id);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                    ? "Local" : "Server";

            if (snapshot != null && snapshot.exists()) {
                GeoPoint currentLocation = (GeoPoint) snapshot.get("currentLocation");
                start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                findRoutes(start, end);
            } else {

            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

        mUiSettings.setMapToolbarEnabled(true);
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        getDestination();

        if (userData != null && userData.role.equals("Delivery")) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled(true);


            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(saveLocationRunnable, 0, 3, TimeUnit.SECONDS);
        }


    }

    Runnable saveLocationRunnable = new Runnable() {
        public void run() {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


            Map<String, Object> data = new HashMap<>();
            data.put("currentLocation", new GeoPoint(location.getLatitude(), location.getLatitude()));

            firebaseFirestore.collection("deliveries").document(deliveryToView.id)
            .set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("myLogTag", "Current Location saved");
                    }
                    else{
                        Log.d("myLogTag", task.getException().getMessage());
                    }
                }
            });
        }
    };

    public void findRoutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(getContext(),"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    //.key("AIzaSyD4uStbluZBnwKADWRtCPalZoddDXdNQbs")  //also define your api key here.
                    .key(getContext().getString(R.string.google_api_key))
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = view.findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(getContext(),"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        map.clear();

        if(polyLines !=null) {
            polyLines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polyLines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.purple_500));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = map.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polyLines.add(polyline);

            }
            else {

            }

        }

        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("Current Location");
        Marker sMarker = map.addMarker(startMarker);

        txtCurrentLocation.setText("Current Location: " + getCity(sMarker));

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        Marker eMarker = map.addMarker(endMarker);

        txtDestination.setText("Destination: " + getCity(eMarker));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 14));
        float[] results = new float[1];
        android.location.Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);

        if (results.length > 0) {
            distanceTxtView.setText(String.format("%.2f", (results[0] / 1000)) + " km");
            for (float f :
                    results) {
                Log.d("myLogTag", String.valueOf(f));
            }
        }
    }

    @Override
    public void onRoutingCancelled() {
        findRoutes(start,end);
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


    private ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            if (permissions.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Permission granted
            } else {
                // Permission denied
            }
        });



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("Track delivery");

    }

}