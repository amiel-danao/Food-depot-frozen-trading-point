package com.thesis.deliverytracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thesis.deliverytracking.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private List<String> data;

    public CustomSpinnerAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.text1);
        textView.setText(data.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(data.get(position));

        return convertView;
    }
}
