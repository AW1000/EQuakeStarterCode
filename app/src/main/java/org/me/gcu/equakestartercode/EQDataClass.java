/*
Name: Aidan Watret
Matriculation Number:S1803674
 */

package org.me.gcu.equakestartercode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EQDataClass implements Serializable {
    private String title;
    private String description;
    private String pubDate;
    private double latitude;
    private double longitude;
    private String location;
    private double magnitude;
    private int depth;
    private Date eqDate = null;

    public EQDataClass() {
    }

    public EQDataClass(String title, String description, String pubDate, double latitude, double longitude, String location, double magnitude, int depth) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.magnitude = magnitude;
        this.depth = depth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Date getEqDate() {
        return eqDate;
    }

    public void setEqDate(Date eqDate) {
        this.eqDate = eqDate;
    }

    @Override
    public String toString() {
        return "EQDataClass{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", location='" + location + '\'' +
                ", magnitude=" + magnitude +
                ", depth=" + depth +
                ", eqDate=" + eqDate.toString() +
                '}';
    }

    public String locationMagnitude() {
        return "Location : " + location + " Magnitude : " + magnitude;
    }

    public String displayData() {
        return "Location : " + location + "\nMagnitude : " + magnitude + "\nDate : " + pubDate + "\nLatitude : " + latitude + "\nLongitude : " + longitude + "\nDepth : " + depth + "km";
    }
}

