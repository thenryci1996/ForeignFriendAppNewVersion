package com.group11.kth.foreignfriend;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.internal.LoginAuthorizationType;
import com.facebook.internal.Utility;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import android.Manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, FusedLocationProviderApi {

    public GoogleMap mMap;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;



    List<String> coursearray = new ArrayList<String>(); //Arrays.asList("ID001", "ID002"));
    List<String> fieldsarray = new ArrayList<String>(); //Arrays.asList("Physics"));

    private GoogleApiClient client;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    HashMap<String, Marker> existingMarkers = new HashMap<String, Marker>();
    HashMap<String, ArrayList<String>> studentsFilters = new HashMap<String, ArrayList<String>>();
    // Update userid to real Facebook user id
    // String userId = "juanluisrto";
    String userId;
    String name_user;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 5;


    DatabaseReference userRef;
    DatabaseReference filtersRef = database.getReference("filters/");
    DatabaseReference locationsRef = database.getReference("location/");
    DatabaseReference rootRef = database.getReference();

    SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* set real user id*/
        mPrefs = this.getSharedPreferences(getString(R.string.user_log_status_file), Context.MODE_PRIVATE);

        userId = mPrefs.getString(getString(R.string.user_id), "null");
        userRef = database.getReference(getString(R.string.Users) + "/" + userId);
        /* get name */
        name_user = mPrefs.getString(getString(R.string.Name), "null");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        client.connect();
        //Saving location locally to use between activities
        mPrefs = this.getSharedPreferences(getString(R.string.user_log_status_file), Context.MODE_PRIVATE);

        //Bottom navigation View
        findViewById(R.id.filter).setOnClickListener((View.OnClickListener) this);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_id:
                        Intent intent;
                        intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        break;

                    case R.id.home_id:
                        Intent intent1;
                        intent1 = new Intent(getApplicationContext(), MapsActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent1);
                }
                return false;
            }
        });

       /* if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);


        //...............................................
        // Axel: added call to style the map
        // Go here to make a new style https://mapstyle.withgoogle.com/
        // Place the file in \app\src\main\res\raw

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.styled_map));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        //.....................................................


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        locationRequest = LocationRequest.create()
                .setInterval(500) // every second
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client,
                        builder.build());




/*
/       Location last_location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(new LatLng(last_location.getLatitude(),last_location.getLongitude()), 16);
        mMap.animateCamera(cam);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(getString(R.string.latitude), String.valueOf(last_location.getLatitude()));
        editor.putString(getString(R.string.longitude), String.valueOf(last_location.getLongitude()));
        editor.commit();
*/


        //Create listeners to the locations in the filters I am subscribed to

        final ChildEventListener filterListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(getApplicationContext(),"get filters calling", Toast.LENGTH_LONG).show();

                boolean match = true; // getFilters(dataSnapshot.getKey());
                if (existingMarkers.containsKey(dataSnapshot.getKey())) {
                    Marker existingMarker = existingMarkers.get(dataSnapshot.getKey());
                    existingMarker.setPosition(parseLatLng(dataSnapshot));
                    existingMarker.setVisible(match);
                    existingMarker.setTag(dataSnapshot.getKey());

                } else {

                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(parseLatLng(dataSnapshot))
                            .title((String) dataSnapshot.child(getString(R.string.Name)).getValue()));
                    //.snippet(dataSnapshot.getValue().toString()));
                    newMarker.setTag(dataSnapshot.getKey());
                    newMarker.setVisible(match);
                    existingMarkers.put(dataSnapshot.getKey(), newMarker);

                }

                //Toast.makeText(MapsActivity.this,dataSnapshot.child("filters").getValue().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                boolean match = true; //getFilters(dataSnapshot.getKey());
                Marker oldM = existingMarkers.get(dataSnapshot.getKey());
                //MarkerOptions replaceMarker = new MarkerOptions().position(parseLatLng(dataSnapshot)).title(dataSnapshot.getKey()).snippet(dataSnapshot.getValue().toString());
                //  Toast.makeText(MapsActivity.this, dataSnapshot.getKey() + dataSnapshot.toString(), Toast.LENGTH_LONG).show();
                oldM.setPosition(parseLatLng(dataSnapshot));
                oldM.setVisible(match);
                //Marker newM = mMap.addMarker(replaceMarker);
                //oldM.remove();
                //existingMarkers.remove(dataSnapshot.getKey());
                //existingMarkers.put(dataSnapshot.getKey(),newM);
                //Toast.makeText(MapsActivity.this,dataSnapshot.child("filters").getValue().toString(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Marker m = existingMarkers.get(dataSnapshot.getKey());
                m.remove();
                existingMarkers.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // takes care of creating and deleting listeners once filters are activated/deactivated
        ChildEventListener my_own_filters_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getRef().getParent().getKey() == "courses") {
                    coursearray.add(dataSnapshot.getKey());
                } else if (dataSnapshot.getRef().getParent().getKey() == "fields") {
                    fieldsarray.add(dataSnapshot.getKey());
                }
                String path = dataSnapshot.getRef().getParent().getKey() + "/" + dataSnapshot.getValue(); //[course-field]/name
                filtersRef.child(path).addChildEventListener(filterListener);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getRef().getParent().getKey() == "courses") {
                    coursearray.remove(dataSnapshot.getKey());
                } else if (dataSnapshot.getRef().getParent().getKey() == "fields") {
                    fieldsarray.remove(dataSnapshot.getKey());
                }
                String path = dataSnapshot.getRef().getParent().getKey() + "/" + dataSnapshot.getValue(); //[course-field]/name
                filtersRef.child(path).removeEventListener(filterListener);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };




        //Downloads and plots the info of all the current users in a course

        ValueEventListener downloadMarkers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(MapsActivity.this, dataSnapshot.toString(), Toast.LENGTH_LONG).show();
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if (existingMarkers.containsKey(child.getKey())) {
                        Log.e("existingMarkers", existingMarkers.toString());
                        Log.e("thisMarkerkey", existingMarkers.get(child.getKey()).toString());
                        Marker existingMarker = existingMarkers.get(child.getKey());
                        existingMarker.setPosition(parseLatLng(child));
                        existingMarker.setTag(child.getKey());
                    } else {
                        Marker newMarker = mMap.addMarker(new MarkerOptions()
                                .position(parseLatLng(child))
                                .title((String) child.child(getString(R.string.Name)).getValue()));
                        newMarker.setTag(child.getKey());
                        //.snippet(child.getValue().toString()));
                        existingMarkers.put(child.getKey(), newMarker);
                        Log.e("newMarker", newMarker.getTitle() + newMarker.getPosition().toString());
                    }
                    //Toast.makeText(MapsActivity.this, child.getKey()+ child.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener downloadFilters = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getRef().getParent().getKey() == "courses") {
                    coursearray.add(dataSnapshot.getKey());
                } else if (dataSnapshot.getRef().getParent().getKey() == "fields") {
                    fieldsarray.add(dataSnapshot.getKey());
                }
                String path = dataSnapshot.getRef().getParent().getKey() + "/" + dataSnapshot.getValue(); //[course-field]/name
                filtersRef.child(path).addChildEventListener(filterListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

      /*  //adds courses and filters from the database to the arrays
        userRef.child("filters/courses").addListenerForSingleValueEvent(downloadFilters);
        userRef.child("filters/fields").addListenerForSingleValueEvent(downloadFilters);

        Log.e("existingMarkers before", existingMarkers.toString());
        //creates a listener to every course and field in which one is subscribed
        for (String course : coursearray) {
            //Toast.makeText(MapsActivity.this, course, Toast.LENGTH_LONG).show();
            filtersRef.child("courses").child(course).addListenerForSingleValueEvent(downloadMarkers); //downloads the locations of all the current users in a course
            filtersRef.child("courses").child(course).addChildEventListener(filterListener);
        }
        for (String field : fieldsarray) {
            filtersRef.child("fields").child(field).addListenerForSingleValueEvent(downloadMarkers); //downloads the locations of all the current users in a field
            filtersRef.child("fields").child(field).addChildEventListener(filterListener);
        }
        userRef.child("filters/courses").addChildEventListener(my_own_filters_listener);
        userRef.child("filters/fields").addChildEventListener(my_own_filters_listener);*/

        locationsRef.addChildEventListener(filterListener);

    }


   public  Boolean  getFilters(String studentId){
       Toast.makeText(getApplicationContext(),"start", Toast.LENGTH_LONG).show();
       Toast.makeText(getApplicationContext(),"id :"+studentId, Toast.LENGTH_LONG).show();
       final boolean[] test = {false};
       final ArrayList<String> studentFilters = new ArrayList<String>();
       rootRef.child("Users/" + studentId +"filters").addListenerForSingleValueEvent( new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               Toast.makeText(getApplicationContext(),"on daata chnage", Toast.LENGTH_LONG).show();
               for (DataSnapshot course : dataSnapshot.child("courses").getChildren()) {
                   studentFilters.add(course.getValue().toString());
               }
               for (DataSnapshot course : dataSnapshot.child("fields").getChildren()) {
                   studentFilters.add(course.getValue().toString());
               }

             //  int c = (int) dataSnapshot.child("test").getValue();
               //if (c == 1)
               //    test[0] = true;
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });
       final ArrayList<String> myFilters = new ArrayList<String>();
       rootRef.child("Users/" + userId +"filters").addListenerForSingleValueEvent( new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               for (DataSnapshot course : dataSnapshot.child("courses").getChildren()) {
                   String s = course.getValue().toString();
                   Toast.makeText(getApplicationContext()," value :"+s, Toast.LENGTH_LONG).show();
                   myFilters.add(s);
               }
               for (DataSnapshot course : dataSnapshot.child("fields").getChildren()) {
                   myFilters.add(course.getValue().toString());
               }

           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });

       int before = myFilters.size();
       myFilters.removeAll(studentFilters);
       int after = myFilters.size();
       boolean match = false;

       if (before!=after){
           match = true;
       }

       return false;
   }



    protected void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

            //   Toast.makeText(this, "Location not working", Toast.LENGTH_LONG).show();
            Log.d("Location :", "Location not working ");
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, locationRequest, this);
    }

    public LatLng parseLatLng(DataSnapshot dataSnapShot) {
        Object o = dataSnapShot.child("latitude").getValue();
        Log.e("type of value", o.getClass().getName());
        double latitude = (double) dataSnapShot.child("latitude").getValue();
        double longitude = (double) dataSnapShot.child("longitude").getValue();
        LatLng latlng = new LatLng(latitude, longitude);
        return latlng;
    }
