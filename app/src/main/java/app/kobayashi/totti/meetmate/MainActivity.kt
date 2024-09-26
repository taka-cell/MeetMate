package app.kobayashi.totti.meetmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.kobayashi.totti.meetmate.databinding.ActivityMainBinding
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val CALENDAR_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        binding.makeButton.setOnClickListener {
            val addPage = Intent(this, AddActivity::class.java)
            startActivity(addPage)
            finish()
        }
        binding.checkButton.setOnClickListener {
            val listPage = Intent(this, ListActivity::class.java)
            startActivity(listPage)
            finish()
        }

        binding.shareButton.setOnClickListener {
            val sharePage = Intent(this, ShareActivity::class.java)
            startActivity(sharePage)
            finish()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_PERMISSION_REQUEST_CODE)
        }


        handleIncomingIntent(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            val jsonData = uri.getQueryParameter("json")

            if (jsonData != null) {
                processReceivedData(jsonData)
            }
        }
    }

    private fun processReceivedData(jsonData: String) {
        val data = Gson().fromJson(jsonData, ScheduleData::class.java)

        openDateActivity(data)
    }
    private fun openDateActivity(scheduleData: ScheduleData) {
        val intent = Intent(this, DateActivity::class.java).apply {
            putExtra("scheduleData", scheduleData)
        }
        startActivity(intent)
    }

}
