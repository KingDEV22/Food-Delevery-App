package com.example.delicious.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.Volley
import com.example.delicious.R
import com.example.delicious.fragments.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView
    private var previousMenuItem: MenuItem? = null
    private lateinit var preferences: com.example.delicious.util.Preferences
    private lateinit var sharedPrefs: SharedPreferences

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = com.example.delicious.util.Preferences(this@MainActivity)
        sharedPrefs = this@MainActivity.getSharedPreferences(
                preferences.PREF_NAME,
                preferences.PRIVATE_MODE
        )

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)

        setUpToolbar()
        openAllRestraunts()
        val actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener {
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 100)

            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    AllRestaurents()
                            ).commit()
                    supportActionBar?.title = "All Restaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    MyProfile()
                            ).commit()
                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.favourite -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    FavouriteRestaurants()
                            ).commit()
                    supportActionBar?.title = "My Favourite Restraunts"
                    drawerLayout.closeDrawers()
                }
                R.id.OrderHistory -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    OrderHistory()
                            ).commit()
                    supportActionBar?.title = "My order history"
                    drawerLayout.closeDrawers()
                }
                R.id.logout -> {
//                  if the user wishes to log out we show a alert dialog box
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                            .setMessage("Are you sure you want exit?")
                            .setPositiveButton("Yes") { _, _ ->
                                preferences.setLogin(false)
                                sharedPrefs.edit().clear().apply()
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                                ActivityCompat.finishAffinity(this)
                            }
                            .setNegativeButton("No") { _, _ ->
                                openAllRestraunts()
                            }
                            .create()
                            .show()
                    drawerLayout.closeDrawers()
                }
                R.id.faqs -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    FAQs()
                            ).commit()
                    supportActionBar?.title = "Frequently Asked Questions?"
                    drawerLayout.closeDrawers()
                }
            }
            return@setNavigationItemSelectedListener true
        }
        val headerView = LayoutInflater.from(this@MainActivity).inflate(R.layout.drawer_header, null)
        val txtHeaderName: TextView = headerView.findViewById(R.id.txtHeaderName)
        val txtHeaderMobile: TextView = headerView.findViewById(R.id.txtHeaderMobile)
        val imgPersonHeader: ImageView = headerView.findViewById(R.id.imgPersonHeader)
        txtHeaderName.text = sharedPrefs.getString("user_name", null)
        val phoneText = "+91-${sharedPrefs.getString("user_mobile_number", null)}"
        txtHeaderMobile.text = phoneText
        navigationView.addHeaderView(headerView)

        imgPersonHeader.setOnClickListener {
            val myProfile = MyProfile()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, myProfile)
            transaction.commit()
            supportActionBar?.title = "My profile"
            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 100)
            navigationView.setCheckedItem(R.id.profile)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = " Toolbar Title "
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// for displaying the default icon
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            val mPendingRunnable = Runnable { drawerLayout.openDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 100)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openAllRestraunts() {
        val fragment = AllRestaurents()
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.frame, fragment)
        fragmentManager.commit()
        supportActionBar?.title = " All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)
        when (frag) {
            !is AllRestaurents -> openAllRestraunts()
            else -> {
                Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                ActivityCompat.finishAffinity(this@MainActivity)
            }
        }
    }
}