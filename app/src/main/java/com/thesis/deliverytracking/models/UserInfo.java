package com.thesis.deliverytracking.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    public String id;
    public String email;
    public String username;
    public String fullName;
    public String role;

    public UserInfo() {
    }

    public UserInfo(String id, String email, String username, String fullname, String role) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.fullName = fullname;
        this.role = role;
    }


    protected UserInfo(Parcel in) {
        id = in.readString();
        email = in.readString();
        username = in.readString();
        fullName = in.readString();
        role = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(fullName);
        dest.writeString(role);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
