package com.example.delicious.model

import org.json.JSONArray

data class OrderItemDetails(
        val orderId: Int,
        val resName: String,
        val orderDate: String,
        val foodItems: JSONArray
)