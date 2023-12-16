package com.ciberprotech.finalproject.model;

public class Cart {

    private String id;
    private String title;
    private String categoryName;
    private String price;
    private String qty;
    private String imagePath;

    public Cart() {
    }

    public Cart(String id, String title, String categoryName, String price, String qty,String imagePath) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.price = price;
        this.qty = qty;
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
