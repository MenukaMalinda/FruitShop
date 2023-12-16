package com.ciberprotech.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.model.Cart;
import com.ciberprotech.finalproject.model.Order;
import com.ciberprotech.finalproject.model.Product;
import com.ciberprotech.finalproject.model.User;
import com.ciberprotech.finalproject.util.Format;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BuyNowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker_current, marker_pin;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private LatLng dLocation;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String ProductId;
    private Product product;
    private NotificationManager notificationManager;
    private String channelId = "info";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_now);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "INFO", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription("This is Information");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }


        TextView titleView = findViewById(R.id.textName);
        TextView priceView = findViewById(R.id.buyPrice);
        TextView categoryView = findViewById(R.id.textCategory1);
        TextView qtyView = findViewById(R.id.buyQty);
        TextView amountTotal = findViewById(R.id.buyTotal);

        EditText mobileView = findViewById(R.id.editTextCheckoutMobile);
        EditText address1View = findViewById(R.id.editTextAdress1);
        EditText address2View = findViewById(R.id.editTextAdress2);
        EditText cityView = findViewById(R.id.editTextCity);

        if (!getIntent().getExtras().getString("productID").isEmpty()) {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

                    findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                    //Load Product Details
                    ProductId = getIntent().getExtras().getString("productID");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            firestore.collection("Items").whereEqualTo("id", ProductId).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                product = snapshot.toObject(Product.class);

                                                if (product.getId().equals(ProductId)) {

                                                    if (Integer.parseInt(product.getQty()) <= 0) {
                                                        finish();
                                                    }

                                                    titleView.setText(product.getTitle());
                                                    priceView.setText("Rs."+ new Format(String.valueOf(product.getPrice())).formatPrice().toString());
                                                    categoryView.setText(product.getCategoryName());
                                                    qtyView.setText("1");
                                                    amountTotal.setText("Rs. "+ new Format(String.valueOf(product.getPrice())).formatPrice().toString());

                                                    break;
                                                }

                                            }

                                        }
                                    });

                        }
                    }).start();

                    findViewById(R.id.buyPlusBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String oldQtyS = qtyView.getText().toString();
                            Integer oldQtyI = Integer.parseInt(oldQtyS);
                            Integer newQtyI = oldQtyI + 1;

                            if (newQtyI > Integer.parseInt(product.getQty())) {
                                Toast.makeText(getApplicationContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                            } else {
                                qtyView.setText(newQtyI.toString());
                                Integer totalprice = newQtyI * Integer.parseInt(product.getPrice());
                                amountTotal.setText("Rs. "+ new Format(String.valueOf(totalprice)).formatPrice().toString());
                            }

                        }
                    });

                    findViewById(R.id.buyMinusBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String oldQtyS = qtyView.getText().toString();
                            Integer oldQtyI = Integer.parseInt(oldQtyS);
                            Integer newQtyI = oldQtyI - 1;

                            if (newQtyI <= 0) {
                                qtyView.setText("1");
                                amountTotal.setText("Rs. "+ new Format(String.valueOf(product.getPrice())).formatPrice().toString());
                            }else{
                                qtyView.setText(newQtyI.toString());
                                Integer totalprice = newQtyI * Integer.parseInt(product.getPrice());
//                                amountTotal.setText("Rs. "totalprice.toString());
                                amountTotal.setText("Rs. "+ new Format(String.valueOf(totalprice.toString())).formatPrice().toString());

                            }

                        }
                    });

                    //Load User Details
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            firestore.collection("Users").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        user = documentSnapshot.toObject(User.class);



                                        if (user.getMobile() != null) {
                                            mobileView.setText(user.getMobile());
                                        }
                                        if (user.getAddress1() != null) {
                                            address1View.setText(user.getAddress1());
                                        }
                                        if (user.getAddress2() != null) {
                                            address2View.setText(user.getAddress2());
                                        }
                                        if (user.getCity() != null) {
                                            cityView.setText(user.getCity());
                                        }

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(400);
                                                } catch (InterruptedException e) {
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        findViewById(R.id.animloader).setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }).start();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

                } else {
                    Toast.makeText(getApplicationContext(), "Please verify your Email", Toast.LENGTH_SHORT).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                }
            } else {
               Toast.makeText(getApplicationContext(),"Please Sign In",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        }else {
            finish();
        }

        findViewById(R.id.confirmBtn1).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                String address1 = address1View.getText().toString().replace(',', ' ');
                String address2 = address2View.getText().toString().replace(',', ' ');
                String city = cityView.getText().toString().replace(',', ' ');
                String mobile = mobileView.getText().toString();

                if (address1.isEmpty()) {
                    Toast.makeText(BuyNowActivity.this, "Please enter Address line1", Toast.LENGTH_SHORT).show();
                } else if (address2.isEmpty()) {
                    Toast.makeText(BuyNowActivity.this, "Please enter Address line2", Toast.LENGTH_SHORT).show();
                } else if (city.isEmpty()) {
                    Toast.makeText(BuyNowActivity.this, "Please enter City", Toast.LENGTH_SHORT).show();
                } else if (mobile.isEmpty()) {
                    Toast.makeText(BuyNowActivity.this, "Please enter Mobile", Toast.LENGTH_SHORT).show();
                } else {

                    if (dLocation != null && marker_current != null) {

                        String ref = String.valueOf(System.currentTimeMillis());

                        LocalDateTime currentDateTime = LocalDateTime.now();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = currentDateTime.format(formatter);

                        Integer total = (Integer.parseInt(qtyView.getText().toString()) * Integer.parseInt(product.getPrice().toString()));
                        String address = address1 + ", " + address2 + ", " + city;

                        Order order = new Order(ref, firebaseAuth.getCurrentUser().getEmail(), total.toString(), mobile, address, String.valueOf(dLocation.latitude), String.valueOf(dLocation.longitude), formattedDateTime, 0);

                        firestore.collection("Users")
                                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String userId = documentSnapshot.getId();

                                            firestore.collection("orders")
                                                    .add(order)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {

                                                            String orderDocId = documentReference.getId();

                                                            String itemQty = qtyView.getText().toString();

                                                            firestore.collection("Items").whereEqualTo("id", product.getId()).get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                                Product addproduct = snapshot.toObject(Product.class);

                                                                                firestore.collection("Items/")
                                                                                        .whereEqualTo("id", addproduct.getId())
                                                                                        .get()
                                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                                                                                    Integer nowQty = Integer.parseInt(addproduct.getQty()) - Integer.parseInt(itemQty);

                                                                                                    HashMap<String, Object> data = new HashMap<>();
                                                                                                    data.put("qty", String.valueOf(nowQty));

                                                                                                    firestore.document("Items/" + documentSnapshot.getId())
                                                                                                            .update(data)
                                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void aVoid) {

                                                                                                                    addproduct.setQty(itemQty);


                                                                                                                    firestore.collection("orders/" + orderDocId + "/Items")
                                                                                                                            .add(addproduct)
                                                                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                                                @Override
                                                                                                                                public void onSuccess(DocumentReference documentReference) {

                                                                                                                                    Intent intent = new Intent(BuyNowActivity.this,OrderHistoryFragment.class);

                                                                                                                                    PendingIntent pendingIntent = PendingIntent
                                                                                                                                            .getActivity(BuyNowActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);

                                                                                                                                    Notification notification = new NotificationCompat.Builder(getApplicationContext(),channelId)
                                                                                                                                            .setSmallIcon(R.drawable.ic_stat_name)
                                                                                                                                            .setContentTitle("Order Comfirmation")
                                                                                                                                            .setContentText("Your Order will be delivered soon.")
                                                                                                                                            .setContentIntent(pendingIntent)
                                                                                                                                            .build();

                                                                                                                                    notificationManager.notify(1,notification);

                                                                                                                                    Toast.makeText(getApplicationContext(), "Confirmed Your Order.", Toast.LENGTH_SHORT).show();
                                                                                                                                    startActivity(new Intent(BuyNowActivity.this, MainActivity.class));
                                                                                                                                    finish();

                                                                                                                                }
                                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                @Override
                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            })
                                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                                @Override
                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                }
                                                                                                            });

                                                                                                }
                                                                                            }
                                                                                        });


                                                                            }
                                                                        }
                                                                    });

//                                                            }


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                                        }
                                                    });


                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    } else {
                        Toast.makeText(BuyNowActivity.this, "Please select your deliver location", Toast.LENGTH_SHORT).show();

                    }

                }


            }
        });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (latLng != null && marker_current != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    dLocation = latLng;
                    marker_current.setPosition(latLng);
                }
            }
        });

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if (checkPermissions()) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
                return false;
            }
        });

        if (checkPermissions()) {
            map.setMyLocationEnabled(true);
            getLastLocation();
        } else {
            requestPermissions(
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        dLocation = latLng;
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }
            });
            //Current location live update
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(true)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdateDelayMillis(1000)
                    .build();

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            currentLocation = locationResult.getLastLocation();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            dLocation = latLng;

            if (marker_current == null) {
                MarkerOptions options = new MarkerOptions()
                        .title("My Location")
                        .position(latLng);
                marker_current = map.addMarker(options);
            } else {
                marker_current.setPosition(latLng);
            }

        }
    };

    private boolean checkPermissions() {
        boolean permission = false;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            permission = true;
        }
        return permission;
    }
}