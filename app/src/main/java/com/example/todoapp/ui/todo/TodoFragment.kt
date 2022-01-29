package com.example.todoapp.ui.todo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.todoapp.adapters.TodoItemClickListener
import com.example.todoapp.adapters.TodoListAdapter
import com.example.todoapp.databinding.FragmentTodoBinding
import com.example.todoapp.models.TodoModel
import com.example.todoapp.ui.dialog.DialogButtonClicked
import com.example.todoapp.ui.dialog.MyDialog
import com.example.todoapp.utilService.SharedPreferenceClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class TodoFragment : Fragment(), TodoItemClickListener, DialogButtonClicked {

    private lateinit var todoViewModel: TodoViewModel
    private var _binding: FragmentTodoBinding? = null
    private lateinit var fab: FloatingActionButton
    private lateinit var sharedPreferenceClass: SharedPreferenceClass
    private lateinit var token: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var todoListAdapter: TodoListAdapter
    private lateinit var emptyTV: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var todoList: ArrayList<TodoModel>
    private lateinit var dialog: MyDialog
    private val TAG = "com.example.todoapp.ui.todo.TodoFragment"
    private lateinit var deleteId: String
    private var deletePos: Int = -1
    private lateinit var updateId: String
    private var updatePos = -1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        todoViewModel =
            ViewModelProvider(this)[TodoViewModel::class.java]

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferenceClass = SharedPreferenceClass(requireContext())
        token = sharedPreferenceClass.getValueString("token").toString()
        dialog = MyDialog(this)

        fab = binding.todoFab
        progressBar = binding.todoProgressBar
        emptyTV = binding.todoText
        recyclerView = binding.todoRecycler

        fab.setOnClickListener {
            dialog.show(requireFragmentManager(), dialog.TODO_ADD_DIALOG)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        todoListAdapter = TodoListAdapter(this)
        recyclerView.adapter = todoListAdapter

        getTask()

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            doneTodo(todoList[position].id)
        }

    }

    private fun getTask() {
        todoList = ArrayList()
        todoList.clear()
        progressBar.visibility = View.VISIBLE
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url,
            null, { response ->
                try {
                    if (response.getBoolean("success")) {
                        val jsonArray = response.getJSONArray("todos")

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
                                Log.d(TAG, "Todo list size is mean")
                                todoListAdapter.updateList(todoList)
                            }
                            Log.d(TAG, "Todo list size is")
                        }
                    }
                    progressBar.visibility = View.GONE
                } catch (error: JSONException) {
                    error.printStackTrace()
                }
            }, { error ->
                val response = error.networkResponse
                Log.e(TAG, "Error listener")
                if (response != null && error is ServerError) {
                    try {
                        progressBar.visibility = View.GONE
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

        val socketTime = 3000
        val policy = DefaultRetryPolicy(
            socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        jsonObjectRequest.retryPolicy = policy

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    private fun addTodo(title: String, description: String) {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo"

        val body = HashMap<String, String>()
        body["title"] = title
        body["description"] = description

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, JSONObject(body as Map<*, *>?),{ response ->
            try {
                if (response.getBoolean("success")) {
                    Toast.makeText(activity, "Added Successfully", Toast.LENGTH_SHORT).show()
                    getTask()
                }
            } catch (error: JSONException) {
                error.printStackTrace()
            }
        }, { error ->
            val response = error.networkResponse
            try {
                if (response != null) {
                    Toast.makeText(activity, "${response.data}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }
        val socketTime = 3000
        val policy = DefaultRetryPolicy(socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsonObjectRequest.retryPolicy = policy

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    private fun deleteTodo() {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/$deleteId"

        val jsonObjectRequest= JsonObjectRequest(Request.Method.DELETE, url, null, { response ->
            try {
                if (response.getBoolean("success")) {
                    getTask()
                    Toast.makeText(context, "Successfully deleted",Toast.LENGTH_SHORT).show()
                }
            } catch (je : JSONException) {
                je.printStackTrace()
            }
        },{ error ->
            Toast.makeText(context,"${error.message}",Toast.LENGTH_SHORT).show()
        })

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }

    private fun updateTodo(title: String, description: String) {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/$updateId"

        val body = HashMap<String, String>()
        body["title"] = title
        body["description"] = description

        val jsonObjectRequest = object : JsonObjectRequest(Method.PUT, url, JSONObject(body as Map<*, *>?), { response ->
            try {
                if (response.getBoolean("success")) {
                    getTask()
                    Toast.makeText(context, "Successfully updated", Toast.LENGTH_SHORT).show()
                }
            } catch (je: JSONException) {
                je.printStackTrace()
            }
        },{ error ->
            Toast.makeText(context, "Problem because ${error.message}", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
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

    private fun doneTodo(doneId: String) {
        val url = "https://todoapplicationxyz.herokuapp.com/api/todo/$doneId"

        val body = HashMap<String, Boolean>()
        body["finished"] = true

        val jsonObjectRequest = object : JsonObjectRequest(Method.PUT, url, JSONObject(body as Map<*, *>?), { response ->
            try {
                if (response.getBoolean("success")) {
                    getTask()
                    Snackbar.make(binding.root, "Task done", Snackbar.LENGTH_SHORT).show()
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

    //Implement method of adapter listener
    override fun onItemClickListener(position: Int) {

    }

    override fun onLongItemClickListener(position: Int) {

    }

    override fun onEditButtonClick(position: Int) {
        updateId = todoList[position].id
        updatePos = position
        fragmentManager?.let { dialog.show(it, dialog.TODO_UPDATE_DIALOG) }
    }

    override fun onDeleteButtonClick(position: Int) {
        deleteId = todoList[position].id
        deletePos = position
        fragmentManager?.let { dialog.show(it, dialog.TODO_DELETE_DIALOG) }
    }

    override fun onDoneButtonClick(position: Int) {
        doneTodo(todoList[position].id)
    }

    //Implement method of dialog listener

    override fun onDialogAddButtonClicked(title: String, description: String) {
        addTodo(title,description)
    }

    override fun onDialogUpdateButtonClicked(title: String, description: String) {
        updateTodo(title, description)
    }

    override fun onDialogDeleteButtonClicked() {
        deleteTodo()
    }
}