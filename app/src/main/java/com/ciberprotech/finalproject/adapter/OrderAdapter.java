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
import com.ciberprotech.finalproject.model.Cart;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private ArrayList<Cart> products;
    private Context context;
    private FirebaseStorage storage;

    public OrderAdapter(ArrayList<Cart> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Cart cart = products.get(position);
        holder.productName.setText(cart.getTitle());
        holder.category.setText(cart.getCategoryName());
        holder.price.setText("Rs. " + cart.getPrice()+".00");
        holder.qty.setText(cart.getQty());



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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.orderProductName);
            category = itemView.findViewById(R.id.orderCategory);
            price = itemView.findViewById(R.id.orderPrice);
            qty = itemView.findViewById(R.id.orderQty);
            image = itemView.findViewById(R.id.orderImage);
        }
    }


}
