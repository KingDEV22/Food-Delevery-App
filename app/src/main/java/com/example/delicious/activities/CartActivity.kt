package com.example.delicious.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.adapter.RestaurantsMenuAdapter
import com.example.delicious.adapter.CartRecyclerAdapter
import com.example.delicious.database.CartEntities
import com.example.delicious.database.CartDatabase
import com.example.delicious.model.Menu
import com.example.delicious.util.ConnectionManager
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerCart: RecyclerView
    private lateinit var cartItemAdapter: CartRecyclerAdapter
    private var orderFoodList = ArrayList<Menu>()
    private lateinit var txtResName: TextView
    private lateinit var progressLayout: RelativeLayout
    private lateinit var relativeCart: RelativeLayout
    private lateinit var btnPlaceOrder: TextView
    private var resId: Int = 0
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        receiveData()
        btnPlaceOrder = findViewById(R.id.btnConfirmOrder)
        setupToolbar()
        setUpCartList()
        var sum = 0
        for (i in 0 until orderFoodList.size) {
            sum += orderFoodList[i].itemPrice as Int
        }
        val total = "Place Order(Total: Rs. $sum)"
        btnPlaceOrder.text = total
        val cartResName: TextView = findViewById(R.id.txtCartResName)
        cartResName.text = resName

        btnPlaceOrder.setOnClickListener {
            placeOrderRequest(sum.toString())
        }
    }

    private fun receiveData() {
        progressLayout = findViewById(R.id.progressLayout)
        relativeCart = findViewById(R.id.relativeCart)
        txtResName = findViewById(R.id.txtCartResName)
        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCartItems)
        val dbList: List<CartEntities> = GetItemsFromDBAsync(applicationContext).execute().get()
        for (element in dbList) {
            orderFoodList.addAll(
                    Gson().fromJson(element.foodItems, Array<Menu>::class.java).asList()
            )
        }
        if (orderFoodList.isEmpty()) {
            relativeCart.visibility = View.GONE
            progressLayout.visibility = View.VISIBLE
        } else {
            relativeCart.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
        }
        cartItemAdapter = CartRecyclerAdapter(orderFoodList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter
    }

    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<CartEntities>>() {
        private val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<CartEntities> {
            return db.orderDao().getAllOrders()
        }
    }

    private fun placeOrderRequest(sum: String) {
        if (ConnectionManager().isNetworkAvailable(this@CartActivity)) {
            val queue = Volley.newRequestQueue(this)
            val orderUrl = "http://13.235.250.119/v2/place_order/fetch_result/"
            val jsonParams = JSONObject()
            jsonParams.put("user_id",
                    this@CartActivity.getSharedPreferences("FoodRunner", Context.MODE_PRIVATE)
                            .getString("user_id", null)
                            as String
            )
            val bundle = intent.getBundleExtra("data")
            resId = bundle?.getInt("resId", 0) as Int
            jsonParams.put("restaurant_id", resId.toString())
            jsonParams.put("total_cost", sum)
            val foodArray = JSONArray()
            for (i in 0 until orderFoodList.size) {
                val foodId = JSONObject()
                foodId.put("food_item_id", orderFoodList[i].id)
                foodArray.put(i, foodId)
            }
            jsonParams.put("food", foodArray)

            val jsonObjectRequest =
                    object :
                            JsonObjectRequest(Method.POST, orderUrl, jsonParams, Response.Listener {

                                try {
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success")
                                    if (success) {
                                        ClearDBAsync(applicationContext, resId.toString()).execute()
                                                .get()
                                        RestaurantsMenuAdapter.CartEmpty = true
                                        val dialog = Dialog(
                                                this@CartActivity,
                                                android.R.style.Theme_NoTitleBar_Fullscreen
                                        )
                                        dialog.setContentView(R.layout.order_placed)
                                        dialog.show()
                                        dialog.setCancelable(false)
                                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                                        btnOk.setOnClickListener {
                                            dialog.dismiss()
                                            startActivity(
                                                    Intent(
                                                            this@CartActivity,
                                                            MainActivity::class.java
                                                    )
                                            )
                                            ActivityCompat.finishAffinity(this@CartActivity)
                                        }
                                    } else {
                                        relativeCart.visibility = View.VISIBLE
                                        Toast.makeText(
                                                this@CartActivity,
                                                "Some Error occurred",
                                                Toast.LENGTH_SHORT
                                        )
                                                .show()
                                    }
                                } catch (e: Exception) {
                                    relativeCart.visibility = View.VISIBLE
                                    e.printStackTrace()
                                }
                            }, Response.ErrorListener {
                                relativeCart.visibility = View.VISIBLE
                                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@CartActivity, " No Internet Connection found", Toast.LENGTH_SHORT).show()
        }

    }

    class ClearDBAsync(context: Context, private val resId: String) : AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        ClearDBAsync(applicationContext, resId.toString()).execute().get()
        RestaurantsMenuAdapter.CartEmpty = false
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        ClearDBAsync(applicationContext, resId.toString()).execute().get()
        RestaurantsMenuAdapter.CartEmpty = false
        super.onBackPressed()
    }
}





















