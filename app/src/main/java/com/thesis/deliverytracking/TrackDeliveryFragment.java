package com.thesis.deliverytracking;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.models.UserInfo;

import java.io.File;
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

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.EasyPermissions;

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
    private TextView txtPermissionWarning;
    private Button btnDriverAction;
    private View uploadParent;
    private Button btnUpload;
    private Uri filePath;
    private ImageView uploadPicture;

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
        txtPermissionWarning = view.findViewById(R.id.txtPermissionWarning);
        btnDriverAction = view.findViewById(R.id.btnDriverAction);
        uploadParent = view.findViewById(R.id.uploadParent);
        btnUpload = view.findViewById(R.id.btnUpload);
        uploadPicture = view.findViewById(R.id.uploadPicture);

        if (getArguments() != null) {
            deliveryToView = getArguments().getParcelable("id");
            userData = getArguments().getParcelable("userData");
        }


        //automatically set the delivery status to ongoing
        if(userData != null && userData.role.equals("Delivery")) {
            updateDriverActionButton();
            if (deliveryToView != null && deliveryToView.status.equals("Pending")) {
                setDeliveryStatus("Ongoing");
            }
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

    private void updateDriverActionButton() {
        btnDriverAction.setVisibility(View.VISIBLE);
        if(deliveryToView.status.equals("Ongoing")) {
            btnDriverAction.setOnClickListener(successDeliveryClickListener);
            btnUpload.setOnClickListener(uploadClickListener);

        }
        else if(deliveryToView.status.equals("For Approval")){
            btnDriverAction.setEnabled(false);
            btnDriverAction.setText("Waiting for approval");
        }
    }

    private View.OnClickListener uploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(filePath == null){
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

                if(EasyPermissions.hasPermissions(getContext(), permissions)){
                    imagePicker();
                }
                else{
                    EasyPermissions.requestPermissions(getActivity(), "App need access to your camera and storage", 100, permissions);
                }
            }
            else{
                StorageReference riversRef = FirebaseStorage.getInstance().getReference().child(deliveryToView.id+ "/"+filePath.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(filePath);

// Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        setDeliveryStatus("For Approval");
                    }
                });
            }
        }
    };

//    private ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
//            new ActivityResultContracts.RequestMultiplePermissions(), result ->  {
//                for (String key :
//                        result.keySet()) {
//                    if(!Boolean.TRUE.equals(result.get(key))){
//                        Toast.makeText(getContext(), "Permission for : " + key + " was not granted", Toast.LENGTH_LONG).show();
//                        break;
//                    }
//                }
//            }
//    );

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK && data != null){
//            if(requestCode == FilePickerConst.REQUEST_CODE_PHOTO){
//
//                ArrayList<Uri> file = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
//
//                if(!file.isEmpty()) {
//                    filePath = file.get(0);
//                    Glide.with(getContext()).load(filePath).into(uploadPicture);
//                }
//            }
//        }
//    }


    private void imagePicker() {
//        FilePickerBuilder.getInstance()
//                .setActivityTitle("Select image")
//                .setMaxCount(1)
//                .pickPhoto(getActivity());
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        filePath = data.getData();
                        Glide.with(getContext()).load(filePath).into(uploadPicture);
                    }
                }
            });

    private final View.OnClickListener successDeliveryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            uploadParent.setVisibility(View.VISIBLE);
            btnDriverAction.setVisibility(View.GONE);
//
        }
    };

    private void setDeliveryStatus(String status){
        Map<String, Object> data = new HashMap<>();
        data.put("status", status);
        firebaseFirestore.collection("deliveries").document(deliveryToView.id)
        .set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getContext(), "Delivery status was updated successfully : " + status, Toast.LENGTH_SHORT).show();

                deliveryToView.status = status;
            }
        });
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
                Log.d("myLogTag", "Permission not granted");
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
                Log.d("myLogTag", "Permission not granted");
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
                    .key(getContext().getString(R.string.google_maps_key))
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = view.findViewById(android.R.id.content);
        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
//        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
//        snackbar.show();
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
                txtPermissionWarning.setVisibility(View.GONE);
                Log.d("myLogTag", "Permission was granted");
            } else {
                // Permission denied
                txtPermissionWarning.setVisibility(View.VISIBLE);
                Log.d("myLogTag", "Permission was denied");
            }
        });


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("Track delivery");

    }

}