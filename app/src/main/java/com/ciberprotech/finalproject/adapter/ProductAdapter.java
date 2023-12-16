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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ciberprotech.finalproject.R;
import com.ciberprotech.finalproject.listner.ProductSelectListener;
import com.ciberprotech.finalproject.model.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;
    private ProductSelectListener selectListener;

    public ProductAdapter(ArrayList<Product> products, Context context, ProductSelectListener selectListener) {
        this.products = products;
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
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = products.get(position);
        holder.productName.setText(product.getTitle());
        holder.category.setText(product.getCategoryName());
        holder.price.setText("Rs. "+product.getPrice()+".00");

        holder.wishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.addWishlistProduct(products.get(position));
            }
        });

        holder.productCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewSingleProduct(products.get(position));
            }
        });

        storage.getReference("item_img/"+product.getImagePath1())
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
        ImageView image;
        ImageView wishlistBtn;

        CardView productCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            category = itemView.findViewById(R.id.textCategory);
            price = itemView.findViewById(R.id.textPrice);
            image = itemView.findViewById(R.id.productImage);
            wishlistBtn = itemView.findViewById(R.id.wishlistBtn);

            productCardView = itemView.findViewById(R.id.productCardView);

        }
    }
}
