package ru.nehodov.tourist.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class RoutePoint {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private final double latitude;
    private final double longitude;
    private long routeId;

    public RoutePoint(long id, double latitude, double longitude, long routeId) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.routeId = routeId;
    }

    @Ignore
    public RoutePoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public long getRouteId() {
        return routeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoutePoint that = (RoutePoint) o;
        return id == that.id
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && routeId == that.routeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude, routeId);
    }
}
