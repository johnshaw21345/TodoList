package my.johnshaw21345.todolist;


import my.johnshaw21345.todolist.beans.Note;

public interface NoteOperator {

    void deleteNote(Note note);

    void updateNote(Note note);
}
