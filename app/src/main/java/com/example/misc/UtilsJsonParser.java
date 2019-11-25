package com.example.misc;

import android.graphics.Color;
import android.service.autofill.FillEventHistory;
import android.text.Html;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.example.misc.Person.*;

public class UtilsJsonParser {
    private Polyline mPolyline;
    private ArrayList<LatLng> pts = null;
    private PolylineOptions ligneOpts = null;
    Color c;

//    public ArrayList<LatLng> parse(JSONObject jObject) {
//        //List<List<HashMap<String, String>>> routes = new ArrayList<>();
//        ArrayList<LatLng> pts =new ArrayList<>();
//        double lat,lng;
//        LatLng position;
//        JSONObject p1;
//        JSONObject jRoutes_L1=null;
//        JSONArray jRoutes_L2=null;
//        JSONArray jRoutes_L3=null;
//
//
//        try{
//            //List path = new ArrayList<HashMap<String,String>>();
//            p1=jObject.getJSONObject("1");
//            jRoutes_L1=p1.getJSONObject("routes");
////            lat = Double.parseDouble((jRoutes_L1).getString("from_latitude"));
////            lng = Double.parseDouble((jRoutes_L1).getString("from_longitude"));
////            position = new LatLng(lat, lng);
////            pts.add(position);
//            jRoutes_L2=p1.getJSONObject("routes").getJSONArray("routes");
//            for(int k=0;k<jRoutes_L2.length();k++) {
//                    jRoutes_L3 =((JSONObject)jRoutes_L2.get(k)).getJSONArray("routes");
//                    if (jRoutes_L3.length()>0) {
//                        for (int i = 0; i < jRoutes_L3.length(); i++) {
//                            lat = Double.parseDouble(((JSONObject) jRoutes_L3.get(i)).getString("from_latitude"));
//                            lng = Double.parseDouble(((JSONObject) jRoutes_L3.get(i)).getString("from_longitude"));
//                            position = new LatLng(lat, lng);
//                            pts.add(position);
//                        }
//                    }
//                    else{
//                        lat = Double.parseDouble(((JSONObject) jRoutes_L2.get(k)).getString("from_latitude"));
//                        lng = Double.parseDouble(((JSONObject) jRoutes_L2.get(k)).getString("from_longitude"));
//                        position = new LatLng(lat, lng);
//                        pts.add(position);
//                    }
//                }
//        }
//
//        catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return pts ;
//    }


    public List<List<HashMap<String, String>>> displayRoutes(JSONObject jObject, GoogleMap mMap) {
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONObject jRoutes = null;
        JSONArray jPolyLine = null;
        JSONArray jInstructions=null;
        ArrayList<String> s_intsructions=null;
        String polyline = "";
        Person person;
        JSONObject p1;

        try {
            for (int id = 0; id < jObject.length(); id++) {
                p1 = jObject.getJSONObject(String.valueOf(id));
                person = Person.Persons.get(id);
                if(person.getPolyline() != null)
                    person.getPolyline().setVisible(false);
                jRoutes = p1.getJSONObject("routes");
                jInstructions=jRoutes.getJSONArray("instructions");
                try {
                    s_intsructions = new ArrayList<>();
                    for (int x = 0; x < jInstructions.length(); x++)
                        s_intsructions.add(String.valueOf(Html.fromHtml(jInstructions.getString(x))));
                    person.setInstructions(s_intsructions);
                }catch (Exception e){e.printStackTrace();}

                List path = new ArrayList<HashMap<String, String>>();
                //Traversing all routes
                for (int i = 0; i < jRoutes.length(); i++) {
                    jPolyLine = jRoutes.getJSONArray("polyline");
                    for (int j = 0; j < jPolyLine.length(); j++) {
                        polyline = (String) jPolyLine.get(j);

                        List<LatLng> list = decodePoly(polyline);
                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                }
                path = (List) path.stream().distinct().collect(Collectors.toList());

                pts = new ArrayList<>();
                PolylineOptions ligneOpts = new PolylineOptions();

                for (int l = 0; l < path.size(); l++) {
                    HashMap<String, String> pt = (HashMap<String, String>) path.get(l);
                    double lat = Double.parseDouble(pt.get("lat"));
                    double lng = Double.parseDouble(pt.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    pts.add(position);
                }

                ligneOpts.addAll(pts);
                ligneOpts.width(8);
                ligneOpts.color(person.getColor());

                // Drawing polyline in the Google Map for the i-th route
                mPolyline = mMap.addPolyline(ligneOpts);

                person.setPolyline(mPolyline);

                routes.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return routes;
    }


    /**
     * Method to decode polyline points
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

}
