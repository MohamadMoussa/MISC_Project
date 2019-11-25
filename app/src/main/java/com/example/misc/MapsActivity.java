package com.example.misc;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MapsActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnPoiClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String PREFS = "prefs";
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LinearLayout dragV;
    private RelativeLayout relPanel;
    private ListView listView;
    private FloatingActionButton fab;
    private TextView lblDirections;
    private SlidingUpPanelLayout sLayout;
    private double lati = 0.0, longi = 0.0;
    private static SharedPreferences prefs;
    private MapUtils mapUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain user's location
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dragV = findViewById(R.id.dragview);
        listView = findViewById(R.id.listView);
        lblDirections = findViewById(R.id.txtDirections);
        relPanel = findViewById(R.id.relativeText);
        fab = findViewById(R.id.fab);
        sLayout = findViewById(R.id.main_layout);
        System.out.println("-------------State : " + sLayout.getPanelState());

        //dragV.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        relPanel.setVisibility(View.VISIBLE);

        //dataUtils.createWebSocketClient();
        prefs = getSharedPreferences(PREFS, 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mapUtils = new MapUtils(this,mMap);
        mMap.setOnMarkerClickListener(this);
        boolean success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        final LatLng montbeliard = new LatLng(47.51, 6.80);

        mapUtils.createWebSocketClient();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPoiClickListener(this);


        mapUtils.getPersons();
        mapUtils.getInstantLocation(1);
        enableMyLocation();
        mapUtils.getPlaces();
        mapUtils.sendInstantLocation();
    }


    public void RDV(View view) {
        findViewById(R.id.dragview).setVisibility(View.VISIBLE);
        mapUtils.getRoutesToNearest(listView);
        sLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mapUtils.getRDVPlace();

        return;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull  Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                        poi.name + "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        if(mapUtils.gotDirections && marker.getTag().toString()!="place") {
            int pid = Integer.parseInt(marker.getTag().toString());
            Person p = Person.getPersonById(pid);
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    p.getInstructions()));

            lblDirections.setText(p.getNom() + "'s Directions");
            for(int i=0;i<Person.Persons.size();i++){
                Person.Persons.get(i).getPolyline().setVisible(false);
            }
            p.getPolyline().setVisible(true);
            return true;

        }
        return false;
    }
}
