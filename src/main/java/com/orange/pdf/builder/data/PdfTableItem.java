package com.orange.pdf.builder.data;

// Data class for table items
public class TableItem {
    private String index;
    private String name;
    private int quantity;
    private String unit;
    private double value;

    public TableItem(String index, String name, int quantity, String unit, double value) {
        this.index = index;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.value = value;
    }

    public String getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }
}