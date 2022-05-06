package com.example.delicious.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.delicious.R
import com.example.delicious.model.Menu

class CartRecyclerAdapter(private val cartList: ArrayList<Menu>, val context: Context) :
        RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CartViewHolder {
        val itemView = LayoutInflater.from(p0.context).inflate(R.layout.cart_single_row, p0, false)
        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, p1: Int) {
        val cartObject = cartList[p1]
        holder.itemName.text = cartObject.itemName
        val cost = "Rs. ${cartObject.itemPrice}"
        holder.itemCost.text = cost

    }


    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.txtCartItemName)
        val itemCost: TextView = view.findViewById(R.id.txtCartPrice)
    }
}