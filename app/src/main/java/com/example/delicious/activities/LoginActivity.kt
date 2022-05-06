package com.example.delicious.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.util.ConnectionManager
import com.example.delicious.util.Constraints
import com.example.delicious.util.Preferences
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var edtLogMobileNumber: EditText
    private lateinit var edtLogPassword: EditText
    private lateinit var btnLogin: TextView
    private lateinit var txtSignUp: TextView
    private lateinit var txtForgotPassword: TextView
    private lateinit var preferences: Preferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferences = Preferences(this)
        sharedPreferences =
                this.getSharedPreferences(preferences.PREF_NAME, preferences.PRIVATE_MODE)

        txtSignUp = findViewById(R.id.txtSignUp)


        txtSignUp.setOnClickListener {
            val intent = Intent(baseContext, RegistrationActivity::class.java)
            startActivity(intent)
        }
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        val text = getString(R.string.forgot_password)
        val content = SpannableString(text)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        txtForgotPassword.text = content


        txtForgotPassword.setOnClickListener {
            val intent = Intent(baseContext, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        edtLogMobileNumber = findViewById(R.id.edtLogMobileNumber)
        edtLogPassword = findViewById(R.id.edtLogPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            logInUser()
        }
    }

    private fun logInUser() {

        val txtLogMobileNumber: String = edtLogMobileNumber.text.toString()
        val txtLogPassword: String = edtLogPassword.text.toString()
        if (Constraints.validateMobile(edtLogMobileNumber.text.toString()) && Constraints.validatePasswordLength(edtLogPassword.text.toString())) {

            // here we are checking for internet connection
            if (ConnectionManager().isNetworkAvailable(this@LoginActivity)) {

                val queue = Volley.newRequestQueue(this@LoginActivity)// volley is a library
                val url = "http://13.235.250.119/v2/login/fetch_result "
                val jsonParams = JSONObject()

                jsonParams.put("mobile_number", txtLogMobileNumber)
                jsonParams.put("password", txtLogPassword)

                val jsonObjectRequest =
                        object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                            try {
                                val loginJsonObject = it.getJSONObject("data")
                                val success = loginJsonObject.getBoolean("success")
                                if (success) {


                                    val response = loginJsonObject.getJSONObject("data")
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

                                    // after saving we go to main or home activity
                                    startActivity(
                                            Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                            )
                                    )
                                    finish()
                                } else {
                                    val message: String = loginJsonObject.getString("errorMessage")
                                    Toast.makeText(
                                            this@LoginActivity,
                                            "$message ",
                                            Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, Response.ErrorListener {
                            println("error is $it")

                            Toast.makeText(
                                    this@LoginActivity,
                                    "Some error has occurred",
                                    Toast.LENGTH_SHORT
                            ).show()
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

                // if there is no internet connection available we show a alert dialog box

                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(" Error ")
                dialog.setMessage(" Internet Connection is not Found")
                dialog.setPositiveButton("Open Settings") { _, _ ->

                    // open settings
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    // this code is used to finish the app at any moment
                    ActivityCompat.finishAffinity(this)
                }
                dialog.create()
                dialog.show()
            }
        } else {
            btnLogin.visibility = View.VISIBLE
            txtForgotPassword.visibility = View.VISIBLE
            txtSignUp.visibility = View.VISIBLE
            Toast.makeText(this@LoginActivity, "Invalid Number or Password", Toast.LENGTH_SHORT)
                    .show()

        }
    }
}
