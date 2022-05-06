package com.example.delicious.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface cartDao {
    @Insert
    fun insertOrder(cartEntities: CartEntities)

    @Delete
    fun deleteOrder(cartEntities: CartEntities)

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<CartEntities>

    @Query("DELETE FROM orders WHERE resId = :resId")
    fun deleteOrders(resId: String)
}