package com.totvs.beetlesrestaurant.models;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

/**
 * Created by rond.borges on 02/09/2015.
 */
public class BeaconModel {

    private static BeaconModel beaconModel = null;

    private String proximityUUID;
    private String macAddress;
    private String nameBeacon;
    private Integer major;
    private Integer minor;
    private Integer measuredPower;
    private Integer rssi;
    private Double accuracy;
    private String company;
    private Integer table;

    public String getProximityUUID() {
        return proximityUUID;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getNameBeacon() {
        return nameBeacon;
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }

    public Integer getMeasuredPower() {
        return measuredPower;
    }

    public Integer getRssi() {
        return rssi;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company){
        this.company = company;
    }

    public Integer getTable(){
        return table;
    }

    public void setTable(Integer table){
        this.table = table;
    }

    private BeaconModel(){
        this.macAddress = "";
    }

    BeaconModel(String proximityUUID, String macAddress, String nameBeacon, Integer major, Integer minor, Integer measuredPower, Integer rssi, Double accuracy, String company, Integer table){
        this.proximityUUID  = proximityUUID;
        this.macAddress     = macAddress;
        this.nameBeacon     = nameBeacon;
        this.major          = major;
        this.minor          = minor;
        this.measuredPower  = measuredPower;
        this.rssi           = rssi;
        this.accuracy       = accuracy;
        this.company        = company;
        this.table          = table;
    }

    public void copyFrom(BeaconModel source){
        this.proximityUUID  = source.getProximityUUID();
        this.macAddress     = source.getMacAddress();
        this.nameBeacon     = source.getNameBeacon();
        this.major          = source.getMajor();
        this.minor          = source.getMinor();
        this.measuredPower  = source.getMeasuredPower();
        this.rssi           = source.getRssi();
        this.accuracy       = source.getAccuracy();
        this.company        = source.getCompany();
        this.table          = source.getTable();
    }

    public void copyFrom(Beacon source){
        this.proximityUUID  = source.getProximityUUID();
        this.macAddress     = source.getMacAddress();
        this.nameBeacon     = source.getName();
        this.major          = source.getMajor();
        this.minor          = source.getMinor();
        this.measuredPower  = source.getMeasuredPower();
        this.rssi           = source.getRssi();
        this.accuracy       = Utils.computeAccuracy(source);
        this.company        = "";
        this.table          = 0;
    }

    public static synchronized BeaconModel getInstance(){
        if(null == beaconModel){
            beaconModel = new BeaconModel();
        }
        return beaconModel;
    }

    public void clear(){
        this.proximityUUID  = "";
        this.macAddress     = "";
        this.nameBeacon     = "";
        this.major          = 0;
        this.minor          = 0;
        this.measuredPower  = 0;
        this.rssi           = 0;
        this.accuracy       = 0.0;
        this.company        = "";
        this.table          = 0;
    }
}
