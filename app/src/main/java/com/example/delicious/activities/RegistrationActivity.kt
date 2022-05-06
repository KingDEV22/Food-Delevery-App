package com.example.delicious.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.util.ConnectionManager
import com.example.delicious.util.Constraints
import com.example.delicious.util.Preferences
import org.json.JSONObject
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var edtRegisterName: EditText
    private lateinit var edtEmailAddress: EditText
    private lateinit var edtMobileNumber: EditText
    private lateinit var edtDeliveryAddress: EditText
    private lateinit var edtRegPassword: EditText
    private lateinit var edtRegConfirmPassword: EditText
    private lateinit var btnRegister: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        toolbar = findViewById(R.id.register_toolbar)
        setUpToolbar()

        edtRegisterName = findViewById(R.id.edtRegisterName)
        edtEmailAddress = findViewById(R.id.edtEmailAddress)
        edtMobileNumber = findViewById(R.id.edtMobileNumber)
        edtDeliveryAddress = findViewById(R.id.edtDeliveryAddress)
        edtRegPassword = findViewById(R.id.edtRegPassword)
        edtRegConfirmPassword = findViewById(R.id.edtRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        preferences = Preferences(this@RegistrationActivity)
        sharedPreferences = this@RegistrationActivity.getSharedPreferences(
                preferences.PREF_NAME,
                preferences.PRIVATE_MODE
        )


        btnRegister.setOnClickListener() {

            if (Constraints.validateNameLength(edtRegisterName.text.toString())) {
                edtRegisterName.error = null // for showing a visual error
                if (Constraints.validateEmail(edtEmailAddress.text.toString())) {
                    edtEmailAddress.error = null
                    if (Constraints.validateMobile(edtMobileNumber.text.toString())) {
                        edtMobileNumber.error = null
                        if (Constraints.validatePasswordLength(edtRegPassword.text.toString())) {
                            edtRegPassword.error = null
                            if (Constraints.matchPassword(
                                            edtRegPassword.text.toString(),
                                            edtRegConfirmPassword.text.toString()
                                    )
                            ) {
                                edtRegPassword.error = null
                                edtRegConfirmPassword.error = null
                                registerUser()

                            } else {
                                edtRegPassword.error = "Passwords don't match"
                                edtRegConfirmPassword.error = "Passwords don't match"
                                Toast.makeText(this@RegistrationActivity, "Passwords don't match", Toast.LENGTH_SHORT)
                                        .show()
                            }
                        } else {
                            edtRegPassword.error = "Password should be more than or equal 4 digits"
                            Toast.makeText(
                                    this@RegistrationActivity,
                                    "Password should be more than or equal 4 digits",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        edtMobileNumber.error = "Invalid Mobile number"
                        Toast.makeText(this@RegistrationActivity, "Invalid Mobile number", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    edtEmailAddress.error = "Invalid Email"
                    Toast.makeText(this@RegistrationActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
                }
            } else {
                edtRegisterName.error = "Invalid Name"
                Toast.makeText(this@RegistrationActivity, "Invalid Name", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun registerUser() {

        val txtRegisterName = edtRegisterName.text.toString()
        val txtEmailAddress = edtEmailAddress.text.toString()
        val txtMobileNumber = edtMobileNumber.text.toString()
        val txtDeliveryAddress = edtDeliveryAddress.text.toString()
        val txtRegPassword = edtRegPassword.text.toString()

        if (ConnectionManager().isNetworkAvailable(this@RegistrationActivity)) {

            val queue = Volley.newRequestQueue(this@RegistrationActivity)
            val url = "http://13.235.250.119/v2/register/fetch_result"
            val jsonParams = JSONObject()
            jsonParams.put("name", txtRegisterName);
            jsonParams.put("mobile_number", txtMobileNumber)
            jsonParams.put("password", txtRegPassword)
            jsonParams.put("address", txtDeliveryAddress)
            jsonParams.put("email", txtEmailAddress)

            val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                        try {

                            val registerJsonObject = it.getJSONObject("data")
                            val success = registerJsonObject.getBoolean("success")
                            if (success) {
                                val response = registerJsonObject.getJSONObject("data")
                                sharedPreferences.edit()
                                        .putString("user_id", response.getString("user_id")).apply()
                                sharedPreferences.edit()
                                        .putString("user_name", response.getString("name")).apply()
                                sharedPreferences.edit()
                                        .putString(
                                                "user_mobile_number",
                                                response.getString("mobile_number")
                                        )
                                        .apply()
                                sharedPreferences.edit()
                                        .putString("user_address", response.getString("address"))
                                        .apply()
                                sharedPreferences.edit()
                                        .putString("user_email", response.getString("email")).apply()
                                preferences.setLogin(true)
                                val intent = Intent(baseContext, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                val message: String = registerJsonObject.getString("errorMessage")
                                Toast.makeText(
                                        this@RegistrationActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                ).show()

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener {
                        println("error is $it")

                        Toast.makeText(this@RegistrationActivity, "Some error has occurred", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No Internet Connection found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Registration"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            val intent = Intent(baseContext, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}
