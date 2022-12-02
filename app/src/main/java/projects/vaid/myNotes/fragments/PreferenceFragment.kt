package projects.vaid.myNotes.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.*
import projects.vaid.myNotes.R

class PreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener{

    private lateinit var switchPreference: SwitchPreference
    private lateinit var listPreference: ListPreference
    private lateinit var fillPreference: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        switchPreference = findPreference("nightMode")!!
        switchPreference.onPreferenceChangeListener = this

        listPreference = findPreference("textSize")!!
        listPreference.onPreferenceChangeListener = this

        fillPreference = findPreference("colorFillOption")!!
        fillPreference.onPreferenceChangeListener = this

    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {

        val sharedPreferences = requireContext().getSharedPreferences("SettingPreferences", MODE_PRIVATE)
        val spEditor = sharedPreferences.edit()

        if (preference.key == switchPreference.key) {
            spEditor.putBoolean("darkModeInit", newValue as Boolean) //сохраняем в sp состояние switchPreference
            spEditor.apply()

            if (newValue)
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }

        if (preference.key == listPreference.key){
            spEditor.putInt("textSizeSetting", newValue.toString().toInt()) //сохраняем в sp значение listPreference
            spEditor.apply()
        }

        if (preference.key == fillPreference.key){
            spEditor.putInt("colorFillOption", newValue.toString().toInt())
            spEditor.apply()
        }

        return true
    }
}