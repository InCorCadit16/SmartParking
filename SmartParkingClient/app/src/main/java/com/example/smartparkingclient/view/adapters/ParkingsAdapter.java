package com.example.smartparkingclient.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.models.Parking;

import java.util.List;


public class ParkingsAdapter extends BaseAdapter {
    private final List<Parking> parkings;
    private final Context context;

    public ParkingsAdapter(Context context, List<Parking> parkings) {
        this.context = context;
        this.parkings = parkings;
    }

    @Override
    public int getCount() {
        return parkings.size();
    }

    @Override
    public Parking getItem(int position) {
        return parkings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return parkings.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.parking_item, parent, false);
        }

        Parking parking = getItem(position);

        TextView name = convertView.findViewById(R.id.parking_name);
        TextView placesCount = convertView.findViewById(R.id.parking_places_count);
        name.setText(parking.name);
        placesCount.setText(String.valueOf(parking.placesNumber));

        return convertView;
    }
}
