package projects.vaid.myNotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import projects.vaid.myNotes.fragments.NoteDetailFragment
import projects.vaid.myNotes.fragments.NotesListFragment
import projects.vaid.myNotes.fragments.PreferenceFragment
import projects.vaid.myNotes.interfaces.Callback
import java.util.*

class MainActivity : AppCompatActivity(), Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //изменение темы
        val sharedPreferences = this.getSharedPreferences("SettingPreferences", MODE_PRIVATE)
        val isDarkModeOn = sharedPreferences.getBoolean("darkModeInit",false) //берем сохраненное значение switchPreference
        if (isDarkModeOn)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        val fragmentManager = supportFragmentManager.findFragmentById(R.id.container)

        if (fragmentManager == null){
            val fragment = NotesListFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit()
        }
    }

    //реализация интерфейса
    override fun onItemSelected(id: UUID) {

        val fragment = NoteDetailFragment.newInstance(id)
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .addToBackStack(null)
            .replace(R.id.container, fragment)
            .commit()
    }


    override fun onSettingMenuSelected() {
        val fragment = PreferenceFragment()
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .addToBackStack(null)
            .replace(R.id.container, fragment)
            .commit()
    }
}