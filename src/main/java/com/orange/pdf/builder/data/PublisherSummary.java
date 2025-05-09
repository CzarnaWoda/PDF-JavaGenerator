package com.orange.pdf.builder.data;

import lombok.Getter;

@Getter
public class PublisherSummary {
    private String publisher;
    private int count;

    public PublisherSummary(String publisher, int count) {
        this.publisher = publisher;
        this.count = count;
    }
}