package com.pearnode.app.placero.media.model;

import com.google.gson.Gson;

public class Media {

    private Long id = -1L;
    private Long placeRef = -1L;
    private String name = "";
    private String type = "";
    private String tfName = "";
    private String tfPath = "";
    private String rfName = "";
    private String rfPath = "";
    private String lat = "";
    private String lng = "";
    private Long createdOn = -1L;
    private Long fetchedOn = -1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlaceRef() {
        return placeRef;
    }

    public void setPlaceRef(Long placeRef) {
        this.placeRef = placeRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTfName() {
        return tfName;
    }

    public void setTfName(String tfName) {
        this.tfName = tfName;
    }

    public String getRfName() {
        return rfName;
    }

    public void setRfName(String rfName) {
        this.rfName = rfName;
    }

    public String getTfPath() {
        return tfPath;
    }

    public void setTfPath(String tfPath) {
        this.tfPath = tfPath;
    }

    public String getRfPath() {
        return rfPath;
    }

    public void setRfPath(String rfPath) {
        this.rfPath = rfPath;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Long getFetchedOn() {
        return fetchedOn;
    }

    public void setFetchedOn(Long fetchedOn) {
        this.fetchedOn = fetchedOn;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

}