package com.example.delicious.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.util.ConnectionManager
import com.example.delicious.util.Constraints
import org.json.JSONObject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var edtOTP: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmNewPassword: EditText
    private lateinit var btnSubmit: Button
    private var mobileNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        edtOTP = findViewById(R.id.edtOTP)
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        btnSubmit = findViewById(R.id.btnSubmit)

        if (intent != null) {
            mobileNumber = intent.getStringExtra("user_mobile").toString()
        }

        btnSubmit.setOnClickListener {

            // again we nested if statements for the validations
            if (edtOTP.text.length == 4) {
                if (Constraints.validatePasswordLength(edtNewPassword.text.toString())) {
                    if (Constraints.matchPassword(
                                    edtNewPassword.text.toString(),
                                    edtConfirmNewPassword.text.toString()
                            )
                    ) {
                        sendOtp(
                                mobileNumber,
                                edtOTP.text.toString(),
                                edtNewPassword.text.toString()
                        )
                    } else {
                        Toast.makeText(
                                this@ResetPasswordActivity,
                                "Passwords do not match",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                            this@ResetPasswordActivity,
                            "Invalid Password",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this@ResetPasswordActivity, "Incorrect OTP", Toast.LENGTH_SHORT)
                        .show()
            }
        }


    }

    private fun sendOtp(mobileNumber: String, otp: String, newPassword: String) {
        if (ConnectionManager().isNetworkAvailable(this@ResetPasswordActivity)) {
            val queue = Volley.newRequestQueue(this)
            val resetUrl = "http://13.235.250.119/v2/reset_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", newPassword)
            jsonParams.put("otp", otp)

            val jsonObjectRequest =
                    object :
                            JsonObjectRequest(Request.Method.POST, resetUrl, jsonParams, Response.Listener {
                                try {
                                    val resetObject = it.getJSONObject("data")
                                    val success = resetObject.getBoolean("success")
                                    if (success) {
                                        val builder = AlertDialog.Builder(this@ResetPasswordActivity)
                                        builder.setTitle("Confirmation")
                                        builder.setMessage("Your password has been changed successfully")
                                        builder.setIcon(R.drawable.ic_action_success)
                                        builder.setCancelable(false)
                                        builder.setPositiveButton("Ok") { _, _ ->
                                            startActivity(
                                                    Intent(
                                                            this@ResetPasswordActivity,
                                                            LoginActivity::class.java
                                                    )
                                            )
                                            ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                                        }
                                        builder.create().show()
                                    } else {
                                        val error = resetObject.getString("errorMessage")
                                        Toast.makeText(
                                                this@ResetPasswordActivity,
                                                error,
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                            this@ResetPasswordActivity,
                                            "Incorrect Response!!",
                                            Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, Response.ErrorListener {
                                VolleyLog.e("Error::::", "post request fail! Error: ${it.message}")
                                Toast.makeText(this@ResetPasswordActivity, it.message, Toast.LENGTH_SHORT)
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

}

