package projects.vaid.myNotes.application

import android.app.Application
import projects.vaid.myNotes.database.NoteRepository

class MyNotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
            NoteRepository.initialize(applicationContext)

    }
}