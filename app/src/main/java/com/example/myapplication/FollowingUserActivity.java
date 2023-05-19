package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.myapplication.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class FollowingUserActivity extends AppCompatActivity {
    public static final double RADIUS_OF_EARTH_KM = 6371.01;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    FirebaseAuth auth;
    FirebaseDatabase db;
    MaterialToolbar menuToolBar;
    MapView map;
    Marker followingUserMarker, currentUserMarker;
    Double currentUserLatitude = 0d, currentUserLongitude = 0d;
    User currentUser, followingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_user);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = notifyCallbackChanges();
        locationRequest = createLocationRequest();
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        menuToolBar = findViewById(R.id.appBarMenu);
        setSupportActionBar(menuToolBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserLocationFromDb();
        getFollowingUserLocation(getIntent().getStringExtra("followingUserId"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        stopLocationUpdates();
    }

    private LocationCallback notifyCallbackChanges() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentUserLatitude = location.getLatitude();
                    currentUserLongitude = location.getLongitude();
                    if (currentUserMarker == null) {
                        currentUserMarker = createMarker(new GeoPoint(currentUserLatitude, currentUserLongitude),
                                "",
                                "",
                                R.drawable.ic_user_marker_blue);
                        placeMarker(currentUserMarker);
                    } else {
                        currentUserMarker.setPosition(new GeoPoint(currentUserLatitude, currentUserLongitude));
                        setUserCurrentLocationOnDb();
                    }
                }
            }
        };
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private Marker createMarker(GeoPoint p, String title, String desc, int iconID) {
        Marker marker = null;
        if (map != null) {
            marker = new Marker(map);
            if (title != null) marker.setTitle(title);
            if (desc != null) marker.setSubDescription(desc);
            if (iconID != 0) {
                Drawable myIcon = getResources().getDrawable(iconID, this.getTheme());
                marker.setIcon(myIcon);
            }
            marker.setPosition(p);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        }
        return marker;
    }

    private void placeMarker(Marker marker) {
        map.getOverlays().add(marker);
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result * 100.0) / 100.0;
    }

    private void getFollowingUserLocation(String id) {
        DatabaseReference ref = db.getReference("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getKey().equals(id)) {
                        followingUser = data.getValue(User.class);
                        if (followingUserMarker == null) {
                            followingUserMarker = createMarker(new GeoPoint(followingUser.getLatitude(), followingUser.getLongitude()),
                                    followingUser.getName() + " " + followingUser.getLastName(),
                                    "",
                                    R.drawable.ic_user_marker_teal);
                            placeMarker(followingUserMarker);
                            IMapController mapController = map.getController();
                            mapController.setZoom(13.0);
                            mapController.setCenter(new GeoPoint(followingUser.getLatitude(), followingUser.getLongitude()));
                            menuToolBar.setTitle("Distance: " + String.valueOf(distance(currentUserLatitude, currentUserLongitude, followingUser.getLatitude(), followingUser.getLongitude())));
                        } else {
                            followingUserMarker.setPosition(new GeoPoint(followingUser.getLatitude(), followingUser.getLongitude()));
                            IMapController mapController = map.getController();
                            mapController.setZoom(13.0);
                            mapController.setCenter(new GeoPoint(followingUser.getLatitude(), followingUser.getLongitude()));
                            menuToolBar.setTitle("Distance to " + followingUser.getName() + " " + followingUser.getLastName() + ": " + String.valueOf(distance(currentUserLatitude, currentUserLongitude, followingUser.getLatitude(), followingUser.getLongitude())));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUserLocationFromDb() {
        DatabaseReference ref = db.getReference("users").child(auth.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(auth.getUid())) {
                    currentUserLatitude = snapshot.getValue(User.class).getLatitude();
                    currentUserLongitude = snapshot.getValue(User.class).getLongitude();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setUserCurrentLocationOnDb() {
        db.getReference("users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(auth.getUid())) {
                    currentUser = snapshot.getValue(User.class);
                    currentUser.setLatitude(currentUserLatitude);
                    currentUser.setLongitude(currentUserLongitude);
                    db.getReference("users").child(auth.getUid()).setValue(currentUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}