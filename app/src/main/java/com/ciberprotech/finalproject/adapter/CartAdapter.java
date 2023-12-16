package com.ciberprotech.finalproject.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ciberprotech.finalproject.R;
import com.ciberprotech.finalproject.listner.CartSelectListener;
import com.ciberprotech.finalproject.model.Cart;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{
    private ArrayList<Cart> products;
    private Context context;
    private FirebaseStorage storage;
    private CartSelectListener selectListener;

    public CartAdapter(ArrayList<Cart> products, Context context, CartSelectListener selectListener) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cart_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Cart cart = products.get(position);
        holder.productName.setText(cart.getTitle());
        holder.category.setText(cart.getCategoryName());
        holder.price.setText("Rs. " + cart.getPrice()+".00");
        holder.qty.setText(cart.getQty());

        holder.plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.productAddQty(products.get(position));
            }
        });

        holder.minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.productRemoveQty(products.get(position));
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.removeProduct(products.get(position));
            }
        });


        storage.getReference("item_img/"+cart.getImagePath())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .fit()
                                .centerCrop()
                                .into(holder.image);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView category;
        TextView price;
        TextView qty;
        ImageView image;
        ImageView plusBtn;
        ImageView minusBtn;
        ImageView deleteBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.cartProductName);
            category = itemView.findViewById(R.id.cartCategory);
            price = itemView.findViewById(R.id.cartPrice);
            qty = itemView.findViewById(R.id.cartQty);
            image = itemView.findViewById(R.id.cartImage);
            plusBtn = itemView.findViewById(R.id.cartPlusBtn);
            minusBtn = itemView.findViewById(R.id.cartMinusBtn);
            deleteBtn = itemView.findViewById(R.id.deleteCartBtn);

        }
    }
}