//riend E/UncaughtException: java.lang.ClassCastException: java.lang.Double cannot be cast to java.lang.Long
    //java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Double


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") //
                //
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {

        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        // mGoogleApiClient.connect(); //location connected
        client.connect();
        //   AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
      //  AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void goToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter:
                Intent i = new Intent(getApplicationContext(), FilterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
        }
    }


    void updateLocation(LatLng latLng) {

        SharedPreferences.Editor editor = mPrefs.edit();


        //Toast.makeText(this, "Update Location Toast", Toast.LENGTH_SHORT).show();
        editor.putString(getString(R.string.latitude), String.valueOf(latLng.latitude));
        editor.putString(getString(R.string.longitude), String.valueOf(latLng.longitude));
        //  Toast.makeText(this, "Location Update", Toast.LENGTH_LONG).show();
        editor.commit();


        userRef.child("location/latitude").setValue(latLng.latitude);
        userRef.child("location/longitude").setValue(latLng.longitude);
        Map<String, Object> locationUpdates = new HashMap<String, Object>();
        locationUpdates.put(userId,latLng);
        locationsRef.updateChildren(locationUpdates);

        Map<String, Object> filterUpdates = new HashMap<String, Object>();
        for (String ftoAdd : fieldsarray) {
            filterUpdates.put("fields/" + ftoAdd + "/" + userId, latLng);
        }
        for (String ctoAdd : coursearray) {
            filterUpdates.put("courses/" + ctoAdd + "/" + userId, latLng);
        }

        filtersRef.updateChildren(filterUpdates); //updates the value of my location in every course adn field I am subscribed to

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        // to be detailed
        Intent intent = new Intent(this, StudentOnlineActivity.class);
        // send userId

        String studentId = (String) marker.getTag();//"10154392139174033";
        Log.d("tag", studentId);
        intent.putExtra(getString(R.string.student_online_id), studentId);
        startActivity(intent);
        return false;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location last_location = LocationServices.FusedLocationApi.getLastLocation(client);
        if (last_location != null) {
            CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(new LatLng(last_location.getLatitude(),last_location.getLongitude()), 16);
            mMap.animateCamera(cam);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(getString(R.string.latitude), String.valueOf(last_location.getLatitude()));
            editor.putString(getString(R.string.longitude), String.valueOf(last_location.getLongitude()));
            editor.commit();

        }

        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      //  Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);
        updateLocation(latLng);
    }

    @Override
    public Location getLastLocation(GoogleApiClient googleApiClient) {
        return null;
    }

    @Override
    public LocationAvailability getLocationAvailability(GoogleApiClient googleApiClient) {
        return null;
    }

    @Override
    public PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationListener locationListener) {
        return null;
    }

    @Override
    public PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationListener locationListener, Looper looper) {
        return null;
    }

    @Override
    public PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationCallback locationCallback, Looper looper) {
        return null;
    }

    @Override
    public PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, PendingIntent pendingIntent) {
        return null;
    }

    @Override
    public PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, LocationListener locationListener) {
        return null;
    }

    @Override
    public PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, PendingIntent pendingIntent) {
        return null;
    }

    @Override
    public PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, LocationCallback locationCallback) {
        return null;
    }

    @Override
    public PendingResult<Status> setMockMode(GoogleApiClient googleApiClient, boolean b) {
        return null;
    }

    @Override
    public PendingResult<Status> setMockLocation(GoogleApiClient googleApiClient, Location location) {
        return null;
    }

    @Override
    public PendingResult<Status> flushLocations(GoogleApiClient googleApiClient) {
        return null;
    }
}