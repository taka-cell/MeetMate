package app.kobayashi.totti.meetmate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.kobayashi.totti.meetmate.databinding.ActivityShareBinding
import com.google.gson.Gson
import java.util.*

class ShareActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShareBinding
    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater).apply { setContentView(this.root) }
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

        // SendButtonでメールアプリを使用してJSONデータを共有
        binding.sendButton.setOnClickListener {
            val id = binding.idEditText.text.toString().toLong()
            val startDate = binding.startdayEditText.text.toString()
            val endDate = binding.enddayEditText.text.toString()
            val emptyDates = getEmptyDatesFromCalendar()

            // JSONデータを生成
            val jsonData = sendDateToHost(id, startDate, endDate, emptyDates)

            // メールアプリを使ってJSONデータを送信
            shareJsonViaEmail(jsonData)
        }

        binding.cancelButton.setOnClickListener {
            val homePage = Intent(this, MainActivity::class.java)
            startActivity(homePage)
            finish()
        }
    }

    private fun getEmptyDatesFromCalendar(): List<String> {
        return listOf("2024-10-01", "2024-10-02", "2024-10-03")
    }

    // メールで送信するためにJSONデータを生成
    private fun sendDateToHost(id: Long, startDate: String, endDate: String, emptyDates: List<String>): String {
        val data = mapOf(
            "scheduleId" to id,
            "startDate" to startDate,
            "endDate" to endDate,
            "emptyDates" to emptyDates
        )

        // JSONデータを返す
        return Gson().toJson(data)
    }

    // メールアプリでJSONデータを送信
    private fun shareJsonViaEmail(jsonData: String) {
        val subject = "Shared Schedule Data"
        val body = "$jsonData"

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Send data"))
    }
}
