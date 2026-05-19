package com.example.lostandfoundapp;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.lostandfoundapp.data.LostItem;
import com.example.lostandfoundapp.database.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;


public class MapActivity extends AppCompatActivity {
    private static final int REQ_LOC = 2001;

    // fixed radius for the 5km filter
    private static final int RADIUS_KM = 5;

    private GoogleMap map;
    private FusedLocationProviderClient fusedClient;
    private DatabaseHelper databaseHelper;

    // Centre of the radius search = where the user is right now
    private double userLat = 0, userLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        databaseHelper = new DatabaseHelper(this);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFrag != null) {

            // getMapAsync
            // map loads in the background
            mapFrag.getMapAsync(googleMap -> {
                map = googleMap;
                map.getUiSettings().setZoomControlsEnabled(true);
                enableMyLocationAndLoad();
            });
        }
    }
    private void enableMyLocationAndLoad() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
            return;
        }

        // The blue dot showing the user's location
        map.setMyLocationEnabled(true);
        fusedClient.getLastLocation().addOnSuccessListener(this, loc -> {
            if (loc != null) {
                userLat = loc.getLatitude();
                userLng = loc.getLongitude();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLat, userLng), 13f));
            }
            refreshMarkers();
        });
    }
    private void refreshMarkers() {
        map.clear();
        ArrayList<LostItem> items = databaseHelper.getAllItems();
        int shown = 0;
        for (LostItem item : items) {
            // Items saved before Task 9.1 have lat/lng = 0
            if (item.getLatitude() == 0 && item.getLongitude() == 0) continue;

            // === The actual radius search ===
            // distanceBetween() uses Haversine under the hood, returns metres
            float[] dist = new float[1];
            Location.distanceBetween(userLat, userLng,
                    item.getLatitude(), item.getLongitude(), dist);
            double km = dist[0] / 1000.0;

            // out of range → don't draw a marker
            if (km > RADIUS_KM) continue;
            LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());

            // Colour code so Lost vs Found pops at a glance
            float hue = "Lost".equalsIgnoreCase(item.getPostType())
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_GREEN;
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(item.getName() + " (" + item.getPostType() + ")")
                    .snippet(item.getCategory() + " · " + item.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            shown++;
        }
        Toast.makeText(this,
                shown + " item(s) within " + RADIUS_KM + "km",
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOC
                && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndLoad();
        }
    }
}