package com.example.delicious.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/* Dao for accessing the data present inside the DB*/

@Dao
interface favresDao {

    @Insert
    fun insertRestaurant(restaurantEntity: FavresEntities)

    @Delete
    fun deleteRestaurant(restaurantEntity: FavresEntities)

    @Query("SELECT * FROM ` Favourite Restaurants`")
    fun getAllRestaurants(): List<FavresEntities>

    @Query("SELECT * FROM ` Favourite Restaurants` WHERE id = :resId")
    fun getRestaurantById(resId: String): FavresEntities
}