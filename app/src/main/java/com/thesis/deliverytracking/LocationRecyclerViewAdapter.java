package com.thesis.deliverytracking;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.databinding.FragmentLocationsBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {

    private final List<Location> mValues;
    private final FragmentActivity activity;

    public LocationRecyclerViewAdapter(List<Location> items, FragmentActivity activity) {
        mValues = items;
        this.activity = activity;
    }

    @Override
    public LocationRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new LocationRecyclerViewAdapter.ViewHolder(FragmentLocationsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final LocationRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mAddressView.setText(mValues.get(position).address);
        holder.mShortNameView.setText(mValues.get(position).locationName);

        holder.parent.setTag(holder.mItem);
        holder.parent.setOnClickListener(view -> {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putParcelable("id", ((Location)view.getTag()));
            AddLocationFragment fragment = new AddLocationFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.body_container, fragment, "locationList");
            transaction.addToBackStack("locationList");
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mAddressView;
        public final TextView mShortNameView;
        public final View parent;
        public Location mItem;

        public ViewHolder(FragmentLocationsBinding binding) {
            super(binding.getRoot());
            mAddressView = binding.address;
            mShortNameView = binding.shortName;
            parent = binding.parent;
        }

    }
}