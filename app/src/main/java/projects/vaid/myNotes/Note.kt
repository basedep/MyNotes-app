package projects.vaid.myNotes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity("notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var primaryKey: Int? = null,
    var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var text: String = "",
    var color: String = "",
    var dateOfEdit: Date? = Date()
)