package company.kch.d_project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import company.kch.d_project.adapter.CustomInfoWindowAdapter;
import company.kch.d_project.model.ChatModel;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker myMarker;
    DatabaseReference reference;
    List<ChatModel> markerList;
    List<String> markerKeyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerList = new ArrayList<>();
        markerKeyList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);

                assert chatModel != null;
                if ((Calendar.getInstance().getTimeInMillis() - chatModel.dateTime.getTime()) > 172800000) {
                    reference.child(dataSnapshot.getKey()).removeValue();
                } else {
                    markerList.add(chatModel);
                    markerKeyList.add(dataSnapshot.getKey());
                    LatLng m = new LatLng(chatModel.latitude, chatModel.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions
                            .position(m)
                            .title("Чат \"" + chatModel.chatName + '\"')
                            .snippet("Нажмите, чтобы подключиться");
                    mMap.addMarker(markerOptions);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(getIntent().getDoubleExtra("Lat", 0), getIntent().getDoubleExtra("Long", 0)))
                .zoom(15)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(getIntent().getDoubleExtra("Lat", 0), getIntent().getDoubleExtra("Long", 0)))
                .radius(1000); // In meters

        // Get back the mutable Circle
        mMap.addCircle(circleOptions);

        mMap.moveCamera(update);


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng myPosition = new LatLng(getIntent().getDoubleExtra("Lat", 0), getIntent().getDoubleExtra("Long", 0));
                float[] results = new float[1];
                Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude, myPosition.latitude, myPosition.longitude, results);

                if (results[0] > 1000) {
                    showToast("Чат вне зоны доступа");
                } else {
                    Intent intent = new Intent(MapsActivity.this, ChatActivity.class);
                    for (int i = 0; i < markerList.size(); i++) {
                        if (marker.getPosition().latitude == markerList.get(i).latitude && marker.getPosition().longitude == markerList.get(i).longitude) {
                            intent.putExtra("chatID", markerKeyList.get(i));
                        }
                    }
                    intent.putExtra("UserName", getIntent().getStringExtra("UserName"));
                    startActivity(intent);
                }
            }
        });



    }

    //Текстовое уведомление
    public void showToast(String message) {
        //создаем и отображаем текстовое уведомление
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.TOP, 0, 80);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
