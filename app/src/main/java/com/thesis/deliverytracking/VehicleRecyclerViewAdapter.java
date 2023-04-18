package com.thesis.deliverytracking;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thesis.deliverytracking.models.Vehicle;
import com.thesis.deliverytracking.databinding.FragmentVehicleBinding;

import java.util.List;

public class VehicleRecyclerViewAdapter extends RecyclerView.Adapter<VehicleRecyclerViewAdapter.ViewHolder> {

    private final List<Vehicle> mValues;
    private final FragmentActivity activity;

    public VehicleRecyclerViewAdapter(List<Vehicle> items, FragmentActivity activity) {
        mValues = items;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentVehicleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).plateNumber);
        holder.mContentView.setText(mValues.get(position).vehicleType);
        holder.parent.setTag(holder.mItem);
        holder.parent.setOnClickListener(view -> {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putParcelable("id", ((Vehicle)view.getTag()));
            AddVehicleFragment fragment = new AddVehicleFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.body_container, fragment, "vehicleList");
            transaction.addToBackStack("vehicleList");
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public final View parent;
        public Vehicle mItem;

        public ViewHolder(FragmentVehicleBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            parent = binding.parent;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}