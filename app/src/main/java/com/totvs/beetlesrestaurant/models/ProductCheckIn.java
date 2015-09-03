package com.totvs.beetlesrestaurant.models;

/**
 * Created by rond.borges on 03/09/2015.
 */
public class ProductCheckIn {

    private String title;
    private String description;
    private String pictureUrl;
    private Double price;
    private String company;
    private String transaction;
    private String productTransaction;
    private Boolean checked;

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

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getProductTransaction(){
        return productTransaction;
    }

    public void setProductTransaction(String productTransaction){
        this.productTransaction = productTransaction;
    }

    public Boolean getChecked(){
        return checked;
    }

    public void setChecked(Boolean checked){
        this.checked = checked;
    }

    private ProductCheckIn(){

    }

    public ProductCheckIn(String title, String description, String pictureUrl, Double price, String company, String transaction, String productTransaction, Boolean checked) {
        this.title              = title;
        this.description        = description;
        this.pictureUrl         = pictureUrl;
        this.price              = price;
        this.company            = company;
        this.transaction        = transaction;
        this.productTransaction = productTransaction;
        this.checked            = checked;
    }

    public void copyFrom(Product source){
        this.title          = source.getTitle();
        this.description    = source.getDescription();
        this.pictureUrl     = source.getPictureUrl();
        this.price          = source.getPrice();
        this.company        = source.getCompany();
    }
}
