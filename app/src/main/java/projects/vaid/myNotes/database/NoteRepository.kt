package projects.vaid.myNotes.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import projects.vaid.myNotes.Note
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

/** Класс репозиторий для доступа к базе данных */

private const val DATABASE_NAME = "notes-database"

class NoteRepository private constructor(context: Context){

    //бд
    private val database = Room.databaseBuilder(context, NoteDatabase::class.java, DATABASE_NAME)
        .build()

    //работа с бд выполняется не в основном потоке
    private val executor = Executors.newSingleThreadExecutor()

    private val noteDao = database.noteDao()

    fun getAllNotes(): LiveData<List<Note>>{
        return noteDao.getAllNotes()
    }

    fun getNote(id: UUID): LiveData<Note?>{
        return noteDao.getNote(id)
    }

    fun getQueryNote(queryString: String): LiveData<List<Note>>{
        return noteDao.getQueryNotes(queryString)
    }

    fun addNote(note: Note) {
        executor.execute {
            noteDao.addNote(note)
        }
    }

    fun updateNote(note: Note){
        executor.execute{
            noteDao.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        executor.execute {
            noteDao.deleteNote(note)
        }
    }

    companion object{
        private var INSTANCE: NoteRepository?  = null
        //инициализировать класс
        fun initialize(context: Context){
            if (INSTANCE == null)
                INSTANCE = NoteRepository(context)
        }
        //получить доступ к бд
        fun getDatabase(): NoteRepository{
            return INSTANCE ?: throw IllegalStateException("not initialized")
        }
    }

}