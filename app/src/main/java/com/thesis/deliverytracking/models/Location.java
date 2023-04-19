package com.thesis.deliverytracking.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */

public class Location implements Parcelable {

    public String id;
    public GeoPoint position;
    public String address;
    public String locationName;

    public Location() {
    }

    public Location(String id, GeoPoint position, String address, String name) {
        this.id = id;
        this.position = position;
        this.address = address;
        this.locationName = name;
    }

    protected Location(Parcel in) {
        id = in.readString();
        address = in.readString();
        locationName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(address);
        dest.writeString(locationName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }
}