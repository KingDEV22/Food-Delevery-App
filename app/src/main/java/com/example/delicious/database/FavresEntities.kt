package com.example.delicious.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = " Favourite Restaurants")
data class
FavresEntities(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name") val resName: String,
        @ColumnInfo(name = "rating") val resRating: String,
        @ColumnInfo(name = "cost_for_two") val resCostForTwo: String,
        @ColumnInfo(name = "image_url") val resImageUrl: String
)