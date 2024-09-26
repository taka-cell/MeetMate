package app.kobayashi.totti.meetmate

import RecyclerViewAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobayashi.totti.meetmate.databinding.ActivityListBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var adapter: RecyclerViewAdapter
    private val list: MutableList<ScheduleData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        adapter = RecyclerViewAdapter(this) { scheduleData ->
            val intent = Intent(this, DateActivity::class.java)
            intent.putExtra("scheduleData", scheduleData)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadListFromPreferences()

        val newdata = intent.getSerializableExtra("newdata") as? ScheduleData
        if (newdata != null) {
            list.add(newdata)
            saveListToPreferences()
            adapter.submitList(list.toMutableList())
        }

        binding.addButton.setOnClickListener {
            val addPage = Intent(this, AddActivity::class.java)
            startActivity(addPage)
            finish()
        }

        binding.home.setOnClickListener{
            val homePage = Intent(this,MainActivity::class.java)
            startActivity(homePage)
            finish()
        }
    }

    private fun loadListFromPreferences() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("meetmate_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("schedule_list", null)

        if (json != null) {
            val type = object : TypeToken<MutableList<ScheduleData>>() {}.type
            val savedList: MutableList<ScheduleData> = gson.fromJson(json, type)
            list.clear()
            list.addAll(savedList)
            adapter.submitList(list.toMutableList())
        }
    }

    private fun saveListToPreferences() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("meetmate_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // リストをJSONに変換して保存
        val json = gson.toJson(list)
        editor.putString("schedule_list", json)
        editor.apply()
    }
}
