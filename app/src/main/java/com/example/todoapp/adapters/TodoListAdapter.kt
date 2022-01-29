package com.example.todoapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.models.TodoModel

class TodoListAdapter(private val listener: TodoItemClickListener) : RecyclerView.Adapter<TodoListViewHolder>() {

    private val todoList: ArrayList<TodoModel> = ArrayList()
    private val TAG = "com.example.todoapp.adapters.TodoListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_list_item, parent, false)
        val viewHolder = TodoListViewHolder(view)

        val colorArray = view.resources.getIntArray(R.array.colorarray)
        val random = java.util.Random()
        val color = colorArray[random.nextInt(colorArray.size)]
        viewHolder.listItemTitle.setBackgroundColor(color)
        viewHolder.listItemBody.setBackgroundColor(color)

        view.setOnClickListener {
            listener.onItemClickListener(viewHolder.adapterPosition)
        }

        view.setOnLongClickListener {
            listener.onLongItemClickListener(viewHolder.adapterPosition)
            true
        }

        viewHolder.arrow.setOnClickListener {
            if (viewHolder.listItemBody.visibility == View.GONE) {
                viewHolder.listItemBody.visibility = View.VISIBLE
            } else {
                viewHolder.listItemBody.visibility = View.GONE
            }
        }

        viewHolder.deleteBtn.setOnClickListener {
            listener.onDeleteButtonClick(viewHolder.adapterPosition)
        }

        viewHolder.editBtn.setOnClickListener {
            listener.onEditButtonClick(viewHolder.adapterPosition)
        }

        viewHolder.doneBtn.setOnClickListener {
            listener.onDoneButtonClick(viewHolder.adapterPosition)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        val description = todoList[position].description
        holder.title.text = todoList[position].title
        if (description != "")
            holder.description.text = description
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun updateList(newList: ArrayList<TodoModel>) {
        todoList.clear()
        todoList.addAll(newList)

        notifyDataSetChanged()
    }

}

class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val listItemTitle: RelativeLayout = itemView.findViewById(R.id.list_item_title)
    val title: TextView = itemView.findViewById(R.id.task_title)
    val description: TextView = itemView.findViewById(R.id.task_description)
    val listItemBody: RelativeLayout = itemView.findViewById(R.id.accordian_body)
    val arrow: ImageView = itemView.findViewById(R.id.arrow)
    val editBtn: ImageView = itemView.findViewById(R.id.editBtn)
    val deleteBtn: ImageView = itemView.findViewById(R.id.deleteBtn)
    val doneBtn: ImageView = itemView.findViewById(R.id.doneBtn)
}

interface TodoItemClickListener {
    fun onItemClickListener(position: Int)

    fun onLongItemClickListener(position: Int)

    fun onEditButtonClick(position: Int)
    fun onDeleteButtonClick(position: Int)
    fun onDoneButtonClick(position: Int)
}