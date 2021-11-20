package my.johnshaw21345.todolist


import android.annotation.SuppressLint
import androidx.annotation.Nullable
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns

import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import my.johnshaw21345.todolist.beans.Note
import my.johnshaw21345.todolist.beans.Priority
import my.johnshaw21345.todolist.beans.State
import my.johnshaw21345.todolist.db.TodoContract
import my.johnshaw21345.todolist.db.TodoDbHelper
import my.johnshaw21345.todolist.debug.DebugActivity
import my.johnshaw21345.todolist.ui.NoteListAdapter

import java.util.*

class MainActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var notesAdapter: NoteListAdapter? = null
    private var dbHelper: TodoDbHelper? = null
    private var database: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivityForResult(
                Intent(this@MainActivity, NoteActivity::class.java),
                REQUEST_CODE_ADD
            )
        }
        dbHelper = TodoDbHelper(this)
        database = dbHelper!!.writableDatabase
        recyclerView = findViewById(R.id.list_todo)
        recyclerView?.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        recyclerView?.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        notesAdapter = NoteListAdapter(object : NoteOperator {
            override fun deleteNote(note: Note) {
                this@MainActivity.deleteNote(note)
            }

            override fun updateNote(note: Note) {
                updateNode(note)
            }
        })
        recyclerView?.adapter = notesAdapter
        notesAdapter!!.refresh(loadNotesFromDatabase())
    }

    override fun onDestroy() {
        super.onDestroy()
        database!!.close()
        database = null
        dbHelper!!.close()
        dbHelper = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.action_debug -> {
                startActivity(Intent(this, DebugActivity::class.java))
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD
            && resultCode == RESULT_OK
        ) {
            notesAdapter!!.refresh(loadNotesFromDatabase())
        }
    }

    @SuppressLint("Range")
    private fun loadNotesFromDatabase(): List<Note> {
        if (database == null) {
            return emptyList()
        }
        val result: MutableList<Note> = LinkedList<Note>()
        var cursor: Cursor? = null
        try {
            cursor = database!!.query(
                TodoContract.TodoNote.TABLE_NAME, null,
                null, null,
                null, null, TodoContract.TodoNote.COLUMN_PRIORITY + " DESC"
            )
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_CONTENT))
                val dateMs = cursor.getLong(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_DATE))
                val intState = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_STATE))
                val intPriority = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_PRIORITY))
                val note = Note(id)
                note.content = content
                note.date = Date(dateMs)
                note.state = State.from(intState)
                note.priority = Priority.from(intPriority)
                result.add(note)
            }
        } finally {
            cursor?.close()
        }
        return result
    }

    private fun deleteNote(note: Note) {
        if (database == null) {
            return
        }
        val rows = database!!.delete(
            TodoContract.TodoNote.TABLE_NAME, BaseColumns._ID + "=?", arrayOf(
                (note.id).toString()
            )
        )
        if (rows > 0) {
            notesAdapter!!.refresh(loadNotesFromDatabase())
        }
    }

    private fun updateNode(note: Note) {
        if (database == null) {
            return
        }
        val values = ContentValues()
        values.put(TodoContract.TodoNote.COLUMN_STATE, note.state.intValue)
        val rows = database!!.update(
            TodoContract.TodoNote.TABLE_NAME, values, BaseColumns._ID + "=?", arrayOf(
                (note.id).toString()
            )
        )
        if (rows > 0) {
            notesAdapter!!.refresh(loadNotesFromDatabase())
        }
    }

    companion object {
        private const val REQUEST_CODE_ADD = 1002
    }
}