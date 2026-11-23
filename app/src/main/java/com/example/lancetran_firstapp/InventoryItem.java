package com.example.lancetran_firstapp;

public class InventoryItem {
    private int id;
    private String name;
    private String sku;
    private String storageLocation;
    private int quantity;
    private String notes;

    // Full constructor
    public InventoryItem(int id, String name, String sku, String storageLocation, int quantity, String notes) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.storageLocation = storageLocation;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}