package com.thesis.deliverytracking;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thesis.deliverytracking.models.UserInfo;
import com.thesis.deliverytracking.databinding.FragmentDriversBinding;
import com.thesis.deliverytracking.models.Vehicle;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link UserInfo}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DriversRecyclerViewAdapter extends RecyclerView.Adapter<DriversRecyclerViewAdapter.ViewHolder> {

    private final List<UserInfo> mValues;
    private final FragmentActivity activity;

    public DriversRecyclerViewAdapter(List<UserInfo> items, FragmentActivity activity) {
        mValues = items;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentDriversBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).username);
        holder.parent.setTag(holder.mItem);
        holder.parent.setOnClickListener(view -> {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putParcelable("id", ((UserInfo)view.getTag()));
            RegisterFragment fragment = new RegisterFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.body_container, fragment, "driverList");
            transaction.addToBackStack("driverList");
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
        public UserInfo mItem;

        public ViewHolder(FragmentDriversBinding binding) {
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