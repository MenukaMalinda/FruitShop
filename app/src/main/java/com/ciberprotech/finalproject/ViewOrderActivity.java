package com.ciberprotech.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.CartAdapter;
import com.ciberprotech.finalproject.adapter.OrderAdapter;
import com.ciberprotech.finalproject.model.Cart;
import com.ciberprotech.finalproject.model.Order;
import com.ciberprotech.finalproject.model.Product;
import com.ciberprotech.finalproject.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ViewOrderActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Cart> products;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private Integer total=0;
    private TextView totalLabel;
    private OrderAdapter cartAdapter;
    private NotificationManager notificationManager;
    private String channelId = "info";

    private int x =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
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

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

            if (!getIntent().getExtras().getString("mobile").isEmpty() || !getIntent().getExtras().getString("address").isEmpty() || !getIntent().getExtras().getString("latitude ").isEmpty() || !getIntent().getExtras().getString("longitude ").isEmpty()) {

                String mobile = getIntent().getExtras().getString("mobile");
                String address = getIntent().getExtras().getString("address");

                TextView nameView = findViewById(R.id.name);
                TextView addressView = findViewById(R.id.address);
                TextView mobileView = findViewById(R.id.mobile);

                addressView.setText(address);
                mobileView.setText(mobile);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        products = new ArrayList<>();

                        RecyclerView itemView = findViewById(R.id.loadOrder);

                        cartAdapter = new OrderAdapter(products, ViewOrderActivity.this);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewOrderActivity.this);
                        itemView.setLayoutManager(linearLayoutManager);

                        itemView.setAdapter(cartAdapter);


                        firestore.collection("Users")
                                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            userId = documentSnapshot.getId();

                                            nameView.setText(documentSnapshot.toObject(User.class).getFirstName()+" "+documentSnapshot.toObject(User.class).getLastName());

                                            firestore.collection("Users/" + userId + "/cart")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                            if (task.isSuccessful() && task.getResult().size() > 0) {

                                                                CollectionReference collectionReference = firestore.collection("Items");
                                                                HashMap<String, String> pInfo = new HashMap<>();
                                                                int numberOfIds = task.getResult().size();
                                                                int index = 0;

                                                                String[] idsArray = new String[numberOfIds];


                                                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                    String id = snapshot.getData().get("id").toString();
                                                                    pInfo.put(id, snapshot.getData().get("qty").toString());
                                                                    idsArray[index++] = id;
                                                                }

                                                                List<String> idsToMatch = Arrays.asList(idsArray);

                                                                Query query = collectionReference.whereIn("id", idsToMatch);

                                                                query.get()
                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                products.clear();

                                                                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                                    Product product = snapshot.toObject(Product.class);

                                                                                    products.add(new Cart(product.getId(), product.getTitle(), product.getCategoryName(), product.getPrice(), pInfo.get(product.getId()), product.getImagePath1()));

                                                                                }

                                                                                cartAdapter.notifyDataSetChanged();
                                                                                UpdateTotal();

                                                                            }
                                                                        });
                                                            } else {
                                                            }

                                                        }
                                                    });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ViewOrderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }).start();
                //Animation

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
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

                findViewById(R.id.confirmtBtn).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {

                        String ref = String.valueOf(System.currentTimeMillis());

                        String mobile = getIntent().getExtras().getString("mobile");
                        String address = getIntent().getExtras().getString("address");
                        String latitude = getIntent().getExtras().getString("latitude");
                        String longitude = getIntent().getExtras().getString("longitude");

                        LocalDateTime currentDateTime = LocalDateTime.now();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = currentDateTime.format(formatter);

                        Order order = new Order(ref, firebaseAuth.getCurrentUser().getEmail(), total.toString(), mobile, address, latitude, longitude, formattedDateTime, 0);

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


                                                            for (Cart cartitem : products) {

                                                                firestore.collection("Items").whereEqualTo("id", cartitem.getId()).get()
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

                                                                                                        Integer nowQty = Integer.parseInt(addproduct.getQty()) - Integer.parseInt(cartitem.getQty());

                                                                                                        HashMap<String, Object> data = new HashMap<>();
                                                                                                        data.put("qty", String.valueOf(nowQty));

                                                                                                        firestore.document("Items/" + documentSnapshot.getId())
                                                                                                                .update(data)
                                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                                        addproduct.setQty(cartitem.getQty());

                                                                                                                        firestore.collection("orders/" + orderDocId + "/Items")
                                                                                                                                .add(addproduct)
                                                                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                                                    @Override
                                                                                                                                    public void onSuccess(DocumentReference documentReference) {

                                                                                                                                        firestore.collection("Users/" + userId + "/cart/")
                                                                                                                                                .get()
                                                                                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                                                                                                firestore.collection("Users/" + userId + "/cart/").document(document.getId()).delete();
                                                                                                                                                                int count = products.size();
                                                                                                                                                                x++;
                                                                                                                                                                if (count == x) {

                                                                                                                                                                    Intent intent = new Intent(ViewOrderActivity.this,OrderHistoryFragment.class);

                                                                                                                                                                    PendingIntent pendingIntent = PendingIntent
                                                                                                                                                                            .getActivity(ViewOrderActivity.this,0,null,PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);

                                                                                                                                                                    Notification notification = new NotificationCompat.Builder(getApplicationContext(),channelId)
                                                                                                                                                                            .setSmallIcon(R.drawable.ic_stat_name)
                                                                                                                                                                            .setContentTitle("Order Comfirmation")
                                                                                                                                                                            .setContentText("Your Order will be delivered soon.")
                                                                                                                                                                            .setContentIntent(pendingIntent)
                                                                                                                                                                            .build();

                                                                                                                                                                    notificationManager.notify(1,notification);

                                                                                                                                                                    Toast.makeText(getApplicationContext(), "Confirmed Your Order.", Toast.LENGTH_LONG).show();
                                                                                                                                                                    startActivity(new Intent(ViewOrderActivity.this, MainActivity.class));
                                                                                                                                                                    finish();
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                });

                                                                                                                                    }
                                                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                    @Override
                                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

                                                            }


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                                        }
                                                    });

                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

            }
        } else {
            startActivity(new Intent(ViewOrderActivity.this, LoginActivity.class));
            finish();
        }
    }

    public void UpdateTotal(){
        total=0;
        for (Cart cartitem : products) {
            total = total + Integer.valueOf((Integer.valueOf(cartitem.getPrice()) * Integer.valueOf(cartitem.getQty())));
        }
        totalLabel = findViewById(R.id.orderTotal);
        totalLabel.setText("Rs."+total.toString()+"/=");
    }
}