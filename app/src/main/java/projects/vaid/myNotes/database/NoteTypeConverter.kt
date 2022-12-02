package projects.vaid.myNotes.database

import androidx.room.TypeConverter
import java.util.*

class NoteTypeConverter {

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }


}