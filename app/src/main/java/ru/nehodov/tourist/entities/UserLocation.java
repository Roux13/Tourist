package ru.nehodov.tourist.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class UserLocation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String time;
    private double latitude;
    private double longitude;

    public UserLocation(int id, String name, String time, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public UserLocation(String name, String time, double latitude, double longitude) {
        this.name = name;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserLocation that = (UserLocation) o;
        return id == that.id
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Objects.equals(name, that.name)
                && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, time, latitude, longitude);
    }

}
