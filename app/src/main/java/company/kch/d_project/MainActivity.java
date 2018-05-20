package company.kch.d_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import company.kch.d_project.adapter.MessageAdapter;
import company.kch.d_project.model.ChatModel;

public class MainActivity extends AppCompatActivity {

    public static final String USER_NAME = "userName";
    public static final String LATITUD = "Latitud";
    public static final String LONGITUDE = "Longitude";

    String userName = null;

    Button buttonStart;

    //location
    private LocationManager locationManager;
    private Location locationNow;
    TextView textGeoPosition;
    ProgressBar progressBar;
    ImageView imageView;

    //Google Registration in Firebase
    SignInButton signInButton;
    Button signOutButton;
    GoogleSignInClient mGoogleSignInClient;
    EditText editTextGoogleName;
    DatabaseReference reference;

    Date currentTime;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        textGeoPosition = findViewById(R.id.textGeoPosition);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageReady);

        //Google SignIn
        mAuth = FirebaseAuth.getInstance();
        signInButton = findViewById(R.id.signInButton);
        signOutButton = findViewById(R.id.signOutButton);
        editTextGoogleName = findViewById(R.id.editText);

        reference = FirebaseDatabase.getInstance().getReference("chats");

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    signInButton.setVisibility(View.INVISIBLE);
                    signOutButton.setVisibility(View.VISIBLE);
                    editTextGoogleName.setVisibility(View.VISIBLE);
                    myRef.child(mAuth.getUid()).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue(String.class) == null) {
                                myRef.child(mAuth.getUid()).child("name").setValue(mAuth.getCurrentUser().getDisplayName());
                            }
                            editTextGoogleName.setHint(dataSnapshot.getValue(String.class));
                            userName = dataSnapshot.getValue(String.class);
                            if (locationNow != null) {
                                buttonStart.setEnabled(true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
        Intent intent1 = new Intent(MainActivity.this, MessageAdapter.class);
        intent1.putExtra("test", userName);
        editTextGoogleName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (editTextGoogleName.getText().length() < 4) {
                        showToast("Слишком короткое имя");
                    } else {
                        myRef.child(mAuth.getUid()).child("name").setValue(editTextGoogleName.getText().toString());
                        editTextGoogleName.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(USER_NAME, userName);
                intent.putExtra(LATITUD, locationNow.getLatitude());
                intent.putExtra(LONGITUDE, locationNow.getLongitude());
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException ignored) {

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
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

    public void onClickSignOutButton (View view) {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.INVISIBLE);
        userName = null;
        editTextGoogleName.setVisibility(View.INVISIBLE);
        buttonStart.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER) || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            textGeoPosition.setText(formatLocation(location));
            locationNow = new Location(location);
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            if (mAuth.getUid() != null) {
                buttonStart.setEnabled(true);
            }
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return location.getLatitude() + " : " + location.getLongitude();
    }

    public void onClickMap (View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    public void onCreateChatClick (View view) {
        currentTime = Calendar.getInstance().getTime();
        reference.push().setValue(new ChatModel(userName, "test", currentTime, locationNow.getLatitude(), locationNow.getLongitude()));
        System.out.println(reference.getKey());
    }
}
