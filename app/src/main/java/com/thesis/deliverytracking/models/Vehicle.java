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
    public String status = "available";
    public float gas;

    public Vehicle() {
    }

    public Vehicle(String id, String plateNumber, String vehicleType, String status, float gas) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.status = status;
        this.gas = gas;
    }


    protected Vehicle(Parcel in) {
        id = in.readString();
        plateNumber = in.readString();
        vehicleType = in.readString();
        status = in.readString();
        gas = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(plateNumber);
        dest.writeString(vehicleType);
        dest.writeString(status);
        dest.writeFloat(gas);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public void setId(String id) {
        this.id = id;
    }
}