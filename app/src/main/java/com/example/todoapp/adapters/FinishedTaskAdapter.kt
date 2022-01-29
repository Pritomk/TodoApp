package com.example.todoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.databinding.TodoListItemBinding
import com.example.todoapp.models.TodoModel

class FinishedTaskAdapter(private val listener: FinishedItemClickListener) : RecyclerView.Adapter<FinishedTaskViewHolder>() {

    private val todoList = ArrayList<TodoModel>()
    private val TAG = "com.example.todoapp.adapters.FinishedTaskAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinishedTaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.finished_todo_item, parent, false)
        val viewHolder = FinishedTaskViewHolder(view)

        val colorArray = view.resources.getIntArray(R.array.colorarray)
        val random = java.util.Random()
        val color = colorArray[random.nextInt(colorArray.size)]
        viewHolder.listItemTitle.setBackgroundColor(color)
        viewHolder.listItemBody.setBackgroundColor(color)

        viewHolder.deleteBtn.setOnClickListener {
            listener.onDeleteButtonClicked(viewHolder.adapterPosition)
        }

        viewHolder.arrow.setOnClickListener {
            if (viewHolder.listItemBody.visibility == View.GONE) {
                viewHolder.listItemBody.visibility = View.VISIBLE
            } else {
                viewHolder.listItemBody.visibility = View.GONE
            }
        }


        return viewHolder

    }

    override fun onBindViewHolder(holder: FinishedTaskViewHolder, position: Int) {
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

class FinishedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val listItemTitle: CardView = itemView.findViewById(R.id.accordian_title)
    val arrow: ImageView = itemView.findViewById(R.id.arrow)
    val title: TextView = itemView.findViewById(R.id.task_title)
    val listItemBody: RelativeLayout = itemView.findViewById(R.id.accordian_body)
    val description: TextView = itemView.findViewById(R.id.task_description)
    val deleteBtn: ImageView = itemView.findViewById(R.id.deleteBtn)
}

interface FinishedItemClickListener {
    fun onDeleteButtonClicked(position: Int)
}