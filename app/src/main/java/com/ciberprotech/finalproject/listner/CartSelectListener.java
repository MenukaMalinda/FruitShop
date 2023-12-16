package com.ciberprotech.finalproject.listner;

import com.ciberprotech.finalproject.model.Cart;

public interface CartSelectListener {
    void viewProduct(Cart cart);
    void productAddQty(Cart cart);
    void productRemoveQty(Cart cart);
    void removeProduct(Cart cart);
}
