package com.singh_shivalika.try_try;

import android.content.Context;

import java.lang.Math;
import java.util.ArrayList;

public class Navigator {

    public void stringToArrayList(ArrayList<NodePoint> store_variable, String json_data){

        String[] str = json_data.split("]");
        for(int i = 0; i< str.length; i++){
            String[] temp = str[i].split(",");
            store_variable.add(new NodePoint(Double.parseDouble(temp[1]),Double.parseDouble(temp[0].substring(1))));
        }
    }

    public static class NodePoint{
        double latitude;
        double longitude;
        double changeInBearing;

        NodePoint(double latitude,double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "NodePoint{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }

    private class Path{
        NodePoint source;
        NodePoint destination;
        double distance;
        double bearingAngle;

        Path(NodePoint source, NodePoint destination){
            this.source = source;
            this.destination = destination;
            distance = getDistance(source.latitude,source.longitude,destination.latitude,destination.longitude);
            bearingAngle = getBearing(source.latitude,source.longitude,destination.latitude,destination.longitude);
        }

        @Override
        public String toString() {
            return "Path{" +
                    "source=" + source.latitude + " " + source.longitude+
                    ", destination=" + destination.latitude + " " + destination.longitude+
                    ", distance=" + distance +
                    ", bearingAngle=" + bearingAngle +
                    '}';
        }
    }

    private static final int R = 6378000;

    private double dTR(double degree){
        return Math.PI*degree/180;
    }

    private double rTD(double radian){
        return 180*radian/Math.PI;
    }

    public double getBearing(double sLat,double sLng, double dLat, double dLng){
        double rad = Math.atan2( Math.sin( dTR(dLng-sLng) ) * Math.cos( dTR(dLat)) , (Math.cos(dTR(sLat)) * Math.sin(dTR(dLat)) - (Math.sin(dTR(sLat)) * Math.cos(dTR(dLat)) * Math.cos(dTR(dLng-sLng))) ));
        return  Double.parseDouble(String.format("%.2f", (rTD(rad) + 360) % 360));
    }


    public double getDistance(double sLat,double sLng, double dLat, double dLng){
        double a = Math.sin(dTR(dLat-sLat)/2) * Math.sin(dTR(dLat-sLat)/2) + Math.cos(dTR(sLat)) * Math.cos(dTR(sLat)) * Math.sin(dTR(dLng-sLng)/2) * Math.sin(dTR(dLng-sLng)/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return Double.parseDouble(String.format("%.2f", R * c));
    }

    ArrayList<NodePoint> coords;
    ArrayList<Path> paths;
    double MAX_ANGLE_DEVIATION=5;
    double MAX_DISTANCE_DEVIATION = 10;
    double MIN_DISTANCE_FOR_CHANGE = 7; //4.5*1.414~7
    NodePoint currentLocation;

    Vibration vibration;
    Compass compass;
    Context appcontext;
    private int UPDATE_INTERVAL;

    Path currentpath=null;

    Navigator(Context appcontext, int update_interval, String json_data) {
        this.appcontext = appcontext;
        vibration = new Vibration(appcontext);
        compass = new Compass(appcontext);
        this.UPDATE_INTERVAL = update_interval;
        json_data = ","+json_data;
        ArrayList<NodePoint> np = new ArrayList<NodePoint>();

        String[] str = json_data.split("]");
        for(int i = 0; i< str.length; i++){
            String[] temp = str[i].substring(1).split(",");

            np.add(new NodePoint(Double.parseDouble(temp[1]),Double.parseDouble(temp[0].substring(1))));
        }

        this.coords = np;

        currentLocation = new NodePoint(coords.get(0).latitude,coords.get(0).longitude);

        //Create paths
        paths = new ArrayList<Path>();
        for(int i = 0; i< coords.size()-1;i++){
            paths.add(new Path(coords.get(i),coords.get(i+1)));
        }

        for(int i = 0; i< paths.size()-1; i++){
            paths.get(i).destination.changeInBearing = paths.get(i+1).bearingAngle-paths.get(i).bearingAngle;
        }

        currentpath = paths.get(0);
    }

    public void setMaxDeviationAngle(double deviationAngle) {
        this.MAX_ANGLE_DEVIATION = deviationAngle;
    }

    public void setMaxDeviationDistance(double deviationDiatance) {
        this.MAX_DISTANCE_DEVIATION = deviationDiatance;
    }

    public void printPath(double initial_angle){

        // TODO:  ////////////////////////////////////               ONLY FOR DEMO                    /////////////////////////////////////////////////////

        //Make the user face correct direction
        double initial_change = paths.get(0).bearingAngle-initial_angle;
        if(initial_change>-MAX_ANGLE_DEVIATION)
            System.out.println("Turn left by "+ String.format("%.2f",  Math.abs(initial_change)) +" degrees");
        else if(initial_change<MAX_ANGLE_DEVIATION)
            System.out.println("Turn right by "+String.format("%.2f", Math.abs(initial_change))+" degrees");

        for(int i = 0; i< paths.size();i++){
            System.out.println("Walk straight by "+ String.format("%.2f", paths.get(i).distance) +" meters");

            if(paths.get(i).destination.changeInBearing<0)
                System.out.println("Turn left by "+ String.format("%.2f", Math.abs(paths.get(i).destination.changeInBearing)) +" degrees");
            else if(paths.get(i).destination.changeInBearing>0)
                System.out.println("Turn right by "+ String.format("%.2f", Math.abs(paths.get(i).destination.changeInBearing)) +" degrees");
        }

        System.out.println("U reached your destination");

        // TODO : ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public String getUpdate(double latitude, double longitude) {
        double compass = this.compass.degree;
        currentLocation.longitude = longitude;
        currentLocation.latitude = latitude;

        if(Math.abs(compass - currentpath.bearingAngle) < MAX_ANGLE_DEVIATION){

            if(getDistance(currentpath.destination.latitude,currentpath.destination.longitude,currentLocation.latitude,currentLocation.longitude)<MIN_DISTANCE_FOR_CHANGE) {
                int i = 0;
                for(i = 0; i< paths.size();i++)
                    if(paths.get(i).equals(currentpath))
                        break;
                currentpath = paths.get(i+1);
                return getUpdate(latitude,longitude);
            }

            if(getDistance(currentLocation.latitude,currentLocation.longitude,currentpath.source.latitude,currentpath.source.longitude)
                    +  getDistance(currentLocation.latitude,currentLocation.longitude,currentpath.destination.latitude,currentpath.destination.longitude)
                    -  getDistance(currentpath.source.latitude,currentpath.source.longitude,currentpath.destination.latitude,currentpath.destination.longitude)  <  MAX_DISTANCE_DEVIATION)
                return "Walk straight for another " + (int) getDistance(currentLocation.latitude,currentLocation.longitude,currentpath.destination.latitude,currentpath.destination.longitude) + " m";
        }
        else{
            if(currentpath.bearingAngle-compass<0) {
                vibration.vibrate(UPDATE_INTERVAL);
                return "Turn left by " + (int) Math.abs(currentpath.bearingAngle - compass) + " degrees";
            }
            else {
                vibration.vibrate(UPDATE_INTERVAL);
                return "Turn right by " + (int) Math.abs(currentpath.bearingAngle - compass) + " degrees";
            }
        }
        return "OFFROUTE";
    }

    public void pause(){
        //compass.pause();

    }

    public void resume(){
        compass.resume();
    }

}

