package com.totvs.beetlesrestaurant.models;

/**
 * Created by rond.borges on 01/09/2015.
 */
public class Company {

    public static final String COMPANY_CODE = "COMPANY_CODE";

    private static Company company = null;

    private String title;
    private String phone;
    private String location;
    private String pictureUrl;
    private String companyCode;

    public static Company getCompany() {
        return company;
    }

    public static void setCompany(Company company) {
        Company.company = company;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getTitle() {
        return title;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    private Company(){
        this.title          = "";
        this.phone          = "";
        this.location       = "";
        this.pictureUrl     = "";
        this.companyCode    = "";
    }

    Company(String title, String phone, String location, String pictureUrl, String companyCode){
        this.title          = title;
        this.phone          = phone;
        this.location       = location;
        this.pictureUrl     = pictureUrl;
        this.companyCode    = companyCode;
    }

    public void copyFrom(Company source){
        this.title          = source.getTitle();
        this.phone          = source.getPhone();
        this.location       = source.getLocation();
        this.pictureUrl     = source.getPictureUrl();
        this.companyCode    = source.getCompanyCode();
    }

    public static synchronized Company getInstance(){
        if(null == company){
            company = new Company();
        }
        return company;
    }

    public void clear(){
        this.title          = "";
        this.phone          = "";
        this.location       = "";
        this.pictureUrl     = "";
        this.companyCode    = "";
    }
}
