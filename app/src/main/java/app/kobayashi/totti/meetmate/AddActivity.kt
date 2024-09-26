package app.kobayashi.totti.meetmate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.kobayashi.totti.meetmate.databinding.ActivityAddBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        val calendar = Calendar.getInstance()

        binding.startdayEditText.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                startDate = calendar.time
                binding.startdayEditText.setText("$year/${month + 1}/$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        binding.enddayEditText.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                endDate = calendar.time
                binding.enddayEditText.setText("$year/${month + 1}/$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // 保存ボタンを押したときの処理
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            if (name.isNotEmpty() && startDate != null && endDate != null) {
                val id = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()

                val emptyDates = calculateEmptyDates(startDate!!, endDate!!)

                val scheduleData = ScheduleData(id, name, startDate!!, endDate!!, emptyDates)

                val intent = Intent(this, ListActivity::class.java).apply {
                    putExtra("newdata", scheduleData)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun calculateEmptyDates(startDate: Date, endDate: Date): List<String> {
        val emptyDates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            emptyDates.add("$year-$month-$day")
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return emptyDates
    }
}
