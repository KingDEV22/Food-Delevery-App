package com.example.delicious.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class CartEntities(

        @PrimaryKey val resId: String,
        @ColumnInfo(name = "food_Items") val foodItems: String
)
