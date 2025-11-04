package com.example.thuoc.model;

public class Medicine {
    private String id;
    private String name;
    private String expirydate;
    private int quantity;
    private String unit;

    public Medicine() {} // Firebase cần constructor rỗng

    public Medicine(String id, String name, String description, int quantity, String unit) {
        this.id = id;
        this.name = name;
        this.expirydate = expirydate;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpiryDate() { return expirydate; }
    public void setExpiryDate(String expirydate) { this.expirydate = expirydate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
