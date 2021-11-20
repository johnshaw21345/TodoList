package my.johnshaw21345.todolist.ui

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import my.johnshaw21345.todolist.NoteOperator
import my.johnshaw21345.todolist.R
import my.johnshaw21345.todolist.beans.Note
import my.johnshaw21345.todolist.beans.State
import java.text.SimpleDateFormat
import java.util.*

class NoteViewHolder(@NonNull itemView: View, operator: NoteOperator) :
    RecyclerView.ViewHolder(itemView) {
    private val operator: NoteOperator
    private val checkBox: CheckBox
    private val contentText: TextView
    private val dateText: TextView
    private val deleteBtn: View
    fun bind(note: Note) {
        contentText.setText(note.getContent())
        dateText.setText(SIMPLE_DATE_FORMAT.format(note.getDate()))
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(note.getState() === State.DONE)
        checkBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                note.setState(if (isChecked) State.DONE else State.TODO)
                operator.updateNote(note)
            }
        })
        deleteBtn.setOnClickListener { operator.deleteNote(note) }
        if (note.getState() === State.DONE) {
            contentText.setTextColor(Color.GRAY)
            contentText.setPaintFlags(contentText.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
        } else {
            contentText.setTextColor(Color.BLACK)
            contentText.setPaintFlags(contentText.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }
        itemView.setBackgroundColor(note.getPriority().color)
    }

    companion object {
        private val SIMPLE_DATE_FORMAT =
            SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH)
    }

    init {
        this.operator = operator
        checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)
        contentText = itemView.findViewById<TextView>(R.id.text_content)
        dateText = itemView.findViewById<TextView>(R.id.text_date)
        deleteBtn = itemView.findViewById(R.id.btn_delete)
    }
}