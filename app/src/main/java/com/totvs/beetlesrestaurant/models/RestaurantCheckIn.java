package com.totvs.beetlesrestaurant.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rond.borges on 18/08/2015.
 */
public class RestaurantCheckIn {

    private static RestaurantCheckIn restaurantCheckIn = null;

    private Integer table;
    private String beaconIdentifier;
    private String pictureUrl;
    private String status;
    private String customerName;
    private String transaction;
    private Double bill;
    private Integer numberOfPeople;
    private String companyCode;

    public RestaurantCheckIn(Integer table, String beaconIdentifier, String pictureUrl, String status, String customerName, String transaction, Double bill, Integer numberOfPeople, String companyCode){
        this.table            = table;
        this.beaconIdentifier = beaconIdentifier;
        this.pictureUrl       = (pictureUrl == "" ? "http://lorempixel.com/75/75/people/" : pictureUrl);
        this.status           = status;
        this.customerName     = customerName;
        this.transaction      = transaction;
        this.bill             = bill;
        this.numberOfPeople   = numberOfPeople;
        this.companyCode      = companyCode;
    }

    public Integer getTable() {
        return table;
    }

    public String getBeaconIdentifier() {
        return beaconIdentifier;
    }

    public void setBeaconIdentifier(String beaconIdentifier){
        this.beaconIdentifier = beaconIdentifier;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTransaction(){
        return transaction;
    }

    public Double getBill(){
        return bill;
    }

    public void setTable(Integer table){
        this.table = table;
    }

    public void setBill(Double bill){
        this.bill = bill;
    }

    public void setNumberOfPeople(Integer numberOfPeople){
        this.numberOfPeople = numberOfPeople;
    }

    public void setCompanyCode(String companyCode){
        this.companyCode = companyCode;
    }

    public String getCompanyCode(){
        return companyCode;
    }

    public String getDate(){
        return new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa").format(new Date());//28 Aug 2015, 7:09pm
    }

    public void copyFrom(RestaurantCheckIn source){
        this.table = source.table;
        this.beaconIdentifier = source.beaconIdentifier;
        this.pictureUrl = source.pictureUrl;
        this.status = source.status;
        this.customerName = source.customerName;
        this.transaction = source.transaction;
        this.bill = source.bill;
        this.numberOfPeople = source.numberOfPeople;
        this.companyCode = source.companyCode;
    }

    private RestaurantCheckIn(){
        this.transaction = "";
    }

    public static synchronized RestaurantCheckIn getInstance(){
        if(null == restaurantCheckIn){
            restaurantCheckIn = new RestaurantCheckIn();
        }
        return restaurantCheckIn;
    }

    public void clear(){
        this.table              = 0;
        this.beaconIdentifier   = "";
        this.pictureUrl         = "";
        this.status             = "";
        this.customerName       = "";
        this.transaction        = "";
        this.bill               = 0.0;
        this.numberOfPeople     = 1;
        this.companyCode        = "";
    };
}
