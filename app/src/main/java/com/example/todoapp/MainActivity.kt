package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.ui.todo.TodoFragment
import com.example.todoapp.utilService.SharedPreferenceClass
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userImage: ImageView
    private lateinit var sharedPreferencesClass: SharedPreferenceClass
    private val TAG = "com.example.todoapp.MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_container)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_todo, R.id.nav_finished_task
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPreferencesClass = SharedPreferenceClass(this)

        getUserProfile()
    }

    private fun getUserProfile() {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/auth"
        val token = sharedPreferencesClass.getValueString("token")

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null,{ response->
            userName = findViewById(R.id.navHeaderUserName)
            userEmail = findViewById(R.id.navHeaderUserEmail)
            userImage = findViewById(R.id.navHeaderIV)
            Log.d(TAG, "$response")
            if (response.getBoolean("success")) {
                val userObj = response.getJSONObject("user")
                userName.text = userObj.getString("username")
                userEmail.text = userObj.getString("email")
                Log.d(TAG, userObj.getString("avatar"))
                Glide.with(this).load(userObj.getString("avatar")).circleCrop().into(userImage)

            }

        },{ error->
            Log.e(TAG, "${error.message}")
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "$token"
                return params
            }
        }

        val socketTime = 30000;
        val policy = DefaultRetryPolicy(socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_share-> {
//                val sharingIntent = Intent(Intent.ACTION_SEND)
//                sharingIntent.type = "plain/text"
//
//                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Checkout this app")
//                startActivity(Intent.createChooser(sharingIntent, "Share via"))
                logout()
                return true
            }
            R.id.refresh_menu-> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_container, TodoFragment()).commit()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        sharedPreferencesClass.clear()
        Log.d(TAG,"Executed main activity ${sharedPreferencesClass.getValueString("token")}")
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}