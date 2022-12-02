package projects.vaid.myNotes.database

import androidx.lifecycle.LiveData
import androidx.room.*
import projects.vaid.myNotes.Note
import java.util.*

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY primaryKey DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' OR text LIKE '%' || :searchQuery || '%' ORDER BY primaryKey DESC")
    fun getQueryNotes(searchQuery: String): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id=(:id)")
    fun getNote(id: UUID): LiveData<Note?>

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNote(note: Note)
}