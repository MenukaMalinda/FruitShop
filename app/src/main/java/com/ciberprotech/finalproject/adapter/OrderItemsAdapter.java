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
import com.ciberprotech.finalproject.model.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {
    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;

    public OrderItemsAdapter() {
    }

    public OrderItemsAdapter(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();

    }

    @NonNull
    @Override
    public OrderItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_view_layout, parent, false);
        return new OrderItemsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = products.get(position);
        holder.productTitleTextView.setText(product.getTitle());
        holder.qty.setText(product.getQty());
        holder.productPriceTextView.setText("Rs." + product.getPrice());

        storage.getReference("item_img/" + product.getImagePath1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(140, 140)
                                .centerCrop()
                                .into(holder.productIconImageView);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitleTextView, productCategoryTextView, productPriceTextView, qty;
        ImageView productIconImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitleTextView = itemView.findViewById(R.id.orderProductName);
            productPriceTextView = itemView.findViewById(R.id.orderPrice);
            productCategoryTextView = itemView.findViewById(R.id.orderCategory);
            productIconImageView = itemView.findViewById(R.id.orderImage);
            qty = itemView.findViewById(R.id.orderQty);
        }
    }

}
