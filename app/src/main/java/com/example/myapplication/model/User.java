package com.example.myapplication.model;

import android.net.Uri;

public class User {
    private Uri profileImage;
    private String identification;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private Double latitude;
    private Double longitude;

    public User() {
    }

    public User(String identification, String name, String lastName, String email, String password, Double latitude, Double longitude) {
        this.identification = identification;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Uri getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Uri profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "User{" +
                "profileImage=" + profileImage +
                ", identification='" + identification + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
