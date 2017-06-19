package com.example.deepu.mapnearbylocationfind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback, LocationListener {


    GoogleMap googleMap;
    Location currentLocation;
    LatLng latLng;
    String locationProvider;
    String item;
    Button searchButton;
    Spinner typeSpinner;
    RequestQueue requestQueue;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchButton = (Button) findViewById(R.id.button_search);
        typeSpinner = (Spinner) findViewById(R.id.spinner_type);


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        List<String> type = new ArrayList<>();
        type.add("ATM");
        type.add("bank");
        type.add("restaurant");
        type.add("school");
        type.add("hospital");
        typeSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, type);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        typeSpinner.setAdapter(dataAdapter);


        requestQueue = Volley.newRequestQueue(this, null);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.clear();
                getNearLocation();
            }
        });
    }


    void getNearLocation() {
        String s = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latLng.latitude + "," + latLng.longitude + "&radius=1000&type=" + item + "&key=" + "AIzaSyDJFWAaQi0luSrCixbCycV6qvIy33ANg9U";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, s, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
Log.e("response","response"+response);
                    JSONArray jsonArray = response.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Log.e("get near by location", "beore parsing");
                        jsonObject = new JSONObject();
                        jsonObject = jsonArray.getJSONObject(i);
                        jsonObject = jsonObject.getJSONObject("geometry");
                        jsonObject = jsonObject.getJSONObject("location");
                        double lat = jsonObject.optDouble("lat");
                        double lng = jsonObject.optDouble("lng");
                        LatLng mLatLng = new LatLng(lat, lng);
                        Log.e("get near by location", "after parsing");
                        googleMap.addMarker(new MarkerOptions().position(mLatLng));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.e("get near by location", "JSONException e");

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("get near by location", "onErrorRespons");

            }
        });

        requestQueue.add(jsonObjectRequest);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        item = adapterView.getItemAtPosition(i).toString();
        Log.e("type", " :" + item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        currentLocation = getLocation();

        if (currentLocation == null) {
            Log.e("onMapReady", "currentLocation=null");
        }
        latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        CameraPosition googleplex = CameraPosition.builder()
                .target(latLng)
                .zoom(16)
                .bearing(0)
                .tilt(0)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(googleplex));
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }


    Location getLocation() {
Log.e("getlocation ","call");
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        currentLocation = locationManager.getLastKnownLocation(locationProvider);
        locationManager.requestLocationUpdates(locationProvider, 15000, 1, this);
        return currentLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}


