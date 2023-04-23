package com.thesis.deliverytracking.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */

public class Delivery implements Parcelable {

    public String id;
    public int number;
    public String driver;
    public String vehicle;
    public String location;
    public GeoPoint currentLocation;
    public GeoPoint destination;
    public String status;
    public Date creationDate;

    public Delivery() {
    }

    public Delivery(String id, int number, String driver, String vehicle, String location, GeoPoint currentLocation, GeoPoint destination, String status, Date creationDate) {
        this.id = id;
        this.number = number;
        this.driver = driver;
        this.vehicle = vehicle;
        this.location = location;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.status = status;
        this.creationDate = creationDate;
    }

    protected Delivery(Parcel in) {
        id = in.readString();
        number = in.readInt();
        driver = in.readString();
        vehicle = in.readString();
        location = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(number);
        dest.writeString(driver);
        dest.writeString(vehicle);
        dest.writeString(location);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Delivery> CREATOR = new Creator<Delivery>() {
        @Override
        public Delivery createFromParcel(Parcel in) {
            return new Delivery(in);
        }

        @Override
        public Delivery[] newArray(int size) {
            return new Delivery[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }
}