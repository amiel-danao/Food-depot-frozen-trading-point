package com.thesis.deliverytracking.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */

public class Vehicle implements Parcelable {

    public String id;
    public String plateNumber;
    public String vehicleType;
    public float gas;

    public Vehicle() {
    }

    public Vehicle(String id, String name, String type, float gas) {
        this.id = id;
        this.plateNumber = name;
        this.vehicleType = type;
        this.gas = gas;
    }

    protected Vehicle(Parcel in) {
        id = in.readString();
        plateNumber = in.readString();
        vehicleType = in.readString();
        gas = in.readFloat();
    }

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    @Override
    public String toString() {
        return plateNumber;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(plateNumber);
        parcel.writeString(vehicleType);
        parcel.writeFloat(gas);
    }
}