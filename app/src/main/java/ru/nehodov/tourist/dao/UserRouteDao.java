package ru.nehodov.tourist.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ru.nehodov.tourist.entities.UserRoute;

@Dao
public interface UserRouteDao {

    @Insert
    long insert(UserRoute route);

    @Query("SELECT * FROM userroute")
    LiveData<List<UserRoute>> getAllRoutes();

}
