package ru.nehodov.tourist.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

@Entity
public class UserRoute {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private final String startPointName;
    private final String endPointName;
    @Ignore
    private List<RoutePoint> routePoints;

    public UserRoute(int id, String startPointName,
                     String endPointName) {
        this.id = id;
        this.startPointName = startPointName;
        this.endPointName = endPointName;
    }

    @Ignore
    public UserRoute(String startPointName, String endPointName, List<RoutePoint> routePoints) {
        this.startPointName = startPointName;
        this.endPointName = endPointName;
        this.routePoints = routePoints;
    }

    public int getId() {
        return id;
    }

    public String getStartPointName() {
        return startPointName;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(List<RoutePoint> routePoints) {
        this.routePoints = routePoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRoute userRoute = (UserRoute) o;
        return Objects.equals(startPointName, userRoute.startPointName)
                && Objects.equals(endPointName, userRoute.endPointName)
                && Objects.equals(routePoints, userRoute.routePoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPointName, endPointName, routePoints);
    }
}
