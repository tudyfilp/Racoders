package com.racoders.racodersproject.classes;

import java.util.Comparator;
import java.util.Date;

public class News {

    private String title;
    private String author;
    private String description;
    private Date publicationDate;
    private String id;
    private String reference;
    private int viewsNumber;

    public News() { }

    public News(String title, String author, String description, Date publicationDate, String id, String reference, int viewsNumber) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publicationDate = publicationDate;
        this.id = id;
        this.reference = reference;
        this.viewsNumber = viewsNumber;
    }


    public String getReference(){return reference;}

    public void setReference(String value){ reference = value;}

    public String getTitle() { return title; }

    public void setTitle(String title) {this.title = title; }

    public String getAuthor() {return author; }

    public void setAuthor(String author) {this.author = author; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void increaseViewsNumber(){
        viewsNumber++;
    }
    public int getViewsNumber(){
        return viewsNumber;
    }
}
