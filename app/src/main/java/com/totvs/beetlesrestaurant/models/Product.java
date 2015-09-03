package com.totvs.beetlesrestaurant.models;

/**
 * Created by rond.borges on 12/08/2015.
 */
public class Product {

    private String title;
    private String description;
    private String pictureUrl;
    private Double price;
    private String company;

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getPictureUrl(){
        return pictureUrl;
    }

    public Double getPrice(){
        return price;
    }

    public String getCompany(){ return company;}

    private Product(){
    }

    Product(String title, String description, String pictureUrl, Double price, String company){
        this.title          = title;
        this.description    = description;
        this.pictureUrl     = pictureUrl;
        this.price          = price;
        this.company        = company;
    }
}
