package com.thesis.deliverytracking;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.databinding.FragmentDeliveryListBinding;
import com.thesis.deliverytracking.models.Location;
import com.thesis.deliverytracking.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.List;


public class DeliveryRecyclerViewAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ViewHolder> {

    public final List<Delivery> mValues;
    private final FragmentActivity activity;
    String pattern = "MM-dd-yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private UserInfo userData;

    public DeliveryRecyclerViewAdapter(List<Delivery> items, FragmentActivity activity, UserInfo userData) {
        mValues = items;
        this.activity = activity;
        this.userData = userData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentDeliveryListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String formattedIdValue = "N/A";
        if(holder.mItem.primaryKey > -1){
            formattedIdValue = String.format("%04d", holder.mItem.primaryKey);
        }
        holder.mIdView.setText("Delivery ID: " +  formattedIdValue);
        if(holder.mItem.creationDate != null) {
            holder.mDeliveryCreationDate.setText("Creation Date: " + simpleDateFormat.format(holder.mItem.creationDate));
        }
        holder.mDeliveryNo.setText("No. of Delivery: " +  holder.mItem.number);
        holder.mDeliveryDriver.setText("Driver: " + holder.mItem.driver);
        holder.mDeliveryVehicle.setText("Vehicle: " + holder.mItem.vehicle);
        holder.mDeliveryLocation.setText("Location: " + holder.mItem.location);
        if(holder.mItem.status.equals("Pending") || holder.mItem.status.equals("Ongoing")){
            holder.mGasConsumption.setVisibility(View.GONE);
        }
        else {
            holder.mGasConsumption.setVisibility(View.VISIBLE);
            holder.mGasConsumption.setText("Gas consumption: " + holder.mItem.gasConsumption + "L");
        }
        holder.parent.setTag(holder.mItem);
        holder.parent.setOnClickListener(view -> {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putParcelable("id", ((Delivery)view.getTag()));
            bundle.putParcelable("userData", userData);
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
        public final TextView mGasConsumption;
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
            mGasConsumption = binding.txtGasConsumption;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDeliveryNo.getText() + "'";
        }
    }
}