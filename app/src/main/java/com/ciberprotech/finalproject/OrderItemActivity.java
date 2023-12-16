package com.ciberprotech.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.OrderAdapter;
import com.ciberprotech.finalproject.adapter.OrderItemsAdapter;
import com.ciberprotech.finalproject.model.Order;
import com.ciberprotech.finalproject.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrderItemActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Product> items;
    private String userId;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        String orderid = getIntent().getExtras().getString("orderId");

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            if (firebaseAuth.getCurrentUser().isEmailVerified()){

            items = new ArrayList<>();
            RecyclerView categoryView = findViewById(R.id.loadProducts);
            OrderItemsAdapter ordersAdapter = new OrderItemsAdapter(items, this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            categoryView.setLayoutManager(linearLayoutManager);
            categoryView.setAdapter(ordersAdapter);

            firestore.collection("orders").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot snapshot1 : task.getResult()) {
                                    Order order = snapshot1.toObject(Order.class);

                                    if (order.getId().equals(orderid)) {

                                        orderId = snapshot1.getId();

                                        firestore.collection("orders/" + orderId + "/Items").get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot snapshot2 : task.getResult()) {
                                                                Product item = snapshot2.toObject(Product.class);
                                                                items.add(item);
                                                            }
                                                            ordersAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                        break;
                                    }
                                }

                            } else {

                            }

                        }
                    });

            } else {
                Toast.makeText(getApplicationContext(),"Please Verify email account.",Toast.LENGTH_SHORT).show();
                firebaseAuth.getCurrentUser().isEmailVerified();
            }
        }else{
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

    }
}




