package com.example.delicious.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.delicious.R
import com.example.delicious.activities.RestaurantMenuActivity
import com.example.delicious.database.CartDatabase
import com.example.delicious.database.FavresEntities
import com.example.delicious.model.Restaurants
import com.squareup.picasso.Picasso

class AllRestaurantsAdapter(val context: Context, var RestaurantList: ArrayList<Restaurants>) : RecyclerView.Adapter<AllRestaurantsAdapter.AllRestaurantsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllRestaurantsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_restraurants_recycler_single_row, parent, false)
        return AllRestaurantsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return RestaurantList.size
    }

    override fun onBindViewHolder(holder: AllRestaurantsViewHolder, position: Int) {

        // displaying the data present in restaurant list
        val Restaurants = RestaurantList[position]
        holder.txtRestaurantName.text = Restaurants.restaurantName
        val txtPricePerPerson = "Rs.${Restaurants.restaurantPrice}/person"
        holder.txtPrice.text = txtPricePerPerson
        holder.txtRating.text = Restaurants.restaurantRating
        Picasso.get().load(Restaurants.restaurantImageUrl).into(holder.imgRestaurantImage)

        holder.imgFav.setOnClickListener {
            val favresEntitiesEntity = FavresEntities(
                    Restaurants.restaurantId,
                    Restaurants.restaurantName,
                    Restaurants.restaurantRating,
                    Restaurants.restaurantPrice,
                    Restaurants.restaurantImageUrl
            )
// this code shows the changes if put the restaurant in the favourite list
            if (!DBAsyncTask(context, favresEntitiesEntity, 1).execute().get()) {
                val async =
                        DBAsyncTask(context, favresEntitiesEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.imgFav.setImageResource(R.drawable.ic_favourited)
                }
            } else {
                val async = DBAsyncTask(context, favresEntitiesEntity, 3).execute()
                val result = async.get()
                if (result) {
                    holder.imgFav.setImageResource(R.drawable.ic_favourites)
                }
            }
        }
// this code shows whether the restauraant is stored in fovourtes or not using heart img
        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()
        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(Restaurants.restaurantId.toString())) {
            holder.imgFav.setImageResource(R.drawable.ic_favourited)
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_favourites)
        }


// if we click on the cardview we go to menu activity
        holder.cardRestaurant.setOnClickListener {
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("id", Restaurants.restaurantId as Int)
            intent.putExtra("name", Restaurants.restaurantName)

            context.startActivity(intent)
        }
    }

    class AllRestaurantsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val imgRestaurantImage: ImageView = view.findViewById(R.id.imgRestaurantImage)
        val cardRestaurant: CardView = view.findViewById(R.id.cardRestaurant)
        val imgFav: ImageView = view.findViewById(R.id.imgFav)

    }

    class DBAsyncTask(context: Context, val favresEntities: FavresEntities, val mode: Int) :
            AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            /*
            Mode 1 -> Check DB if the book is favourite or not
            Mode 2 -> Save the book into DB as favourite
            Mode 3 -> Remove the favourite book
            */

            when (mode) {

                1 -> {
                    val res: FavresEntities? =
                            db.favresDao().getRestaurantById(favresEntities.id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    db.favresDao().insertRestaurant(favresEntities)
                    db.close()
                    return true
                }

                3 -> {
                    db.favresDao().deleteRestaurant(favresEntities)
                    db.close()
                    return true
                }
            }

            return false
        }

    }

    class GetAllFavAsyncTask(
            context: Context
    ) :
            AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, CartDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.favresDao().getAllRestaurants()
            val listOfFavid = arrayListOf<String>()
            for (i in list) {
                listOfFavid.add(i.id.toString())
            }
            return listOfFavid
        }
    }
}


