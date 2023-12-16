package com.ciberprotech.finalproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.adapter.CartAdapter;
import com.ciberprotech.finalproject.listner.CartSelectListener;
import com.ciberprotech.finalproject.model.Cart;
import com.ciberprotech.finalproject.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

public class CartFragment extends Fragment implements CartSelectListener {
    private View view;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Cart> products;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private Integer total=0;
    private TextView totalLabel;
    private CartAdapter cartAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        Button checkout = view.findViewById(R.id.checkoutBtn);

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    products = new ArrayList<>();

                    RecyclerView itemView = view.findViewById(R.id.loadCart);
                    TextView empty = view.findViewById(R.id.textView12);

                    cartAdapter = new CartAdapter(products, getActivity().getApplicationContext(), CartFragment.this);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
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

                                        firestore.collection("Users/" + userId + "/cart")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                        if (task.isSuccessful() && task.getResult().size()>0) {

                                                            empty.setText(" ");

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

                                                                                products.add(new Cart(product.getId(),product.getTitle(),product.getCategoryName(),product.getPrice(), pInfo.get(product.getId()),product.getImagePath1()));

                                                                            }

                                                                            cartAdapter.notifyDataSetChanged();
                                                                            UpdateTotal();
                                                                        }
                                                                    });
                                                        }else {
                                                            empty.setText("EMPTY");
//                                                            checkout.setEnabled(false);
//                                                            view.findViewById(R.id.checkoutBtn).setVisibility(View.GONE);
                                                            view.findViewById(R.id.checkoutBtn).setEnabled(false);
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

            checkout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity().getApplicationContext(), CheckoutActivity.class));
                }
            });

        } else {
            startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            getActivity().finish();
        }

        return view;
    }

    @Override
    public void viewProduct(Cart cart) {

    }

    @Override
    public void productAddQty(Cart cart) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String oldQtyS = cart.getQty().toString();
                Integer oldQtyI = Integer.parseInt(oldQtyS);
                Integer newQtyI = oldQtyI + 1;

                firestore.collection("Items").whereEqualTo("id", cart.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            Product item = snapshot.toObject(Product.class);

                            if (item.getId().equals(cart.getId())) {

                                if (newQtyI > Integer.parseInt(item.getQty())) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                                } else {
                                    String newQtyS = String.valueOf(newQtyI);
                                    for (Cart cartitem : products) {
                                        if (cartitem.getId().equals(cart.getId())) {
                                            cartitem.setQty(newQtyS.toString());
                                            break;
                                        }
                                    }

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("id", cart.getId());
                                    data.put("qty", newQtyI);

                                    firestore.collection("Users/" + userId + "/cart").whereEqualTo("id", cart.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                firestore.document("Users/" + userId + "/cart/" + documentSnapshot.getId()).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Update successful
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Update failed
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    });
                                    cartAdapter.notifyDataSetChanged();
                                    UpdateTotal();
                                }

                                break;
                            }

                        }
                    }
                });

            }
        }).start();
    }

    @Override
    public void productRemoveQty(Cart cart) {
        String oldQtyS = cart.getQty().toString();
        Integer oldQtyI = Integer.parseInt(oldQtyS);
        Integer newQtyI = oldQtyI - 1;

        if (newQtyI >= 1) {
            String newQtyS = String.valueOf(newQtyI);
            for (Cart cartitem : products) {
                if (cartitem.getId().equals(cart.getId())) {
                    cartitem.setQty(newQtyS.toString());
                    break;
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", cart.getId());
            data.put("qty", newQtyI);

            firestore.collection("Users/" + userId + "/cart").whereEqualTo("id", cart.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        firestore.document("Users/" + userId + "/cart/" + documentSnapshot.getId()).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Update successful
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Update failed
                            }
                        });
                        break;
                    }
                }
            });
            cartAdapter.notifyDataSetChanged();
            UpdateTotal();
        }
    }

    @Override
    public void removeProduct(Cart cart) {
        firestore.collection("Users/"+userId+"/cart").whereEqualTo("id", cart.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document
                                firestore.collection("Users/"+userId+"/cart").document(document.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                for (Cart cartitem : products) {
                                                    if (cartitem.getId().equals(cart.getId())) {
                                                        products.remove(cartitem);
                                                        cartAdapter.notifyDataSetChanged();
                                                        UpdateTotal();
                                                        break;
                                                    }
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
                            }
                        } else {
                        }
                    }
                });
    }

    public void UpdateTotal(){
        total=0;
        for (Cart cartitem : products) {
            total = total + Integer.valueOf((Integer.valueOf(cartitem.getPrice()) * Integer.valueOf(cartitem.getQty())));
        }
        totalLabel = view.findViewById(R.id.textTotalAmount);
        totalLabel.setText("Rs."+total.toString()+"/=");
    }

}