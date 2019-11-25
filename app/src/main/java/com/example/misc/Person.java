package com.example.misc;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Random;

public class Person {
    private int id;
    private String name;
    private LatLng position;
    private ArrayList<String> instructions;
    private Marker marker;
    private Polyline polyline;
    private int color;

    public static ArrayList<Person> Persons=new ArrayList<>();

    Random rnd = new Random();

    public Person(int id, String name, LatLng position, Marker marker) {
        this.id = id;
        this.name = name;
        this.position=position;
        this.marker=marker;
        this.color=Color.argb(255,rnd.nextInt(256), 100,rnd.nextInt(256));
        Person.Persons.add(this);
    }


    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<String> instructions) {
        this.instructions = instructions;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getNom() {
        return name;
    }

    public void setNom(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public static Person getPersonById(int id){
        for(int i=0;i<Persons.size();i++){
            if(Persons.get(i).getId()==id)
                return Persons.get(i);
        }
        return null;
    }


}
