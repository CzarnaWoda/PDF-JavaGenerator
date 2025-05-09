package com.orange.pdf.builder.data;

import lombok.Getter;

@Getter
public class GenreSummary {
    private String genre;
    private int count;

    public GenreSummary(String genre, int count) {
        this.genre = genre;
        this.count = count;
    }
}