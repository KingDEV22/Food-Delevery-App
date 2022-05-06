package com.example.delicious.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.delicious.R
import com.example.delicious.adapter.AllRestaurantsAdapter
import com.example.delicious.database.FavresEntities
import com.example.delicious.database.CartDatabase
import com.example.delicious.model.Restaurants

class FavouriteRestaurants : Fragment() {
    private lateinit var recyclerFavouriteRestaurants: RecyclerView
    private lateinit var allRestaurantsAdapter: AllRestaurantsAdapter
    private var restaurantList = arrayListOf<Restaurants>()
    private lateinit var progressLayout: RelativeLayout
    private lateinit var relativeFavourites: RelativeLayout
    private lateinit var rlNoFavourites: RelativeLayout

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restraurants, container, false)
        relativeFavourites = view.findViewById(R.id.relativeFavorites)
        rlNoFavourites = view.findViewById(R.id.rlNoFavorites)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        setUpFavouriteFragment(view)
        return view
    }

    // function setting up the favourites list
    fun setUpFavouriteFragment(view: View) {
        recyclerFavouriteRestaurants = view.findViewById(R.id.recyclerFavourite)
        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            progressLayout.visibility = View.GONE
            relativeFavourites.visibility = View.GONE
            rlNoFavourites.visibility = View.VISIBLE
        } else {
            relativeFavourites.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
            rlNoFavourites.visibility = View.GONE
            for (i in backgroundList) {   //  we add the data to the restautrant list which is of data class restauarants like.
                restaurantList.add(
                        Restaurants(
                                i.id,
                                i.resName,
                                i.resRating,
                                i.resCostForTwo,
                                i.resImageUrl
                        )
                )
            }

            allRestaurantsAdapter = AllRestaurantsAdapter(activity as Context, restaurantList)
            val layoutManager = GridLayoutManager(activity as Context, 2)
            recyclerFavouriteRestaurants.layoutManager = layoutManager
            recyclerFavouriteRestaurants.itemAnimator = DefaultItemAnimator()
            recyclerFavouriteRestaurants.adapter = allRestaurantsAdapter
            recyclerFavouriteRestaurants.setHasFixedSize(true)
        }


    }

    // class responsible for bringing the data back from the the database
    class FavouritesAsync(context: Context) : AsyncTask<Void, Void, List<FavresEntities>>() {

        private val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): List<FavresEntities> {

            return db.favresDao().getAllRestaurants()
        }

    }
}

