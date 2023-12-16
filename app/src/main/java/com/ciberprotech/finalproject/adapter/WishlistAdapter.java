package com.ciberprotech.finalproject.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ciberprotech.finalproject.R;
import com.ciberprotech.finalproject.listner.WishlistSelectListener;
import com.ciberprotech.finalproject.model.Wishlist;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class WishlistAdapter  extends RecyclerView.Adapter<WishlistAdapter.ViewHolder>{
    private ArrayList<Wishlist> wishlists;
    private Context context;
    private FirebaseStorage storage;
    private WishlistSelectListener selectListener;

    public WishlistAdapter(ArrayList<Wishlist> wishlists, Context context, WishlistSelectListener selectListener) {
        this.wishlists = wishlists;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.product_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Wishlist wishlist = wishlists.get(position);
        holder.productName.setText(wishlist.getTitle());
        holder.category.setText(wishlist.getCategoryName());
        holder.price.setText("Rs. " + wishlist.getPrice()+".00");

        holder.wishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context.getApplicationContext(), "Ok", Toast.LENGTH_SHORT).show();
                selectListener.addWishlistProduct(wishlists.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView category;
        TextView price;
        ImageView image;
        Button wishlistBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            category = itemView.findViewById(R.id.textCategory);
            price = itemView.findViewById(R.id.textPrice);
            image = itemView.findViewById(R.id.productImage);
            wishlistBtn = itemView.findViewById(R.id.wishlistBtn);

        }
    }
}
