package app.kobayashi.totti.meetmate

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.kobayashi.totti.meetmate.databinding.ActivityDateBinding
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDateBinding
    private val myEmptyDates = mutableListOf<CalendarDay>()
    private var commonDates = mutableListOf<CalendarDay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        val scheduleData = intent.getSerializableExtra("scheduleData") as ScheduleData
        val startDate = convertDateToCalendarDay(scheduleData.startday)
        val endDate = convertDateToCalendarDay(scheduleData.endday)

        binding.linkTextView.text = scheduleData.id.toString()

        calendarView.state().edit()
            .setMinimumDate(startDate)
            .setMaximumDate(endDate)
            .commit()

        CoroutineScope(Dispatchers.IO).launch {
            val userCalendars = getUserAddedCalendars()
            val events = getCalendarEvents(scheduleData.startday, scheduleData.endday, userCalendars)

            withContext(Dispatchers.Main) {
                myEmptyDates.addAll(getMyEmptyDates(events, startDate, endDate))
                displayMyEmptyDaysOnCalendar(calendarView)
            }
        }

        receiveOtherUsersDates(scheduleData.id)

        binding.listButton.setOnClickListener {
            val listPage = Intent(this, ListActivity::class.java)
            startActivity(listPage)
            finish()
        }

        binding.shareButton.setOnClickListener {
            showPasteDialog()
        }
    }

    private fun showPasteDialog() {
        val input = EditText(this).apply {
            hint = "ここにデータ入力"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("データをペーストして下さい")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val jsonText = input.text.toString()
                if (jsonText.isNotEmpty()) {
                    try {
                        val scheduleData = parseJsonData(jsonText)
                        handleReceivedData(scheduleData)
                        Toast.makeText(this, "データが正常に処理されました", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "無効なJSONデータです", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "JSONデータを入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .create()

        dialog.show()
    }

    private fun parseJsonData(jsonText: String): ScheduleData {
        return Gson().fromJson(jsonText, ScheduleData::class.java)
    }

    private fun handleReceivedData(scheduleData: ScheduleData) {
        val startDate = convertDateToCalendarDay(scheduleData.startday)
        val endDate = convertDateToCalendarDay(scheduleData.endday)
        val emptyDates = scheduleData.emptyDates.map { dateString ->
            val parts = dateString.split("-")
            CalendarDay.from(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }

        commonDates = myEmptyDates.intersect(emptyDates).toMutableList()
        displayCommonEmptyDatesOnCalendar()
    }

    private fun displayMyEmptyDaysOnCalendar(calendarView: MaterialCalendarView) {
        calendarView.addDecorator(CircleBackgroundDecorator(myEmptyDates, "#65558F"))
    }

    private fun receiveOtherUsersDates(scheduleId: Long) {
        val otherUsersDates = listOf<CalendarDay>()
        commonDates = myEmptyDates.intersect(otherUsersDates).toMutableList()

        displayCommonEmptyDatesOnCalendar()
    }

    private fun displayCommonEmptyDatesOnCalendar() {
        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        calendarView.removeDecorators()
        calendarView.addDecorator(CircleBackgroundDecorator(commonDates, "#FF0000"))
    }

    private fun getMyEmptyDates(events: List<CalendarEvent>, startDate: CalendarDay, endDate: CalendarDay): List<CalendarDay> {
        val emptyDates = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        calendar.time = Date(startDate.year - 1900, startDate.month, startDate.day)

        while (!calendar.time.after(Date(endDate.year - 1900, endDate.month, endDate.day))) {
            val currentDate = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            if (events.none { event -> isEventOnDate(event, currentDate) }) {
                emptyDates.add(currentDate)
            }

            calendar.add(Calendar.DATE, 1)
        }

        return emptyDates
    }

    private fun isEventOnDate(event: CalendarEvent, date: CalendarDay): Boolean {
        val eventStartDate = Calendar.getInstance().apply { timeInMillis = event.startTime }
        val eventEndDate = Calendar.getInstance().apply { timeInMillis = event.endTime }
        val eventDay = CalendarDay.from(eventStartDate.get(Calendar.YEAR), eventStartDate.get(Calendar.MONTH), eventStartDate.get(Calendar.DAY_OF_MONTH))

        return eventDay == date || (date.date.after(eventStartDate.time) && date.date.before(eventEndDate.time))
    }

    private fun convertDateToCalendarDay(date: Date): CalendarDay {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    private fun getUserAddedCalendars(): List<Long> {
        val userCalendars = mutableListOf<Long>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val calendarId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val calendarDisplayName = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))

                if (!calendarDisplayName.contains("日本の祝日") && !calendarDisplayName.contains("Holiday")) {
                    userCalendars.add(calendarId)
                }
            }
        }

        return userCalendars
    }

    private fun getCalendarEvents(startDate: Date, endDate: Date, calendarIds: List<Long>): List<CalendarEvent> {
        val eventList = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_ID
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ? AND ${CalendarContract.Events.CALENDAR_ID} IN (${calendarIds.joinToString(",")})"
        val selectionArgs = arrayOf(
            startDate.time.toString(),
            endDate.time.toString()
        )

        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

                eventList.add(CalendarEvent(eventId, title, startTime, endTime))
            }
        }

        return eventList
    }
}
