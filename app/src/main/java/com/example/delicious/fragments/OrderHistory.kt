package com.example.delicious.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.adapter.OrderHistoryAdapter
import com.example.delicious.model.OrderItemDetails
import com.example.delicious.util.ConnectionManager

class OrderHistory : Fragment() {
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var orderHistoryList = ArrayList<OrderItemDetails>()
    private lateinit var linearOrders: LinearLayout
    private lateinit var rlNoOrders: RelativeLayout
    private lateinit var recyclerOrderHistory: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rlLoading: RelativeLayout
    private var userId = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        linearOrders = view.findViewById(R.id.linearOrders)
        rlNoOrders = view.findViewById(R.id.rlNoOrders)
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        rlLoading = view?.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        sharedPreferences =
                (activity as Context).getSharedPreferences("FoodRunner", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null) as String

        sendHistoryRequest(userId)

        return view
    }

    private fun sendHistoryRequest(userId: String) {
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val historyUrl = "http://13.235.250.119/v2/orders/fetch_result/"

            val jsonObjectRequest = object :
                    JsonObjectRequest(Method.GET, historyUrl + userId, null, Response.Listener {
                        rlLoading.visibility = View.GONE
                        try {
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                val resArray = data.getJSONArray("data")
                                if (resArray.length() == 0) {
                                    linearOrders.visibility = View.GONE
                                    rlNoOrders.visibility = View.VISIBLE
                                } else {
                                    for (i in 0 until resArray.length()) {
                                        val orderObject = resArray.getJSONObject(i)
                                        val foodItems = orderObject.getJSONArray("food_items")
                                        val orderDetails = OrderItemDetails(
                                                orderObject.getInt("order_id"),
                                                orderObject.getString("restaurant_name"),
                                                orderObject.getString("order_placed_at"),
                                                foodItems
                                        )
                                        orderHistoryList.add(orderDetails)
                                        if (orderHistoryList.isEmpty()) {
                                            linearOrders.visibility = View.GONE
                                            rlNoOrders.visibility = View.VISIBLE
                                        } else {
                                            linearOrders.visibility = View.VISIBLE
                                            rlNoOrders.visibility = View.GONE
                                            if (activity != null) {
                                                orderHistoryAdapter = OrderHistoryAdapter(
                                                        activity as Context,
                                                        orderHistoryList
                                                )
                                                val mLayoutManager =
                                                        LinearLayoutManager(activity as Context)
                                                recyclerOrderHistory.layoutManager = mLayoutManager
                                                recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                                recyclerOrderHistory.adapter = orderHistoryAdapter
                                            } else {
                                                queue.cancelAll(this::class.java.simpleName)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener {
                        Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
                    }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "ad5493c1644a7d"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(activity, "Make Sure you are connected to Internet", Toast.LENGTH_LONG).show()
        }
    }

}
