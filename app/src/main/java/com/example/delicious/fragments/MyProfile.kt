package com.example.delicious.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.delicious.R

class MyProfile : Fragment() {

    private lateinit var txtUserName: TextView
    private lateinit var txtPhoneNumber: TextView
    private lateinit var txtUserAddress: TextView
    private lateinit var txtUserEmail: TextView
    private lateinit var profilePreferences: SharedPreferences

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)
        profilePreferences = (activity as FragmentActivity).getSharedPreferences("FoodRunner", Context.MODE_PRIVATE)
        txtUserName = view.findViewById(R.id.txtUserName)
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber)
        txtUserAddress = view.findViewById(R.id.txtAddress)
        txtUserEmail = view.findViewById(R.id.txtEmail)
        txtUserName.text = profilePreferences.getString("user_name", null)
        val phoneText = "+91-${profilePreferences.getString("user_mobile_number", null)}"
        txtPhoneNumber.text = phoneText
        txtUserEmail.text = profilePreferences.getString("user_email", null)
        val address = profilePreferences.getString("user_address", null)
        txtUserAddress.text = address

        return view
    }
}
