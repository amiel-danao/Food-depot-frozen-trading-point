package com.thesis.deliverytracking;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.deliverytracking.models.UserInfo;
import com.thesis.deliverytracking.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class VehiclesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VehiclesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static VehiclesFragment newInstance(int columnCount) {
        VehiclesFragment fragment = new VehiclesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drivers_list, container, false);

        FloatingActionButton fabAddDriver = view.findViewById(R.id.fabAddDriver);
        fabAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getActivity().getSupportFragmentManager().beginTransaction()
//                .add(new AddVehicleFragment(), "detail") // Add this transaction to the back stack (name is an optional name for this back stack state, or null).
//                .addToBackStack(null)
//                .commit();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.body_container, new AddVehicleFragment(), "vehicleList");
                transaction.addToBackStack("vehicleList");
                transaction.commit();
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new AddVehicleFragment()).commit();
            }
        });

        recyclerView = view.findViewById(R.id.vehicle_list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Vehicle> vehicles = new ArrayList<>();

        db.collection("vehicles")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Vehicle vehicle = document.toObject(Vehicle.class);
                        vehicle.setId(document.getId());
                        vehicles.add(vehicle);
//                                    Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
//                                Log.d(TAG, "Error getting documents: ", task.getException());
                }

                recyclerView.setAdapter(new VehicleRecyclerViewAdapter(vehicles, getActivity()));
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        toolbar.setTitle("Manage Vehicles");
    }
}