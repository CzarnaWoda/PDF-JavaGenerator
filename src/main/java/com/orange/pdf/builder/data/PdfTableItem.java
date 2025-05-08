package com.orange.pdf.builder.data;

import lombok.Getter;

@Getter

public class PdfTableItem{

    private String index;
    private String name;
    private int quantity;
    private String unit;
    private double value;

    public PdfTableItem(String index, String name, int quantity, String unit, double value) {
        this.index = index;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.value = value;
    }
}