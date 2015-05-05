package com.activity.nikhilesh.dropphoto;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.dropbox.client2.DropboxAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<String> paths;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        paths = (ArrayList<String>) getIntent().getExtras().getSerializable("Coordinates");

        //setUpMapIfNeeded();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //mMap.getMapAsync(this);
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.setMyLocationEnabled(true);
        for(String path : paths){
            //String path = e.path;
            String[] parts = path.split("_");
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]))).title(parts[0]+" "+parts[3]));
        }
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Present Location"));
    }

    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        Criteria c=new Criteria();
        LocationManager lm =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        String provider=lm.getBestProvider(c, false);
        Location l=lm.getLastKnownLocation(provider);

        Location myLocation = googleMap.getMyLocation();  //Nullpointer exception.........
        LatLng myLatLng = new LatLng(l.getLatitude(),
                l.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                myLatLng, 16));
        for(String path : paths){
            //String path = e.path;
            String[] parts = path.split("_");
            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]))).title(parts[0]+" "+parts[3]));
        }
    }
}
