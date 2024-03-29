package com.thesis.deliverytracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thesis.deliverytracking.R;
import com.thesis.deliverytracking.models.Vehicle;

import java.util.List;

public class VehicleSpinnerAdapter extends ArrayAdapter<Vehicle> {
    private LayoutInflater inflater;
    private List<Vehicle> data;

    public VehicleSpinnerAdapter(Context context, int resource, List<Vehicle> data) {
        super(context, resource, data);
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false);
        }
        convertView.setTag(data.get(position));

        TextView textView = convertView.findViewById(R.id.text1);
        textView.setText(data.get(position).plateNumber);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        convertView.setTag(data.get(position));

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(data.get(position).plateNumber);

        return convertView;
    }
}
