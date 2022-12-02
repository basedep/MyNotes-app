package projects.vaid.myNotes.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import projects.vaid.myNotes.Note
import projects.vaid.myNotes.database.NoteRepository

class NotesListViewModel : ViewModel() {

    private val repository = NoteRepository.getDatabase() //получаем бд

    val notesLiveData = repository.getAllNotes() //получаем все заметки

    fun getQueryNote(queryString: String): LiveData<List<Note>>{
        return repository.getQueryNote(queryString)
    }


    fun addNote(note: Note){
        repository.addNote(note)
    }

    fun deleteNote(note: Note){
        repository.deleteNote(note)
    }

}