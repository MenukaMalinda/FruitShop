package com.ciberprotech.finalproject.adapter;


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
import com.ciberprotech.finalproject.listner.SearchSelectListener;
import com.ciberprotech.finalproject.model.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private ArrayList<Product> products;
    private Context context;
    private FirebaseStorage storage;
    private SearchSelectListener selectListener;

    public SearchResultAdapter(ArrayList<Product> products, Context context, SearchSelectListener selectListener) {
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
        return new SearchResultAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText(product.getPrice());
        holder.productCategory.setText(product.getCategoryName());

        holder.productCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.viewProduct(product);
            }
        });

        storage.getReference("item_img/"+product.getImagePath1())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get()
                                .load(uri)
                                .centerCrop()
                                .fit()
                                .into(holder.productIcon);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle, productPrice,productCategory;
        ImageView productIcon;
        View productCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.textProductName);
            productCategory = itemView.findViewById(R.id.textCategory);
            productIcon = itemView.findViewById(R.id.productImage);
            productPrice = itemView.findViewById(R.id.textPrice);
            productCard = itemView.findViewById(R.id.productCardView);
        }
    }
}