package com.thesis.deliverytracking.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfo {
    public final String id;
    public final String username;
    public final String role;

    public UserInfo(String id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    @Override
    public String toString() {
        return username;
    }
}
