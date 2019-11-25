package com.example.misc;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class Place {
    private int id;
    private LatLng position;
    private String name;
    private Marker marker;

    public static ArrayList<Place> Places=new ArrayList<>();
    public static Place RDV;

    public Place(int id, LatLng position, String name, Marker marker) {
        this.id = id;
        this.position = position;
        this.name = name;
        this.marker = marker;
        Place.Places.add(this);
    }


    public static Place getRDV() {
        return RDV;
    }

    public static void setRDV(Place RDV) {
        Place.RDV = RDV;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public static ArrayList<Place> getPlaces() {
        return Places;
    }

    public static void setPlaces(ArrayList<Place> places) {
        Places = places;
    }

    public static Place getRDVplace(int id){
        for(int i=0;i<Places.size();i++){
            if(Places.get(i).getId()==id)
                return Places.get(i);
        }
        return null;
    }
}
