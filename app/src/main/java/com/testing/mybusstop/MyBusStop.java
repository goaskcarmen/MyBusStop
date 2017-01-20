package com.testing.mybusstop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.ResultCallback;


import android.location.Address;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;


import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MyBusStop extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location mLastLocation;

    // receiving loction updates
    //Declare a LocationRequest member variable and a location updated state variable.
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    // REQUEST_CHECK_SETTINGS is used as the request code passed to onActivityResult.
    private static final int REQUEST_CHECK_SETTINGS = 2;

    // Place search
    private static final int PLACE_PICKER_REQUEST = 1;

    private Place destination;

    boolean isNotificActive = false; // track notification

    int notifiID = 33; //track with an id



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bus_stop);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

// Instantiates the mGoogleApiClient field if it’s null.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //location update
        createLocationRequest();

        // place search
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlacePicker();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng seattle = new LatLng(47.607678, -122.334372);
        mMap.addMarker(new MarkerOptions().position(seattle).title("Marker in Seattle"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seattle, 12));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Initiates a background connection of the client to Google Play services.

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Closes the connection to Google Play services if the client is not null and is connected.

        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    //checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }


        ////////////////// GET CURRENT LOCATION //////////////////

        // setMyLocationEnabled enables the my-location layer which draws a light blue dot on the user’s location. It also adds a button to the map that, when tapped, centers the map on the user’s location.
        mMap.setMyLocationEnabled(true);

        // set up map type
        // mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


//        Timer timer = new Timer();
//
//        timer.scheduleAtFixedRate(new TimerTask() {
//
//            synchronized public void run() {
//
//                updateLastLocation();
//            }
//
//        }, 0, 5000);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        updateLastLocation();
                    }
                }, 0, 5, TimeUnit.SECONDS);

    }

    private void updateLastLocation() {
        System.out.println("UpdateLastLocation");

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // getLocationAvailability determines the availability of location data on the device.
        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            // getLastLocation gives you the most recent location currently available
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            System.out.println(String.format("lat: %s, lon: %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()));

            // If you were able to retrieve the the most recent location, then move the camera to the user’s current location.
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude());

                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                if (destination != null) {
                    double distance = getDistance(currentLocation, destination.getLatLng());
                    System.out.println("distance: " + distance);

                    if (distance <= 17000)
                    {
                        showNotification();
                    }
                }
                System.out.println("finished UpdateLastLocation");
            }
        }
    }


    ////////////////// MARKERS //////////////////
    protected void placeMarkerOnMap(LatLng location) {
        // Create a MarkerOptions object and sets the user’s current location as the position for the marker
        MarkerOptions markerOptions = new MarkerOptions().position(location);

        // use custom icon
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource
//                (getResources(), R.mipmap.ic_user_location)));


        // use different color for the default marker
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        // geocoder add address next to the marker as the title
        String titleStr = getAddress(location);
        markerOptions.title(titleStr);

        // Add the marker to the map
        mMap.addMarker(markerOptions);
    }


    ///////////////// GEOCODING ///////////////
    private String getAddress( LatLng latLng ) {
        // Creates a Geocoder object to turn a latitude and longitude coordinate into an address and vice versa.
        Geocoder geocoder = new Geocoder( this );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            // Asks the geocoder to get the address from the location passed to the method.
            addresses = geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 );

            // If the response contains any address, then append it to a string and return.
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressText += (i == 0)?address.getAddressLine(i):("\n" + address.getAddressLine(i));
                }
            }
        } catch (IOException e ) {
        }
        return addressText;
    }


    //////////////// RECEIVING LOCATION UPDATES /////////////////
    protected void startLocationUpdates() {
        //In startLocationUpdates(), if the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        //If there is permission, request for location updates.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);
    }


    // create an instance of LocationRequest, add it to an instance of LocationSettingsRequest.Builder and retrieve and handle any changes to be made based on the current state of the user’s location settings.
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // setInterval() specifies the rate at which your app will like to receive updates. Every 10 sec
        mLocationRequest.setInterval(10000);
        // setFastestInterval() specifies the fastest rate at which the app can handle updates. Setting the fastestInterval rate places a limit on how fast updates will be sent to your app.
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locationRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        locationRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    // A SUCCESS status means all is well and you can go ahead and initiate a location request.

                    case LocationSettingsStatusCodes.SUCCESS:
                        mLocationUpdateState = true;
                        startLocationUpdates();
                        break;
                    // A RESOLUTION_REQUIRED status means the location settings have some issues which can be fixed. This could be as a result of the user’s location settings turned off.
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MyBusStop.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    // A SETTINGS_CHANGE_UNAVAILABLE status means the location settings have some issues that you can’t fix. This could be as a result of the user choosing NEVER on the dialog above.
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }


    // Override FragmentActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationUpdateState = true;
                startLocationUpdates();
            }
        }

        // place search
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(MyBusStop.this, data);

//                String addressText = place.getName().toString();
//                addressText += "\n" + place.getAddress().toString();

                destination = place;
                placeMarkerOnMap(place.getLatLng());
            }
        }
    }

    // Override onPause() to stop location update request
    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    // Override onResume() to restart the location update request.
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();

        //location update
        if (mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //place marker on updated location
//        mLastLocation = location;
//        if (null != mLastLocation) {
//            placeMarkerOnMap(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
//        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    /////////////////// PLACE SEARCH ////////////
    private void loadPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            Intent intent = builder.build(MyBusStop.this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch(GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /////////////// MAP TYPE /////////////////
    //function for the change view (satellite vs. street view) button
    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    ///////////// DISTANCE BETWEEN TWO POINTS////////////
    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        return distance;
    }

    ////////////// GET LATTITUDE AND LONGITUDE FROM AN ADDRESS ///////////
    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }



    public void showNotification() {
        //build notification
        NotificationCompat.Builder notificBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("Get Off Soon")
                .setTicker("Alert New Message")
                .setSmallIcon(R.drawable.ic_stat_name);
//                .setVibrate(long[] pattern);

        // define the intention of opening more information notification in another page
        Intent moreInfoIntent = new Intent(this, MyBusStop.class);

        // when the user clicks on back it will go back to the proper place
        TaskStackBuilder tStackBuilder = TaskStackBuilder.create(this);

        tStackBuilder.addParentStack(MyBusStop.class);

        tStackBuilder.addNextIntent(moreInfoIntent);

        // define an intent and an action to perform with that intent by another application
        PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT); //if this intent exist just update it not create a new one

        //in the taskbar the intent pops up with a little message
        //if the taskbar is dragged and opened
        //show intent when the notification is actually clicked on
        notificBuilder.setContentIntent(pendingIntent);

        //notify of the background event
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifiID, notificBuilder.build());

        isNotificActive = true;


    }
}





