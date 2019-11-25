package com.example.misc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MapUtils {
    private GoogleMap mMap;
    private static final String USER_NAME = "username";
    private static final String USER_ID = "id";
    private Context context;
    private WebSocketClient webSocketClient;
    public  boolean gotDirections=false;
    private final String IP_ADDRESS = "http://192.168.43.176:8080/";
    private double lati = 0.0, longi = 0.0;
    private  FusedLocationProviderClient fusedLocationClient;
    private  SharedPreferences prefs;
    private Activity activity;

    public MapUtils(Context context, GoogleMap mMap){
        this.context=context;
        this.mMap=mMap;
        this.fusedLocationClient=LocationServices.getFusedLocationProviderClient(context);
        this.prefs=context.getSharedPreferences("prefs", 0);
        this.activity=(Activity)context;
        //getPersons();
    }


    public void updateLocation() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);
        int id = sharedPreferences.getInt("id", -1);
        if (id != -1) {
            //getInstantLocation();
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = IP_ADDRESS+"updatePersonLocation";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id",id );
            params.put("latitude",lati );
            params.put("longitude", longi);
            JSONObject parameters = new JSONObject(params);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonObjectRequest);
        }
    }

    public void getPersons(){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final String url = IP_ADDRESS+"locations/persons";
        JsonArrayRequest personsJsonArray=new JsonArrayRequest(Request.Method.GET, url,null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println("test");
                try{
                    for(int i =0;i<response.length();i++) {
                        LatLng position= new LatLng(Double.parseDouble(response.getJSONObject(i).getString("latitude")),
                                Double.parseDouble(response.getJSONObject(i).getString("longitude")));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(response.getJSONObject(i).getString("name")));
                        marker.setTag(Integer.parseInt(response.getJSONObject(i).get("id").toString()));
                        Person p = new Person(Integer.parseInt(response.getJSONObject(i).get("id").toString()),
                                response.getJSONObject(i).getString("name"),
                                position,marker
                        );
                        //Person.Persons.add(p);
                    }
                    getInstantLocation(0);
                    promptUser();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                // Do something when error occurred
                System.out.println(error);
            }
        });
        try {
            requestQueue.add(personsJsonArray);
        }
        catch (Exception ex){
            System.out.println("EXCEPTION WHILE GETTING PERSONS"+ex);
        }
    }

    public void getPlaces(){
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        final String url=IP_ADDRESS+"locations/places";
        JsonArrayRequest placesJsonArray= new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        LatLng position= new LatLng(Double.parseDouble(response.getJSONObject(i).getString("latitude")),
                                Double.parseDouble(response.getJSONObject(i).getString("longitude")));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(response.getJSONObject(i).getString("name")));
                        marker.setTag("place");
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        Place p = new Place(Integer.parseInt(response.getJSONObject(i).get("id").toString()),position,
                                response.getJSONObject(i).getString("name"),
                                marker
                        );

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(placesJsonArray);
    }

    public void getRDVPlace(){
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        final String url=IP_ADDRESS+"nearestPlace";
        JsonObjectRequest RDV_place = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int id = Integer.parseInt(String.valueOf(response.get("id")));
                    Place.setRDV(Place.getRDVplace(id));
                    for(int i=0;i<Place.Places.size();i++){
                        if(Place.Places.get(i).getId()!=Place.getRDV().getId())
                            Place.Places.get(i).getMarker().setVisible(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        requestQueue.add(RDV_place);
    }

    public void getRoutesToNearest(ListView list){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final String url = IP_ADDRESS+"routesToNearest";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    UtilsJsonParser d = new UtilsJsonParser();
                    d.displayRoutes(response,mMap);
                    gotDirections=true;
                    showDirections(list);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void showDirections(ListView listView){
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);
        int id = sharedPreferences.getInt("id", -1);
        Person p =Person.getPersonById(id);
        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, p.getInstructions()));
    }


    public  void promptUser() {

        String username = prefs.getString(USER_NAME,"");
        if (username.length() > 0) {
            Toast.makeText(context, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
        } else {
            //show a dialog box asking for his name
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Hello!");
            alert.setMessage("What is your name?");
            final Spinner spinner = new Spinner(context);
            alert.setView(spinner);
            final HashMap<String, Integer> persons = new HashMap<>();
            for (int i = 0; i < Person.Persons.size(); i++) {
                persons.put(Person.Persons.get(i).getNom(), Person.Persons.get(i).getId());
            }
            ArrayList<String> arrayListPersons = new ArrayList<String>();
            for (String str : persons.keySet())
                arrayListPersons.add(str);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, arrayListPersons);
            spinner.setAdapter(adapter);

            // Make an "OK" button to save the name
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {


                    // Grab the EditText's input
                    String inputName = spinner.getSelectedItem().toString();
                    int id = persons.get(inputName);
                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = prefs.edit();
                    e.putInt(USER_ID, id);
                    e.putString(USER_NAME, inputName);
                    e.commit();

                    // Welcome the new user
                    Toast.makeText(context, "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });
            alert.show();
        }
    }


    public  void getInstantLocation(int c) {
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1000);
        } else {
            try {
                // already permission granted
                // get location here
                fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        lati = location.getLatitude();
                        longi = location.getLongitude();
                        Toast.makeText(context, "" + lati + "," + longi, Toast.LENGTH_SHORT).show();
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lati, longi)));
                        //mMap.addMarker(new MarkerOptions().position(new LatLng(lati,longi)).title("Name"));
                        SharedPreferences sharedPreferences = context.getSharedPreferences("prefss", 0);
                        int id = sharedPreferences.getInt(USER_ID, -1);
                        Person p = Person.getPersonById(id);
                        if(p!=null){
                            Marker old_marker = p.getMarker();
                            old_marker.setVisible(false);
                            LatLng new_Pos = new LatLng(lati, longi);
                            Marker new_marker = mMap.addMarker(new MarkerOptions().position(new_Pos).title(p.getNom()));
                            new_marker.setTag(String.valueOf(p.getId()));
                            p.setMarker(new_marker);
                            p.setPosition(new_Pos);
                        }

                        if(c==1){
                            updateLocation();
                        }
                    }
                });
            } catch (Exception ex) {
                Toast.makeText(context, ex + "", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://192.168.43.176:8080/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Hello World!");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String[] m = message.split(";");
                            int nid = Integer.parseInt(m[0]);
                            double la = Double.parseDouble(m[1]);
                            double  lo = Double.parseDouble(m[2]);
                            if(nid!=prefs.getInt(USER_ID, -1)){
                                Person p = Person.getPersonById(nid);
                                p.getMarker().setVisible(false);
                                LatLng new_pos = new LatLng(la,lo);
                                p.setMarker(mMap.addMarker(new MarkerOptions().position(new_pos).title(p.getNom())));
                            }
                            System.out.println("-----------------MESSAGE REVEIVED------------------"+message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void sendMessage(String msg) {
        Log.i("WebSocket", "Location was sent");
        // Send button id string to WebSocket Server
        webSocketClient.send(msg);
    }


    public void sendInstantLocation(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 20 seconds
                //getInstantLocation(0);
                int id = prefs.getInt("id",-1);
                if(id!=-1)
                    sendMessage(id+";"+lati+";"+longi);
                handler.postDelayed(this,10000);
            }
        }, 10000);
    }

}
