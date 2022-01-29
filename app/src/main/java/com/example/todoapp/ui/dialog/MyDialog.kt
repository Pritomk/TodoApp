package com.example.todoapp.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.todoapp.databinding.FragmentDeleteDialogBinding
import com.example.todoapp.databinding.FragmentDialogBinding

class MyDialog(private val listener: DialogButtonClicked) : DialogFragment() {

    val TODO_ADD_DIALOG = "addTodo"
    val TODO_UPDATE_DIALOG = "updateTodo"
    val TODO_DELETE_DIALOG = "deleteTodo"

    private lateinit var dialogBinding: FragmentDialogBinding
    private lateinit var deleteDialogBinding: FragmentDeleteDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog: Dialog? = null;

        dialogBinding = FragmentDialogBinding.inflate(layoutInflater)
        deleteDialogBinding = FragmentDeleteDialogBinding.inflate(layoutInflater)

        when(tag) {
            TODO_ADD_DIALOG -> {
                dialog = getAddTodoDialog()
            }
            TODO_UPDATE_DIALOG -> {
                dialog = getUpdateTodoDialog()
            }
            TODO_DELETE_DIALOG -> {
                dialog = getDeleteTodoDialog()
            }
        }

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun getAddTodoDialog(): Dialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setView(dialogBinding.root)

        val dialogTitle: TextView = dialogBinding.titleDialog
        val title: TextView = dialogBinding.edt01
        val description: TextView = dialogBinding.edt02
        val addBtn: Button = dialogBinding.addBtn
        val cancelBtn: Button = dialogBinding.cancelBtn

        dialogTitle.text = "Add new ToDo"
        title.hint = "New Task"
        description.hint = "Description"
        cancelBtn.setOnClickListener { v -> dismiss() }
        addBtn.setOnClickListener {
            val titleText = title.text.toString()
            val descriptionText = description.text.toString()
            listener.onDialogAddButtonClicked(titleText,descriptionText)
            dismiss()
        }
        return builder.create()
    }

    private fun getUpdateTodoDialog(): Dialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setView(dialogBinding.root)

        val dialogTitle: TextView = dialogBinding.titleDialog
        val title: TextView = dialogBinding.edt01
        val description: TextView = dialogBinding.edt02
        val addBtn: Button = dialogBinding.addBtn
        val cancelBtn: Button = dialogBinding.cancelBtn

        dialogTitle.text = "Update ToDo"
        title.hint = "New Task"
        description.hint = "Description"
        cancelBtn.setOnClickListener { v -> dismiss() }
        addBtn.setOnClickListener {
            val titleText = title.text.toString()
            val descriptionText = description.text.toString()
            listener.onDialogUpdateButtonClicked(titleText,descriptionText)
            dismiss()
        }
        return builder.create()

    }

    private fun getDeleteTodoDialog(): Dialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setView(deleteDialogBinding.root)

        val cancelBtn = deleteDialogBinding.cancelBtn
        val deleteBtn = deleteDialogBinding.deleteBtn

        cancelBtn.setOnClickListener { dismiss() }
        deleteBtn.setOnClickListener {
            listener.onDialogDeleteButtonClicked()
            dismiss()
        }
        return builder.create()
    }
}

interface DialogButtonClicked {
    fun onDialogAddButtonClicked(title: String, description: String)
    fun onDialogUpdateButtonClicked(title: String, description: String)
    fun onDialogDeleteButtonClicked()
}