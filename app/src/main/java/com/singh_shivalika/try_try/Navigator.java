package com.singh_shivalika.try_try;

import android.content.Context;

import java.lang.Math;
import java.util.ArrayList;

public class Navigator {

    public boolean isPaused = false;

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
    long MIN_DISTANCE_FOR_EARLY_NOTIFICATION = 40;

    NodePoint currentLocation;

    Vibration vibration;
    Compass compass;
    Context appcontext;
    private int UPDATE_INTERVAL;

    int currentpath_index=0;

    Navigator(Context appcontext, int update_interval, String json_data) {
        this.appcontext = appcontext;
        vibration = new Vibration(appcontext);
        compass = new Compass(appcontext);
        this.UPDATE_INTERVAL = update_interval;
        json_data = ","+json_data;
        ArrayList<NodePoint> np = new ArrayList<NodePoint>();

        //Nodepoints array
        String[] str = json_data.split("]");
        for(int i = 0; i< str.length; i++){
            String[] temp = str[i].substring(1).split(",");

            np.add(new NodePoint(Double.parseDouble(temp[1]),Double.parseDouble(temp[0].substring(1))));
        }

        this.coords = np;

        currentLocation = new NodePoint(coords.get(0).latitude,coords.get(0).longitude);

        //Create paths and storing in array
        paths = new ArrayList<Path>();
        for(int i = 0; i< coords.size()-1;i++){
            paths.add(new Path(coords.get(i),coords.get(i+1)));
        }

        for(int i = 0; i< paths.size()-1; i++){
            paths.get(i).destination.changeInBearing = paths.get(i+1).bearingAngle-paths.get(i).bearingAngle;
        }

        currentpath_index = 0;
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

    public void wait(int seconds){
        try {
            Thread.sleep(seconds);
        }catch (Exception e){ }
    }

    public String getUpdate(double latitude, double longitude) {
        if(isPaused)return "PAUSED";
        double compass = this.compass.degree;
        currentLocation.latitude = latitude;
        currentLocation.longitude = longitude;
        String message = "";

        //Check whether user came to a destination node
        if(getDistance(currentLocation.latitude,currentLocation.longitude,paths.get(currentpath_index).destination.latitude,paths.get(currentpath_index).destination.longitude)<MIN_DISTANCE_FOR_CHANGE) {
            if(currentpath_index==paths.size()-1) {
                return "Hola, You reached your destination.";
            }
            currentpath_index++;
            return getUpdate(latitude,longitude);
        }

        //Check whether user is on path
        if(getDistance(currentLocation.latitude,currentLocation.longitude,paths.get(currentpath_index).source.latitude,paths.get(currentpath_index).source.longitude) + getDistance(currentLocation.latitude,currentLocation.longitude,paths.get(currentpath_index).destination.latitude,paths.get(currentpath_index).destination.longitude) - getDistance(paths.get(currentpath_index).source.latitude,paths.get(currentpath_index).source.longitude,paths.get(currentpath_index).destination.latitude,paths.get(currentpath_index).destination.longitude) < MAX_DISTANCE_DEVIATION) {

            //User is on the road, now checking angle
            if (Math.abs(compass - getBearing(currentLocation.latitude, currentLocation.longitude, paths.get(currentpath_index).destination.latitude, paths.get(currentpath_index).destination.longitude)) < MAX_ANGLE_DEVIATION) {

                // User is at correct angle
                long distance = (int) (getDistance(currentLocation.latitude, currentLocation.longitude, paths.get(currentpath_index).destination.latitude, paths.get(currentpath_index).destination.longitude));
                message = "Walk straight by " + distance + " meters.";

                if (distance < MIN_DISTANCE_FOR_EARLY_NOTIFICATION) {
                    if (currentpath_index != paths.size() - 1) {

                        //less than 40 min distance checking
                        int nextBearing = (int) (paths.get(currentpath_index + 1).bearingAngle - getBearing(currentLocation.latitude, currentLocation.longitude, paths.get(currentpath_index).destination.latitude, paths.get(currentpath_index).destination.longitude));
                        if(nextBearing>MAX_ANGLE_DEVIATION) {
                            if (nextBearing > 0)
                                message += " After that take a right of " + Math.abs(nextBearing) + " degrees";
                            else if (nextBearing < 0)
                                message += " After that take a left of " + Math.abs(nextBearing) + " degrees";
                        }
                    }
                }

                return message;
            } else {

                //User is still on the road but facing wrong direction
                int dAngle = (int) (getBearing(currentLocation.latitude, currentLocation.longitude, paths.get(currentpath_index).destination.latitude, paths.get(currentpath_index).destination.longitude) - compass);
                if (dAngle < 0)
                    message = "Turn left by " + (int) Math.abs(dAngle) + " degrees";
                else
                    message = "Turn right by " + (int) Math.abs(dAngle) + " degrees";

                vibration.vibrate(2);
                return message;
            }
        }
        else{
            //User is not on the road....

            //Maybe teleported (:|)  but to correct road
            //Search for roods in paths...
            boolean isNewPathFound = false;
            for(int i = 0; i < paths.size(); i++){
                Path p = paths.get(i);
                if(getDistance(currentLocation.latitude,currentLocation.longitude,p.source.latitude,p.source.longitude) + getDistance(currentLocation.latitude,currentLocation.longitude,p.destination.latitude,p.destination.longitude) - getDistance(p.source.latitude,p.source.longitude,p.destination.latitude,p.destination.longitude) < MAX_DISTANCE_DEVIATION) {
                    currentpath_index = i;
                    isNewPathFound=true;
                }
            }
            if(isNewPathFound)
                return getUpdate(currentLocation.latitude,currentLocation.longitude);

                //User is totally off route...
            else
                return "Sorry, you are off route, requesting path re-route.";
        }
    }

    public void pause(){
        //compass.pause();

    }

    public void resume(){
        compass.resume();
    }

}

