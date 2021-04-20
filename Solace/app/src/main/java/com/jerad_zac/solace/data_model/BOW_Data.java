package com.jerad_zac.solace.data_model;

import java.io.Serializable;
import java.util.Date;

public class BOW_Data implements Serializable {


    // Book
    private String bookTitle;
    private String bookDesc;
    private String bookThumb;
    private String bookLink;
    private Date timestamp;

    public BOW_Data(String bookTitle, String bookDesc, String bookThumb, String bookLink, Date timestamp) {
        this.bookTitle = bookTitle;
        this.bookDesc = bookDesc;
        this.bookThumb = bookThumb;
        this.bookLink = bookLink;
        this.timestamp = timestamp;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookDesc() {
        return bookDesc;
    }

    public void setBookDesc(String bookDesc) {
        this.bookDesc = bookDesc;
    }

    public String getBookThumb() {
        return bookThumb;
    }

    public void setBookThumb(String bookThumb) {
        this.bookThumb = bookThumb;
    }

    public String getBookLink() {
        return bookLink;
    }

    public void setBookLink(String bookLink) {
        this.bookLink = bookLink;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
