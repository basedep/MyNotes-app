package projects.vaid.myNotes.interfaces

import java.util.*

interface Callback {
    fun onItemSelected(id: UUID)
    fun onSettingMenuSelected()
}