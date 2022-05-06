package com.example.delicious.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.adapter.AllRestaurantsAdapter
import com.example.delicious.model.Restaurants
import com.example.delicious.util.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import java.lang.Exception
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class AllRestaurents : Fragment() {
    private lateinit var recyclerRestaurants: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var previousMenuItem: MenuItem? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLayout: RelativeLayout
    lateinit var recyclerAdapter: AllRestaurantsAdapter
    val restaurantInfoList = arrayListOf<Restaurants>()
    lateinit var radioButtonView: View

    private var priceComparator = Comparator<Restaurants> { res1, res2 ->
        val priceOne = res1.restaurantPrice.toInt()
        val priceTwo = res2.restaurantPrice.toInt()
        if (priceOne.compareTo(priceTwo) == 0) {
            ratingComparator.compare(res1, res2)
        } else {
            priceOne.compareTo(priceTwo)
        }
    }
    private var ratingComparator = Comparator<Restaurants> { res1, res2 ->
        val ratingOne = res1.restaurantRating
        val ratingTwo = res2.restaurantRating
        if (ratingOne.compareTo(ratingTwo) == 0) {
            val costOne = res1.restaurantPrice.toInt()
            val costTwo = res2.restaurantPrice.toInt()
            costOne.compareTo(costTwo)
        } else {
            ratingOne.compareTo(ratingTwo)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_restraurants, container, false)
        recyclerRestaurants = view.findViewById(R.id.recyclerAllRestaurants)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        setUPRestaurant()
        return view
    }

    // function for setting up the restaurant list
    private fun setUPRestaurant() {
        layoutManager = GridLayoutManager(activity as Context, 2)
        setHasOptionsMenu(true)
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)// volley is a library
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/ "
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener {
                        progressLayout.visibility = View.GONE

                        try {


                            val jsonObject = it.getJSONObject("data")
                            val success = jsonObject.getBoolean("success")
                            if (success) {

                                val data = jsonObject.getJSONArray("data")
                                for (i in 0 until data.length()) {
                                    val restaurantJsonObject = data.getJSONObject(i)
                                    val restaurantObject = Restaurants(
                                            restaurantJsonObject.getString("id").toInt(),
                                            restaurantJsonObject.getString("name"),
                                            restaurantJsonObject.getString("rating"),
                                            restaurantJsonObject.getString("cost_for_one"),
                                            restaurantJsonObject.getString("image_url")
                                    )
                                    restaurantInfoList.add(restaurantObject)// parsing the data received to the array
                                    recyclerAdapter =
                                            AllRestaurantsAdapter(activity as Context, restaurantInfoList)
                                    recyclerRestaurants.adapter = recyclerAdapter
                                    recyclerRestaurants.layoutManager = layoutManager

                                }
                            } else {
                                Toast.makeText(
                                        activity as Context,
                                        " Error has occurred",
                                        Toast.LENGTH_LONG
                                )
                                        .show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener {
                // Here we will handle the error
                println(" Error is $it")

            }) {
                override fun getHeaders(): MutableMap<String, String> {

                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "ad5493c1644a7d"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)

        } else {
            val dialog = android.app.AlertDialog.Builder(activity as Context)
            dialog.setTitle(" Error ")
            dialog.setMessage(" Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->

                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)// open settings
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                radioButtonView = View.inflate(
                        context,
                        R.layout.sort_radio_button,
                        null
                )
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                        .setTitle("Sort By?")
                        .setView(radioButtonView)
                        .setPositiveButton("OK") { _, _ ->
                            if (radioButtonView.radio_high_to_low.isChecked) {
                                Collections.sort(restaurantInfoList, priceComparator)
                                restaurantInfoList.reverse()
                                recyclerAdapter.notifyDataSetChanged()
                            }
                            if (radioButtonView.radio_low_to_high.isChecked) {
                                Collections.sort(restaurantInfoList, priceComparator)
                                recyclerAdapter.notifyDataSetChanged()
                            }
                            if (radioButtonView.radio_rating.isChecked) {
                                Collections.sort(restaurantInfoList, ratingComparator)
                                restaurantInfoList.reverse()
                                recyclerAdapter.notifyDataSetChanged()
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                        }
                        .create()
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}

