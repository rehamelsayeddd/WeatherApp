import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.Alert.View.OnClickInterface
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.R
import com.example.weatherapp.databinding.AlertDetailsItemBinding
import java.util.Calendar

class AlertAdapter(
    private var alertList: List<AlertData>,
    private var onClick: OnClickInterface,
    private var onDelete: (AlertData) -> Unit // Add onDelete method
) : ListAdapter<AlertData, AlertAdapter.AlertViewHolder>(AlertDiffCallback()) {

    private lateinit var context: Context

    class AlertViewHolder(val viewBinding: AlertDetailsItemBinding) : RecyclerView.ViewHolder(viewBinding.root)

    class AlertDiffCallback : DiffUtil.ItemCallback<AlertData>() {
        override fun areItemsTheSame(oldItem: AlertData, newItem: AlertData): Boolean {
            return oldItem.id == newItem.id // Assuming AlertData has an id property
        }

        override fun areContentsTheSame(oldItem: AlertData, newItem: AlertData): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AlertDetailsItemBinding.inflate(inflater, parent, false)
        context = parent.context // Initialize context here
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alertItem = getItem(position)
        val endTime = choosenTime(alertItem.milleDateTo, alertItem.milleTimeTo)

        // Set text using binding
        holder.viewBinding.timeStartTxt.text = alertItem.fromTime
        holder.viewBinding.startDateText.text = alertItem.fromDate
        holder.viewBinding.timeEndTxt.text = alertItem.toTime
        holder.viewBinding.endDateText.text = alertItem.toDate

        // Trigger onAlarmClick if the alert time has passed
        if (Calendar.getInstance().timeInMillis > endTime.timeInMillis) {
            onClick.onAlarmClick(alertItem)
        }

        // Show delete popup on long click
        holder.itemView.setOnLongClickListener {
            showDeletePopup(holder.itemView, alertItem)
            true
        }
    }

    fun choosenTime(toDate: Long, toTime: Long): Calendar {
        val testCalendar = Calendar.getInstance().apply { timeInMillis = toDate }
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, testCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.MONTH, testCalendar.get(Calendar.MONTH))
            set(Calendar.YEAR, testCalendar.get(Calendar.YEAR))
            testCalendar.timeInMillis = toTime
            set(Calendar.HOUR_OF_DAY, testCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, testCalendar.get(Calendar.MINUTE))
        }
    }

    fun setList(newList: List<AlertData>) {
        alertList = newList
        submitList(newList) // Use submitList from ListAdapter to update the list
    }

    private fun showDeletePopup(anchorView: View, alertItem: AlertData) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.refresh_favourite_item_action_layout, null)
        val myPopupWindow = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Handle delete button click inside the popup
        val deleteButton: View = view.findViewById(R.id.deleter)
        deleteButton.setOnClickListener {
            dialogDeleteConfirmation(alertItem)
            myPopupWindow.dismiss() // Close the popup
        }

        // Show the popup window near the anchor view (the item in the list)
        myPopupWindow.showAsDropDown(anchorView, 0, 0)
    }

    // Show delete confirmation dialog
    fun dialogDeleteConfirmation(alertItem: AlertData) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.removeDialogmsg)
        alertDialogBuilder.setMessage(R.string.deletAlertConfirm)
        alertDialogBuilder.setPositiveButton(R.string.sure) { dialog, _ ->
            onDelete(alertItem) // Call the delete action passed as a lambda
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton(R.string.notsure) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
