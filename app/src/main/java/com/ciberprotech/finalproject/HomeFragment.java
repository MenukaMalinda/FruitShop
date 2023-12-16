package com.ciberprotech.finalproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.CategoryAdapter;
import com.ciberprotech.finalproject.adapter.ProductAdapter;
import com.ciberprotech.finalproject.listner.CategorySelectListener;
import com.ciberprotech.finalproject.listner.ProductSelectListener;
import com.ciberprotech.finalproject.model.Category;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements ProductSelectListener, CategorySelectListener {

    View view;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Product> products;
    private ArrayList<Category> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ImageSlider imageSlider = view.findViewById(R.id.image_slider);

        ArrayList<SlideModel> slideModels = new ArrayList<>(); // Create image list

        slideModels.add(new SlideModel(R.drawable.banner3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner4, ScaleTypes.FIT));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        //Load Categories
        categories = new ArrayList<>();
        RecyclerView categoryView = view.findViewById(R.id.loadCategory);

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, getActivity().getApplicationContext(),this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        categoryView.setLayoutManager(layoutManager);

        categoryView.setAdapter(categoryAdapter);

        firestore.collection("Categories").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                categories.add(new Category("All Categories", null));
                for (DocumentChange change : value.getDocumentChanges()) {
                    Category category = change.getDocument().toObject(Category.class);
                    switch (change.getType()) {
                        case ADDED:
                            categories.add(category);
                        case MODIFIED:
                            Product old = products.stream().filter(i -> i.getTitle().equals(category.getCategoryName())).findFirst().orElse(null);
                            if (old != null) {
                                old.setCategoryName(category.getCategoryName());
                            }
                            break;
                        case REMOVED:
                            products.remove(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });

        //Load Products
        products = new ArrayList<>();
        RecyclerView productView = view.findViewById(R.id.loadProduct);


        ProductAdapter productAdapter = new ProductAdapter(products, getActivity().getApplicationContext(), this);

        GridLayoutManager layoutManager1 = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        productView.setLayoutManager(layoutManager1);

        productView.setAdapter(productAdapter);

        firestore.collection("Items").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Product product = change.getDocument().toObject(Product.class);
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
                productAdapter.notifyDataSetChanged();
            }
        });

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
//                                                                                                        setBackgroundColor(Color.BLACK);
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
//                                                                            wishlistBtn.setBackgroundColor(Color.WHITE);
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

    @Override
    public void selectProduct(Category category) {
        products = new ArrayList<>();
        RecyclerView productView = view.findViewById(R.id.loadProduct);

        ProductAdapter productAdapter = new ProductAdapter(products, getActivity().getApplicationContext(), this);

        GridLayoutManager layoutManager1 = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        productView.setLayoutManager(layoutManager1);

        productView.setAdapter(productAdapter);

        Query query;
        if (category.getCategoryName().equals("All Categories")) {
            query = firestore.collection("Items").whereNotEqualTo("categoryName", null);
        } else {
            query = firestore.collection("Items").whereEqualTo("categoryName", category.getCategoryName());
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Product product = change.getDocument().toObject(Product.class);
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
                productAdapter.notifyDataSetChanged();
            }
        });
    }
}