import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.kobayashi.totti.meetmate.ScheduleData
import app.kobayashi.totti.meetmate.databinding.ListDataCellBinding

class RecyclerViewAdapter(
    private val context: Context,
    private val onItemClick: (ScheduleData) -> Unit
) : ListAdapter<ScheduleData, RecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(val binding: ListDataCellBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scheduleData: ScheduleData) {
            binding.listButton.text = scheduleData.name
            binding.listButton.setOnClickListener {
                onItemClick(scheduleData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListDataCellBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScheduleData>() {
            override fun areItemsTheSame(oldItem: ScheduleData, newItem: ScheduleData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ScheduleData, newItem: ScheduleData): Boolean {
                return oldItem == newItem
            }
        }
    }
}
