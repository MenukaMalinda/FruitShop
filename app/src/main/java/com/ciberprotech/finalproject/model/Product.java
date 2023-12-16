package com.ciberprotech.finalproject.model;

public class Product {
    private String id;
    private String title;
    private String categoryName;
    private String price;
    private String qty;
    private String description;
    private String imagePath1;
    private String imagePath2;
    private String imagePath3;

    public Product() {
    }

    public Product(String id, String title, String categoryName, String price, String qty, String description, String imagePath1, String imagePath2, String imagePath3) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.price = price;
        this.qty = qty;
        this.description = description;
        this.imagePath1 = imagePath1;
        this.imagePath2 = imagePath2;
        this.imagePath3 = imagePath3;
    }

    public Product(String id, String title, String categoryName, String price, String qty, String imagePath1) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.price = price;
        this.qty = qty;
        this.imagePath1 = imagePath1;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath1() {
        return imagePath1;
    }

    public void setImagePath1(String imagePath1) {
        this.imagePath1 = imagePath1;
    }

    public String getImagePath2() {
        return imagePath2;
    }

    public void setImagePath2(String imagePath2) {
        this.imagePath2 = imagePath2;
    }

    public String getImagePath3() {
        return imagePath3;
    }

    public void setImagePath3(String imagePath3) {
        this.imagePath3 = imagePath3;
    }
}
