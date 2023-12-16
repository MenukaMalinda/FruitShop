package com.ciberprotech.finalproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.ProductAdapter;
import com.ciberprotech.finalproject.listner.ProductSelectListener;
import com.ciberprotech.finalproject.model.Product;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishlistFragment extends Fragment implements ProductSelectListener {
    View view;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Product> products;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private ProductAdapter productAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    products = new ArrayList<>();

                    RecyclerView itemView = view.findViewById(R.id.loadWishlist);
                    TextView empty = view.findViewById(R.id.empty);

                    productAdapter = new ProductAdapter(products, getActivity().getApplicationContext(), WishlistFragment.this);

                    GridLayoutManager layoutManager1 = new GridLayoutManager(getActivity().getApplicationContext(), 2);
                    itemView.setLayoutManager(layoutManager1);

                    itemView.setAdapter(productAdapter);

                    firestore.collection("Users")
                            .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        userId = documentSnapshot.getId();

                                        firestore.collection("Users/" + userId + "/wishlist")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                        if (task.isSuccessful() && task.getResult().size() > 0) {

                                                            empty.setText(" ");
                                                            CollectionReference collectionReference = firestore.collection("Items");
                                                            HashMap<String, String> pInfo = new HashMap<>();
                                                            int numberOfIds = task.getResult().size();
                                                            int index = 0;

                                                            String[] idsArray = new String[numberOfIds];


                                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                String id = snapshot.getData().get("id").toString();
//                                                                pInfo.put(id, snapshot.getData().get("id").toString());
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

                                                                                products.add(new Product(product.getId(), product.getTitle(), product.getCategoryName(), product.getPrice(), product.getQty(), product.getImagePath1()));

                                                                            }

                                                                            productAdapter.notifyDataSetChanged();
                                                                        }
                                                                    });
                                                        } else {
                                                            empty.setText("EMPTY");

                                                        }

                                                    }
                                                });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }).start();
        } else {
            startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            getActivity().finish();
        }

        return view;
    }

    @Override
    public void viewSingleProduct(Product product) {
        Intent intent = new Intent(getActivity().getApplicationContext(), SingleProductActivity.class);
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
                                                                                                Toast.makeText(getActivity().getApplicationContext(), "Removed from Wishlist", Toast.LENGTH_LONG).show();
                                                                                            } else {
                                                                                                // Handle the error
                                                                                                Toast.makeText(getActivity().getApplicationContext(), "Failed to remove from Wishlist", Toast.LENGTH_LONG).show();
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
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Added to Wishlist", Toast.LENGTH_LONG).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                                                }
                                                            });

                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
        }


    }
}