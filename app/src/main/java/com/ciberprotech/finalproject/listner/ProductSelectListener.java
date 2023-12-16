package com.ciberprotech.finalproject.listner;

import com.ciberprotech.finalproject.model.Product;

public interface ProductSelectListener {
    void viewSingleProduct(Product product);
    void addWishlistProduct(Product product);

}
