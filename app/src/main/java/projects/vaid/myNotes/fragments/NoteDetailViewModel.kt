package projects.vaid.myNotes.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import projects.vaid.myNotes.Note
import projects.vaid.myNotes.database.NoteRepository
import java.util.*

class NoteDetailViewModel : ViewModel() {

    private val repository = NoteRepository.getDatabase() //получаем бд
    private val noteIdLiveData = MutableLiveData<UUID>() //хранит id отображаемого объекта Note

    //получаем заметку
    var note : LiveData<Note?> =
        Transformations.switchMap(noteIdLiveData){ id->
            repository.getNote(id)
        }

    //присваиваем id для noteIdLiveData
    fun setId(id: UUID){
        noteIdLiveData.value = id
    }

    //обновляем заметку
    fun updateNote(note: Note){
        repository.updateNote(note)
    }

    //удаляем заметку
    fun deleteNote(note: Note){
        repository.deleteNote(note)
    }


}