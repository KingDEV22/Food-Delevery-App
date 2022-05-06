package com.example.delicious.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CartEntities::class, FavresEntities::class], version = 1)
abstract class CartDatabase : RoomDatabase() {
    abstract fun favresDao(): favresDao
    abstract fun orderDao(): cartDao

}