package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.todoapp.databinding.ActivityRegisterBinding
import com.example.todoapp.utilService.SharedPreferenceClass
import com.example.todoapp.utilService.UtilService
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerBtn: TextView
    private lateinit var loginBtn: TextView
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var nameET: EditText
    private lateinit var nameText: String
    private lateinit var emailText: String
    private lateinit var passText: String
    private lateinit var utilService: UtilService
    private lateinit var progressBar: ProgressBar
    private val TAG = "com.example.todoapp.RegisterActivity"
    private lateinit var sharedPreferenceClass: SharedPreferenceClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameET = binding.nameET
        emailET = binding.emailET
        passwordET = binding.passwordET
        loginBtn = binding.loginBtn
        registerBtn = binding.registerBtn
        progressBar = binding.progressBar
        utilService = UtilService()
        sharedPreferenceClass = SharedPreferenceClass(this)

        loginBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        registerBtn.setOnClickListener {view ->
            nameText = nameET.text.toString()
            emailText = emailET.text.toString()
            passText = passwordET.text.toString()
            if (validate(view)) {
                Log.d(TAG,"$nameText $emailText $passText")
                registerUser(view)
            }
        }

        loginBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun registerUser(view: View?) {
        progressBar.visibility = View.VISIBLE

        val params = HashMap<String, String>()
        params["username"] = nameText
        params["email"] = emailText
        params["password"] = passText

        val api = "https://todoapplicationxyz.herokuapp.com/api/todo/auth/register"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            api, JSONObject(params as Map<*, *>?), responseListener, errorListener) {
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


        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    var responseListener: Response.Listener<JSONObject> =
        Response.Listener<JSONObject> { response ->
            try {
                if (response.getBoolean("success")) {
                    val token = response.getString("token")
                    Toast.makeText(this,"Successfully register with token $token",Toast.LENGTH_SHORT).show()
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
            if (error != null && error is ServerError) {
                try {
                    val res = String(response.data)
                    Log.e(TAG, "${response.data}")
                    val obj = JSONObject(res)
                    Toast.makeText(this@RegisterActivity, obj.getString("msg"), Toast.LENGTH_SHORT)
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
            }
        }



    private fun validate(view: View) : Boolean {
        val isValid: Boolean
        if (!TextUtils.isEmpty(nameText)) {
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
        } else {
            isValid = false
            utilService.showSnackBar(view, "Please enter name")
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