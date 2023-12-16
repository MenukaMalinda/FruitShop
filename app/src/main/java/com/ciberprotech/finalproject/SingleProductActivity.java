package com.ciberprotech.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.ProductAdapter;
import com.ciberprotech.finalproject.listner.ProductSelectListener;
import com.ciberprotech.finalproject.model.Product;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleProductActivity extends AppCompatActivity implements ProductSelectListener {

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Product> products;
    private Product product;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (!getIntent().getExtras().getString("itemId").isEmpty()) {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!getIntent().getExtras().getString("itemId").isEmpty()) {

                                String id = getIntent().getExtras().getString("itemId");

                                firestore.collection("Items").get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                    product = snapshot.toObject(Product.class);

                                                    if (product.getId().equals(id)) {

                                                        TextView title = findViewById(R.id.txtProductName);
                                                        TextView price = findViewById(R.id.txtPrice);
                                                        TextView qty = findViewById(R.id.txtQty);
                                                        TextView desc = findViewById(R.id.txtDescripton);

                                                        if (Integer.parseInt(product.getQty()) <= 0) {
                                                            findViewById(R.id.buynowBtn).setVisibility(View.GONE);
                                                            findViewById(R.id.addToCartBtn).setVisibility(View.GONE);
                                                        }


                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ImageSlider imageSlider = findViewById(R.id.product_slider);
                                                                ArrayList<SlideModel> slideModels = new ArrayList<>(); // Create image list

                                                                slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/fruitshop-144f0.appspot.com/o/item_img%2F" + product.getImagePath1() + "?alt=media", ScaleTypes.CENTER_INSIDE));
                                                                slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/fruitshop-144f0.appspot.com/o/item_img%2F" + product.getImagePath2() + "?alt=media", ScaleTypes.CENTER_INSIDE));
                                                                slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/fruitshop-144f0.appspot.com/o/item_img%2F" + product.getImagePath3() + "?alt=media", ScaleTypes.CENTER_INSIDE));

                                                                imageSlider.setImageList(slideModels, ScaleTypes.FIT);

                                                                title.setText(product.getTitle().toString());
                                                                price.setText("RS. " + product.getPrice().toString() + ".00");
                                                                qty.setText(product.getQty().toString());
                                                                desc.setText(product.getDescription().toString());

                                                                category = product.getCategoryName().toString();

                                                            }
                                                        });
                                                        break;
                                                    }
                                                }
                                            }
                                        });

                                //Load Category Products
                                products = new ArrayList<>();
                                RecyclerView productView = findViewById(R.id.productCategoryView);

                                ProductAdapter productAdapter = new ProductAdapter(products, SingleProductActivity.this, SingleProductActivity.this);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(SingleProductActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                productView.setLayoutManager(layoutManager);

                                productView.setAdapter(productAdapter);

                                firestore.collection("Items").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                        for (DocumentChange change : value.getDocumentChanges()) {
                                            Product product = change.getDocument().toObject(Product.class);
                                            if (product.getCategoryName().equals(category) && !product.getId().equals(id)) {
                                                switch (change.getType()) {
                                                    case ADDED:
                                                        products.add(product);
                                                        break;
                                                    case MODIFIED:
                                                        Product old = products.stream().filter(i -> i.getTitle().equals(product.getTitle())).findFirst().orElse(null);
                                                        if (old != null) {
                                                            old.setTitle(product.getTitle());
                                                            old.setCategoryName(product.getCategoryName());
                                                            old.setPrice(product.getPrice());
                                                            old.setQty(product.getQty());
                                                            old.setImagePath1(product.getImagePath1());
                                                        }
                                                        break;
                                                    case REMOVED:
                                                        products.remove(product);
                                                        break;
                                                }
                                            }

                                        }
                                        productAdapter.notifyDataSetChanged();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(2000);
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
                                });

                            }
                        }
                    }).start();

                } else {
                    Toast.makeText(getApplicationContext(), "Please verify your Email", Toast.LENGTH_SHORT).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        } else {
            finish();
        }

        findViewById(R.id.buynowBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(firebaseAuth.getCurrentUser() !=null){
                    if (firebaseAuth.getCurrentUser().isEmailVerified()){
                        startActivity(new Intent(SingleProductActivity.this, BuyNowActivity.class)
                                .putExtra("productID", product.getId())
                        );
                    }else {
                        Toast.makeText(getApplicationContext(),"Please Login",Toast.LENGTH_SHORT).show();

                    }
                }else {

                }

            }
        });

        findViewById(R.id.addToCartBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

                    firestore.collection("Users")
                            .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        String userId = documentSnapshot.getId();

                                        firestore.collection("Users/" + userId + "/cart")
                                                .whereEqualTo("id", product.getId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (queryDocumentSnapshots.size() > 0) {
                                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                String productDoc = documentSnapshot.getId();

                                                                firestore.collection("Users/" + userId + "/cart")
                                                                        .whereEqualTo("id", product.getId())
                                                                        .get()
                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                                                                    String qty = snapshot.getData().get("qty").toString();

                                                                                    if ((Integer.parseInt(qty) + 1) <= Integer.parseInt(product.getQty())) {

                                                                                        Map<String, Object> data = new HashMap<>();
                                                                                        data.put("id", product.getId());
                                                                                        data.put("qty", Integer.parseInt(qty) + 1);

                                                                                        firestore.document("Users/" + userId + "/cart/" + productDoc)
                                                                                                .update(data)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        // Update successful
                                                                                                        Toast.makeText(getApplicationContext(), "Already product have in your cart and increased qty.", Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        // Update failed
                                                                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                                                                                    }
                                                                                                });

                                                                                    } else {
                                                                                        Toast.makeText(getApplicationContext(), "Not enought stock", Toast.LENGTH_LONG).show();
                                                                                    }

                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        } else {

                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("id", product.getId());
                                                            data.put("qty", 1);

                                                            firestore.collection("Users")
                                                                    .document(userId)
                                                                    .collection("cart")
                                                                    .add(data)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            Toast.makeText(getApplicationContext(), "Added to Cart", Toast.LENGTH_LONG).show();
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

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        });

    }

    @Override
    public void viewSingleProduct(Product product) {
        Intent intent = new Intent(SingleProductActivity.this, SingleProductActivity.class);
        intent.putExtra("itemId", product.getId());
        startActivity(intent);
    }

    @Override
    public void addWishlistProduct(Product product) {

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

            firestore.collection("Users")
                    .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                String userId = documentSnapshot.getId();

                                firestore.collection("Users/" + userId + "/wishlist")
                                        .whereEqualTo("id", product.getId())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                if (queryDocumentSnapshots.size() > 0) {
                                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                        String productDoc = documentSnapshot.getId();

                                                        firestore.collection("Users/" + userId + "/wishlist")
                                                                .whereEqualTo("id", product.getId())
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                                                            // delete product in whishlist
                                                                            firestore.collection("Users/" + userId + "/wishlist")
                                                                                    .document(productDoc)
                                                                                    .delete()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                // Product successfully removed from wishlist
                                                                                                Toast.makeText(SingleProductActivity.this, "Removed from Wishlist", Toast.LENGTH_LONG).show();
                                                                                            } else {
                                                                                                // Handle the error
                                                                                                Toast.makeText(SingleProductActivity.this, "Failed to remove from Wishlist", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                } else {

                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("id", product.getId());

                                                    firestore.collection("Users")
                                                            .document(userId)
                                                            .collection("wishlist")
                                                            .add(data)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Toast.makeText(SingleProductActivity.this, "Added to Wishlist", Toast.LENGTH_LONG).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(SingleProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                                                                }
                                                            });

                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SingleProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SingleProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            startActivity(new Intent(SingleProductActivity.this, LoginActivity.class));
        }
    }

}