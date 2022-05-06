package com.example.delicious.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.adapter.RestaurantsMenuAdapter
import com.example.delicious.database.CartDatabase
import com.example.delicious.database.CartEntities
import com.example.delicious.model.Menu
import com.example.delicious.util.ConnectionManager
import com.google.gson.Gson

class RestaurantMenuActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerRestaurantMenu: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLayout: RelativeLayout

    val menuInfoList = arrayListOf<Menu>()
    val orderFoodList = arrayListOf<Menu>()
    lateinit var btnProceedToCart: Button
    lateinit var recyclerAdapter: RestaurantsMenuAdapter

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: Int? = 0
        var resName: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)
        toolbar = findViewById(R.id.toolbar)
        btnProceedToCart = findViewById(R.id.btnGoToCart)
        btnProceedToCart.visibility = View.GONE
        recyclerRestaurantMenu = findViewById(R.id.recyclerRestaurantsMenu)
        layoutManager = LinearLayoutManager(baseContext) as RecyclerView.LayoutManager
        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        // here we get  back the values we passed earlier
        if (intent != null) {
            resId = intent.getIntExtra("id", 0)
            resName = intent.getStringExtra("name").toString()
        }
        if (resId == 0) {
            Toast.makeText(this@RestaurantMenuActivity, "same error", Toast.LENGTH_SHORT).show()
        }
        setUpToolbar(resName)

        setUpRestaurantMenu()
        btnProceedToCart.setOnClickListener {
            proceedToCart(resId)
        }
    }

    // function for proocedding to cart activity
    fun proceedToCart(resId: Int?) {

        // gson is used to convert the array data into simpler strings which will be useful to store in database
        val gson = Gson()
        val menuItems = gson.toJson(orderFoodList) // data in this array is converted to string
        val async = ItemsInCart(baseContext, resId.toString(), menuItems, 1).execute()
        val result = async.get()
        if (result) {
            val data = Bundle()
            data.putInt("resId", resId as Int)
            data.putString("resName", resName)
            val intent = Intent(this@RestaurantMenuActivity, CartActivity::class.java)
            intent.putExtra("data", data)// wealso send some data to cart activity as it might be useful there
            startActivity(intent)
        } else {
            Toast.makeText((this@RestaurantMenuActivity), "Some unexpected error", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // function for setting up the menulist
    fun setUpRestaurantMenu() {
        if (ConnectionManager().isNetworkAvailable(this@RestaurantMenuActivity)) {
            val queue =
                    Volley.newRequestQueue(this@RestaurantMenuActivity)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"
            val jsonObjectRequest =
                    object : JsonObjectRequest(Request.Method.GET, url, null,
                            Response.Listener {
                                progressLayout.visibility = View.GONE
                                try {
                                    val menuJsonObject = it.getJSONObject("data")
                                    val success = menuJsonObject.getBoolean("success")
                                    if (success) {
                                        val data = menuJsonObject.getJSONArray("data")
                                        for (i in 0 until data.length()) {
                                            val menuJsonObject = data.getJSONObject(i)
                                            val menuObject = Menu(
                                                    menuJsonObject.getString("id"),
                                                    menuJsonObject.getString("name"),
                                                    menuJsonObject.getString("cost_for_one").toInt()
                                            )
                                            menuInfoList.add(menuObject)
                                            recyclerAdapter =
                                                    RestaurantsMenuAdapter(this@RestaurantMenuActivity,
                                                            menuInfoList,
                                                            object : RestaurantsMenuAdapter.OnItemClickListener {
                                                                override fun onAddItemClick(menuItem: Menu) {
                                                                    orderFoodList.add(menuItem)
                                                                    if (orderFoodList.size > 0) {
                                                                        btnProceedToCart.visibility =
                                                                                View.VISIBLE
                                                                        RestaurantsMenuAdapter.CartEmpty = false
                                                                    }
                                                                }

                                                                override fun onRemoveItemClick(menuItem: Menu) {
                                                                    orderFoodList.remove(menuItem)
                                                                    if (orderFoodList.isEmpty()) {
                                                                        btnProceedToCart.visibility = View.GONE
                                                                        RestaurantsMenuAdapter.CartEmpty = true
                                                                    }
                                                                }
                                                            })
                                            recyclerRestaurantMenu.adapter = recyclerAdapter
                                            recyclerRestaurantMenu.layoutManager = layoutManager
                                        }
                                    } else {
                                        Toast.makeText(
                                                this@RestaurantMenuActivity,
                                                " Error has occured",
                                                Toast.LENGTH_LONG
                                        )
                                                .show()
                                    }
                                    println("Response is $it")

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }, Response.ErrorListener {
                        println(" Error is $it")

                    }) {
                        override fun getHeaders(): MutableMap<String, String> {

                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/json"
                            headers["token"] = "b312a26fe9bf61"
                            return headers
                        }
                    }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = android.app.AlertDialog.Builder(this)
            dialog.setTitle(" Error ")
            dialog.setMessage(" Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)// open settings
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this) // this code is used to finish the app at any moment
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun setUpToolbar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        if (!RestaurantsMenuAdapter.CartEmpty) {
            val builder = AlertDialog.Builder(this@RestaurantMenuActivity)
            builder.setTitle("Confirmation")
                    .setMessage("Going back will reset your cart items. Do you still want to proceed?")
                    .setPositiveButton("Yes") { _, _ ->
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                        RestaurantsMenuAdapter.CartEmpty = true
                    }
                    .setNegativeButton("No") { _, _ ->
                    }
                    .create()
                    .show()
        } else {
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // class responsible for handling opeartion on data stored in database
    class ItemsInCart(
            context: Context,
            private val restaurantId: String,
            private val orderItems: String,
            private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(CartEntities(restaurantId, orderItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(CartEntities(restaurantId, orderItems))
                    db.close()
                    return true
                }
            }
            return false
        }

    }
}
