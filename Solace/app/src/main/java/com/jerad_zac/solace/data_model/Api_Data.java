package com.jerad_zac.solace.data_model;

import java.io.Serializable;
import java.util.List;

public class Api_Data implements Serializable {

    // Quote
    private String quote;

    // Book
    private String bookTitle;
    private String bookDesc;
    private String bookThumb;
    private String bookLink;

    // Constructor
    public Api_Data(String quote, String bookTitle, String bookDesc, String bookThumb, String bookLink) {
        this.quote = quote;
        this.bookTitle = bookTitle;
        this.bookDesc = bookDesc;
        this.bookThumb = bookThumb;
        this.bookLink = bookLink;
    }

    // Getters
    public String getQuote() {
        return quote;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookDesc() {
        return bookDesc;
    }

    public String getBookThumb() {
        return bookThumb;
    }

    public String getBookLink() {
        return bookLink;
    }

    // Setters

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setBookDesc(String bookDesc) {
        this.bookDesc = bookDesc;
    }

    public void setBookThumb(String bookThumb) {
        this.bookThumb = bookThumb;
    }

    public void setBookLink(String bookLink) {
        this.bookLink = bookLink;
    }
}
