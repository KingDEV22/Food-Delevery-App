package com.example.delicious.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.util.ConnectionManager
import com.example.delicious.util.Constraints
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var btnReset: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var edtForgotMobile: EditText
    private lateinit var edtForgotEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btnReset = findViewById(R.id.btnReset)
        edtForgotEmail = findViewById(R.id.edtForgotEmail)
        edtForgotMobile = findViewById(R.id.edtForgotMobile)
        toolbar = findViewById(R.id.forgotToolbar)
        setUpToolbar()

        btnReset.setOnClickListener {
            val forgotMobileNumber = edtForgotMobile.text.toString()
            if (Constraints.validateMobile(forgotMobileNumber)) {
                edtForgotMobile.error = null
                if (Constraints.validateEmail(edtForgotEmail.text.toString())) {
                    forgotPasswordOtp(edtForgotMobile.text.toString(), edtForgotEmail.text.toString())
                } else {
                    edtForgotEmail.error = "Invalid Email"
                }
            } else {
                edtForgotMobile.error = "Invalid Mobile Number"
            }
        }
    }


    private fun forgotPasswordOtp(mobileNumber: String, emailAddress: String) {
        if (ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {
            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("email", emailAddress)

            val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                        try {
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                val firstTry = data.getBoolean("first_try")
                                if (firstTry) {
                                    val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                                    builder.setTitle("Information")
                                    builder.setMessage("Please check your registered Email for the OTP.")
                                    builder.setCancelable(false)
                                    builder.setPositiveButton("Ok") { _, _ ->
                                        val intent = Intent(
                                                this@ForgotPasswordActivity,
                                                ResetPasswordActivity::class.java
                                        )
                                        intent.putExtra("user_mobile", mobileNumber)
                                        startActivity(intent)
                                    }
                                    builder.create().show()
                                } else {
                                    val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                                    builder.setTitle("Information")
                                    builder.setMessage("Please refer to the previous email for the OTP.")
                                    builder.setCancelable(false)
                                    builder.setPositiveButton("Ok") { _, _ ->
                                        val intent = Intent(
                                                this@ForgotPasswordActivity,
                                                ResetPasswordActivity::class.java
                                        )
                                        intent.putExtra("user_mobile", mobileNumber)
                                        startActivity(intent)
                                    }
                                    builder.create().show()
                                }
                            } else {
                                Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        "Mobile number not registered!",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "Incorrect response error!!",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, Response.ErrorListener {
                        VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")
                        Toast.makeText(this@ForgotPasswordActivity, it.message, Toast.LENGTH_SHORT)
                                .show()
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
            Toast.makeText(this, "No Internet Connection found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Forgot Password"
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

