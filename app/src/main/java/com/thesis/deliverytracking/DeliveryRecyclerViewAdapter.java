package com.thesis.deliverytracking;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.databinding.FragmentDeliveryListBinding;
import com.thesis.deliverytracking.models.Location;

import java.text.SimpleDateFormat;
import java.util.List;


public class DeliveryRecyclerViewAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ViewHolder> {

    public final List<Delivery> mValues;
    private final FragmentActivity activity;
    String pattern = "MM-dd-yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    public DeliveryRecyclerViewAdapter(List<Delivery> items, FragmentActivity activity) {
        mValues = items;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentDeliveryListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText("Delivery ID: " +  mValues.get(position).id);
        holder.mDeliveryCreationDate.setText("Creation Date: " + simpleDateFormat.format(mValues.get(position).creationDate));
        holder.mDeliveryNo.setText("No. of Delivery: " +  mValues.get(position).number);
        holder.mDeliveryDriver.setText("Driver: " + mValues.get(position).driver);
        holder.mDeliveryVehicle.setText("Vehicle: " + mValues.get(position).vehicle);
        holder.mDeliveryLocation.setText("Location: " + mValues.get(position).location);
        holder.parent.setTag(holder.mItem);
        holder.parent.setOnClickListener(view -> {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putParcelable("id", ((Delivery)view.getTag()));
            TrackDeliveryFragment fragment = new TrackDeliveryFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.body_container, fragment, "trackLocation");
            transaction.addToBackStack("trackLocation");
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View parent;
        public final TextView mIdView;
        public final TextView mDeliveryCreationDate;
        public final TextView mDeliveryNo;
        public final TextView mDeliveryDriver;
        public final TextView mDeliveryVehicle;
        public final TextView mDeliveryLocation;
        public Delivery mItem;

        public ViewHolder(FragmentDeliveryListBinding binding) {
            super(binding.getRoot());
            parent = binding.parent;
            mIdView = binding.deliveryId;
            mDeliveryCreationDate = binding.deliveryCreationDate;
            mDeliveryNo = binding.deliveryNumber;
            mDeliveryDriver = binding.deliveryDriver;
            mDeliveryVehicle = binding.deliveryVehicle;
            mDeliveryLocation = binding.deliveryLocation;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDeliveryNo.getText() + "'";
        }
    }
}