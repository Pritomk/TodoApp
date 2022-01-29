package com.example.todoapp.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.todoapp.adapters.FinishedItemClickListener
import com.example.todoapp.adapters.FinishedTaskAdapter
import com.example.todoapp.databinding.FragmentFinishedTaskBinding
import com.example.todoapp.models.TodoModel
import com.example.todoapp.ui.dialog.DialogButtonClicked
import com.example.todoapp.ui.dialog.MyDialog
import com.example.todoapp.utilService.SharedPreferenceClass
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class FinishedTaskFragment : Fragment(), FinishedItemClickListener, DialogButtonClicked {

    private lateinit var finishedTaskViewModel: FinishedTaskViewModel
    private var _binding: FragmentFinishedTaskBinding? = null
    private lateinit var sharedPreferenceClass: SharedPreferenceClass
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTV: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var todoList: ArrayList<TodoModel>
    private lateinit var token: String
    private val TAG = "com.example.todoapp.ui.gallery.FinishedTaskFragment"
    private lateinit var finishedTaskAdapter: FinishedTaskAdapter
    private lateinit var deleteId: String
    private lateinit var dialog: MyDialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        finishedTaskViewModel =
            ViewModelProvider(this).get(FinishedTaskViewModel::class.java)

        _binding = FragmentFinishedTaskBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferenceClass = SharedPreferenceClass(requireContext())
        token = sharedPreferenceClass.getValueString("token").toString()
        dialog = MyDialog(this)

        recyclerView = binding.recyclerView
        emptyTV = binding.emptyTv
        progressBar = binding.progressBar

        todoList = ArrayList()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        finishedTaskAdapter = FinishedTaskAdapter(this)
        recyclerView.adapter = finishedTaskAdapter

        getTask()
    }

    private fun getTask() {
        todoList = ArrayList()
        progressBar.visibility = View.VISIBLE
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/finished"
        Log.d(TAG, "Executed with token $token")
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url,
            null, { response ->
                try {
                    if (response.getBoolean("success")) {
                        val jsonArray = response.getJSONArray("todo")
                        Log.d(TAG, "Todo list size is $jsonArray")

                        if (jsonArray.length() == 0) {
                            emptyTV.visibility = View.VISIBLE
                        } else {
                            emptyTV.visibility = View.GONE
                            progressBar.visibility = View.GONE
                            for (i in 0..jsonArray.length()) {
                                val jsonObject: JSONObject = jsonArray[i] as JSONObject

                                val todoModel = TodoModel(
                                    jsonObject.getString("_id"),
                                    jsonObject.getString("title"),
                                    jsonObject.getString("description")
                                )
                                todoList.add(todoModel)
                                finishedTaskAdapter.updateList(todoList)
                            }
                            Log.d(TAG, "Todo list size is")
                        }
                    }
                    progressBar.visibility = View.GONE
                } catch (error: JSONException) {
                    error.printStackTrace()
                }
            }, { error ->
                Log.d(TAG, "Todo list size is mean2")
                progressBar.visibility = View.GONE
                val response = error.networkResponse
                Log.e(TAG, "Error listener")
                if (response != null && error is ServerError) {
                    try {
                        val res = String(response.data)
                        Log.e(TAG, "${response.data}")
                        Log.e(TAG, "Error in try block")
                        val obj = JSONObject(res)
                        Toast.makeText(context, obj.getString("msg"), Toast.LENGTH_SHORT)
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
                    Log.e(TAG, "$response")
                }

                progressBar.visibility = View.GONE
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            3000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    private fun deleteTodo() {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/$deleteId"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.DELETE, url, null, { response ->
            try {
                if (response.getBoolean("success")) {
                    getTask()
                    Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (je: JSONException) {
                je.printStackTrace()
            }
        }, { error ->
            Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
        })

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    private fun unDoneTodo(unDoneId: String) {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/$unDoneId"

        val body = HashMap<String, Boolean>()
        body["finished"] = false

        val jsonObjectRequest = object : JsonObjectRequest(Method.PUT, url, JSONObject(body as Map<*, *>?), { response ->
            try {
                if (response.getBoolean("success")) {
                    getTask()
                    Snackbar.make(binding.root, "Task undone", Snackbar.LENGTH_SHORT).show()
                }
            } catch (je : JSONException) {
                je.printStackTrace()
            }
        }, { error ->
            Snackbar.make(binding.root, "Something went wrong because ${error.message}", Snackbar.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                params["Authorization"] = token
                return params
            }
        }

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //    Adapter button functionalities
    override fun onDeleteButtonClicked(position: Int) {
        deleteId = todoList[position].id
        fragmentManager?.let { dialog.show(it, dialog.TODO_DELETE_DIALOG) }
    }

    override fun onUnDoneButtonClicked(position: Int) {
        unDoneTodo(todoList[position].id)
    }

    //    Dialog button functionalities
    override fun onDialogAddButtonClicked(title: String, description: String) {

    }

    override fun onDialogUpdateButtonClicked(title: String, description: String) {

    }

    override fun onDialogDeleteButtonClicked() {
        deleteTodo()
    }
}