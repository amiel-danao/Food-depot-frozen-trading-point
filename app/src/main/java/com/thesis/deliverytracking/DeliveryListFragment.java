package com.thesis.deliverytracking;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static com.google.firebase.firestore.DocumentChange.Type.REMOVED;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class DeliveryListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    DeliveryRecyclerViewAdapter adapter;
    private String filter = "Pending";
    UserInfo userData;
    private int selectedTabIndex = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeliveryListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DeliveryListFragment newInstance(int columnCount) {
        DeliveryListFragment fragment = new DeliveryListFragment();
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
            userData = getArguments().getParcelable("userData");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(tabLayout != null){
            tabLayout.getTabAt(selectedTabIndex).select();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_list_list, container, false);

        recyclerView = view.findViewById(R.id.delivery_list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }



        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (adapter == null){
                    return;
                }
                adapter.mValues.clear();
                adapter.notifyDataSetChanged();
                filter = tab.getText().toString();
                selectedTabIndex = tab.getPosition();
                getDeliveries();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        getDeliveries();
        return view;
    }

    private void getDeliveries(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Delivery> deliveries = new ArrayList<>();

        Query query = db.collection("deliveries")
        .whereEqualTo("status", filter)
        .orderBy("primaryKey", Query.Direction.DESCENDING);

        if(userData != null && userData.role.equals("Delivery")){
            query = db.collection("deliveries")
                    .whereEqualTo("status", filter)
                    .whereEqualTo("driver", userData.username)
                    .orderBy("primaryKey", Query.Direction.DESCENDING);
        }

        adapter = new DeliveryRecyclerViewAdapter(deliveries, getActivity(), userData);
        recyclerView.setAdapter(adapter);

        query.addSnapshotListener(MetadataChanges.EXCLUDE, (value, error) -> {
            if (error != null) {
                // Handle any errors
                return;
            }

            for (DocumentChange change : value.getDocumentChanges()) {
                DocumentSnapshot document = change.getDocument();

                switch (change.getType()) {
                    case ADDED:
                        // Handle added document
                        Delivery addedDelivery = document.toObject(Delivery.class);
                        addedDelivery.setId(document.getId());

                        if(!deliveries.contains(addedDelivery)) {
                            deliveries.add(addedDelivery);
                            adapter.notifyItemInserted(deliveries.size()); // Notify adapter of the new item
                        }
                        break;

                    case MODIFIED:
                        // Handle modified document
                        Delivery modifiedDelivery = document.toObject(Delivery.class);
                        modifiedDelivery.setId(document.getId());

                        int existingModifiedItemIndex = deliveries.indexOf(modifiedDelivery);
                        if(existingModifiedItemIndex > -1){
                            deliveries.set(existingModifiedItemIndex, modifiedDelivery);
                            adapter.notifyItemChanged(existingModifiedItemIndex);
                        }
                        break;

                    case REMOVED:
                        // Handle removed document
                        String removedDeliveryId = document.getId();

                        int existingDeletedItemIndex = deliveries.indexOf(removedDeliveryId);
                        if(existingDeletedItemIndex > -1) {
                            deliveries.remove(existingDeletedItemIndex);
                            adapter.notifyItemRemoved(existingDeletedItemIndex); // Notify adapter of the removed item
                        }

                        break;
                }
            }
        });


//        query.get()
//        .addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    Delivery delivery = document.toObject(Delivery.class);
//                    delivery.setId(document.getId());
//                    deliveries.add(delivery);
//                }
//            } else {
////                                Log.d(TAG, "Error getting documents: ", task.getException());
//            }
//
//            adapter = new DeliveryRecyclerViewAdapter(deliveries, getActivity(), userData);
//            recyclerView.setAdapter(adapter);
//        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(toolbar != null) {
            if (userData != null) {
                toolbar.setTitle("Deliveries (" + userData.username + ")");
            } else {
                toolbar.setTitle("Deliveries");
            }
        }
    }
}