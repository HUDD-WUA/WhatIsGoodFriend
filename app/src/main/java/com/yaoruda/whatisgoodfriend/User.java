package com.yaoruda.whatisgoodfriend;

/**
 * Created by yaoruda on 2017/5/5.
 */
public class User {
    private String name;
    private String password;
    private String location_Latitude;
    private String location_Longitude;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getLocation_Latitude() {
        return location_Latitude;
    }
    public void setLocation_Latitude(String location_Latitude) {
        this.location_Latitude = location_Latitude;
    }
    public String getLocation_Longitude() {
        return location_Longitude;
    }
    public void setLocation_Longitude(String location_Longitude) {
        this.location_Longitude = location_Longitude;
    }
}
