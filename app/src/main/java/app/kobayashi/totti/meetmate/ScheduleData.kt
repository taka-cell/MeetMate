package app.kobayashi.totti.meetmate
import java.io.Serializable
import java.util.Date

data class ScheduleData(
    val id: Long,
    val name: String,
    val startday: Date,
    val endday: Date,
    val emptyDates: List<String>
) : Serializable