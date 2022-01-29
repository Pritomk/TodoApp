package com.example.todoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.todoapp.databinding.ActivityLoginBinding
import com.example.todoapp.utilService.SharedPreferenceClass
import com.example.todoapp.utilService.UtilService
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class LoginActivity : AppCompatActivity() {

    private lateinit var createAccountBtn: TextView
    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailText: String
    private lateinit var passText: String
    private lateinit var loginBtn: TextView
    private lateinit var utilService: UtilService
    private lateinit var progressBar: ProgressBar
    private val TAG = "com.example.todoapp.LoginActivity"
    private lateinit var sharedPreferenceClass: SharedPreferenceClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginBtn = binding.btnLogin
        createAccountBtn = binding.createAccountBtn
        progressBar = binding.progressBar
        utilService = UtilService()
        sharedPreferenceClass = SharedPreferenceClass(this)

        createAccountBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener { view->
            emailText = binding.emailET.text.toString()
            passText = binding.passwordET.text.toString()

            if (validate(view)) {
                loginUser(view)
            }

        }
    }

    private fun loginUser(view: View?) {
        progressBar.visibility = View.VISIBLE

        val params = HashMap<String, String>()
        params["email"] = emailText
        params["password"] = passText

        val api = "https://todoapplicationxyz.herokuapp.com/api/todo/auth/login"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            api, JSONObject(params as Map<*, *>?), responseListener,errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return params
            }
        }

        val socketTime = 3000
        val policy = DefaultRetryPolicy(socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        jsonObjectRequest.retryPolicy = policy

        val responseRequest = Volley.newRequestQueue(this)
        responseRequest.add(jsonObjectRequest)
    }

    var responseListener: Response.Listener<JSONObject> =
        Response.Listener<JSONObject> { response ->
            try {
                if (response.getBoolean("success")) {
                    val token = response.getString("token")
                    Toast.makeText(this,"Successfully login with token $token", Toast.LENGTH_SHORT).show()
                    sharedPreferenceClass.setValueString("token", token)
                    startActivity(Intent(this,MainActivity::class.java).putExtra("token",token))
                }
                progressBar.visibility = View.GONE
            } catch (e: JSONException) {
                Log.e(TAG,"Response error ${e.message}")
                progressBar.visibility = View.GONE
                e.printStackTrace()
            }
        }

    var errorListener: Response.ErrorListener =
        Response.ErrorListener { error ->
            val response = error.networkResponse
            Log.e(TAG, "Error listener")
            if (response != null && error is ServerError) {
                try {
                    progressBar.visibility = View.GONE
                    val res = String(response.data)
                    Log.e(TAG, "${response.data}")
                    Log.e(TAG, "Error in try block")
                    val obj = JSONObject(res)
                    Toast.makeText(this, obj.getString("msg"), Toast.LENGTH_SHORT)
                        .show()
                    progressBar.visibility = View.GONE
                } catch (je: JSONException) {
                    Log.e(TAG, "JSONException ${je.message}")
                    je.printStackTrace()
                    progressBar.visibility = View.GONE
                } catch (je: UnsupportedEncodingException) {
                    Log.e(TAG, "JSONException ${je.message}")
                    je.printStackTrace()
                    progressBar.visibility = View.GONE
                }
            } else {
                progressBar.visibility = View.GONE
                Log.e(TAG,"$response")
            }
        }

    private fun validate(view: View): Boolean {
        val isValid: Boolean
        if (!TextUtils.isEmpty(emailText)) {
            if (!TextUtils.isEmpty(passText)) {
                isValid = true
            } else {
                isValid = false
                utilService.showSnackBar(view, "Please enter password")
            }
        } else {
            isValid = false
            utilService.showSnackBar(view, "Please enter email")
        }

        return isValid
    }

    override fun onStart() {
        super.onStart()

        val todoPref = getSharedPreferences("user_todo", MODE_PRIVATE)
        if (todoPref.contains("token")) {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}