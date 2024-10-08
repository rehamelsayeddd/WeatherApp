package com.example.weatherapp.Alert.View

import AlertAdapter
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Alert.NetworkListener
import com.example.weatherapp.Alert.ViewModel.AlertViewModel
import com.example.weatherapp.Alert.ViewModel.AlertViewModelFactory
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.OneCall.Model.AlertData

import com.example.weatherapp.R
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.SharedKey
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.databinding.AlertDialogBinding
import com.example.weatherapp.databinding.FragmentAlertBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit


class AlertFragment : Fragment(),OnClickInterface {
    private lateinit var channelCode:String
    lateinit var binding: FragmentAlertBinding
    lateinit var alarmManager: AlarmManager
    lateinit var pIntent: PendingIntent
    lateinit var alertFactory: AlertViewModelFactory
    private lateinit var repository: WeatherRepository
    private lateinit var remoteData: RemoteData
    private lateinit var localData: LocalData
    private lateinit var sharedPreferencesManager: SharedPreferenceManager
    lateinit var viewModel: AlertViewModel
    lateinit var alertAdapter: AlertAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var edit: SharedPreferences.Editor
    lateinit var choice:String
    lateinit var lat:String
    lateinit var lon:String
    var requestCode:Long =0
    var dateOneSelected: String? = null
    var dateTwoSelected: String? = null
    var timeOne: String? = null
    var timeTwo: String? = null
    var calenderFromTime: Long = 0
    var calenderFromDate: Long = 0
    var calenderToTime: Long = 0
    var calenderToDate: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.setTitle("Alerts")
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        sharedPreferences = context?.getSharedPreferences("choice", Context.MODE_PRIVATE) as SharedPreferences
        edit = sharedPreferences.edit()
        choice = sharedPreferences.getString(SharedKey.ALERT_TYPE.name,"notification") as String
        sharedPreferencesManager = SharedPreferenceManager.getInstance(requireContext())




        if(sharedPreferencesManager.getSavedMap(SharedKey.MAP.name,"")=="alert") {
            val longlat = sharedPreferencesManager.getLocationToAlert(SharedKey.ALERT.name)
            val longg = longlat!!.first
            val latt = longlat.second
            lat =latt.toString()
            lon = longg.toString()
            Log.i("=====latlong Alert", ": "+lat + " "+ lon)


        }
       //current home
            val alertLongLat = sharedPreferencesManager.getLocationToAlert(SharedKey.ALERT.name)
            val latC = alertLongLat!!.first
            val lonC = alertLongLat!!.second
            lat=latC.toString()
            lon =lonC.toString()

            Log.i("======aaajksl", "else in alert location "+lat)



        remoteData = RemoteData()
        localData = LocalData(requireContext())
        repository= WeatherRepository.getInstance(remoteData,localData)

        alertFactory = AlertViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), alertFactory).get(AlertViewModel::class.java)

        alertAdapter= AlertAdapter(listOf(),this)

        linearLayoutManager = LinearLayoutManager(context)
        binding.alertRV.adapter = alertAdapter
        binding.alertRV.layoutManager = linearLayoutManager

        viewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            alertAdapter.setList(alerts) // observe alert from VM and update recylcer view
        }

        binding.btnCompleteAction.setOnClickListener {
            if (NetworkListener.getConnectivity(requireContext())) {
                if (!Settings.canDrawOverlays(requireContext())){
                    requestOverAppPermission()
                }
                showDialog()
            } else
                Snackbar.make(
                    binding.btnCompleteAction,
                    getString(R.string.networkstatus),
                    Snackbar.LENGTH_LONG
                ).show()
        }
        return binding.root
    }

    fun showDialog() {
        val dialogBinding = AlertDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        //to pick time and date for alert
        dialogBinding.fromBtn.setOnClickListener {
            pickTime(dialogBinding, 1)
            pickDate(dialogBinding, 1)
        }
        dialogBinding.toBtn.setOnClickListener {
            pickTime(dialogBinding, 2)
            pickDate(dialogBinding, 2)
        }

        //handle choice bet notfication / alarm
        dialogBinding.timeCalenderRadioGroup.setOnCheckedChangeListener{group , checkedId ->
            if(group.checkedRadioButtonId == R.id.notificationRBtn) {
                edit.putString(SharedKey.ALERT_TYPE.name,"notification")
                edit.commit()
            }
            else if(group.checkedRadioButtonId==R.id.alarmRBtn) {
                edit.putString(SharedKey.ALERT_TYPE.name,"alarm")
                edit.commit()
            }
        }

        //handle save button to create alert
        dialogBinding.OkBtn.setOnClickListener {
            if (timeOne != null && dateOneSelected != null &&
                timeTwo != null && dateTwoSelected != null &&
                (dialogBinding.notificationRBtn.isChecked ||
                        dialogBinding.alarmRBtn.isChecked) &&
                trigerTime(calenderToDate,calenderToTime).timeInMillis >
                trigerTime(calenderFromDate,calenderFromTime).timeInMillis

            ) {
                dialog.dismiss()
                requestCode= (Calendar.getInstance().timeInMillis)-100
                channelCode = requestCode.toString()
                edit.putString("channel",channelCode)
                edit.commit()
                viewModel.insertOneALert(
                    AlertData(
                        timeOne!!,
                        dateOneSelected!!,
                        timeTwo!!,
                        dateTwoSelected!!,
                        calenderFromTime,
                        calenderFromDate,
                        calenderToTime,
                        calenderToDate,
                        requestCode,
                        lon, lat
                    )
                )

                setAlarm(sharedPreferences.getString(SharedKey.ALERT_TYPE.name,"notification").toString())
                dateOneSelected = null
                dateTwoSelected = null
                timeOne = null
                timeTwo = null
            } else
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enterdatainput),
                    Toast.LENGTH_LONG
                ).show()
        }

        dialog.show()
    }


    private fun pickTime(dialogBinding: AlertDialogBinding, choose: Int) {
        val calendarTime = Calendar.getInstance()
        val timePicker = TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
            calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendarTime.set(Calendar.MINUTE, minute)
            calendarTime.timeZone = TimeZone.getDefault()
            updateTimeLabel(calendarTime, dialogBinding, choose)
        }
        TimePickerDialog(
            requireContext(),
            timePicker,
            calendarTime.get(Calendar.HOUR_OF_DAY),
            calendarTime.get(Calendar.MINUTE),
            false
        ).show()
    }
    private fun pickDate(dialogBinding: AlertDialogBinding, choose: Int){
        val calenderDate = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calenderDate.set(Calendar.YEAR, year)
            calenderDate.set(Calendar.MONTH, month)
            calenderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateLabel(calenderDate, dialogBinding, choose)
        }
        DatePickerDialog(
            requireContext(),
            datePicker,
            calenderDate.get(Calendar.YEAR),
            calenderDate.get(Calendar.MONTH),
            calenderDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateTimeLabel(
        calendarTime: Calendar,
        dialogBinding: AlertDialogBinding,
        choose: Int
    ) {
        val format = SimpleDateFormat("hh:mm:aa")
        val time = format.format(calendarTime.time)
        when (choose) {
            1 -> {
                dialogBinding.fromTime.text = time
                timeOne = time
                calenderFromTime = calendarTime.timeInMillis
            }
            2 -> {
                dialogBinding.toTime.text = time
                timeTwo = time
                calenderToTime = calendarTime.timeInMillis
            }

        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateDateLabel(
        calenderDate: Calendar,
        dialogBinding: AlertDialogBinding,
        choose: Int
    ) {
        val day = SimpleDateFormat("dd").format(calenderDate.time)
        val month = SimpleDateFormat("MM").format(calenderDate.time)
        val year = SimpleDateFormat("yyyy").format(calenderDate.time)
        when (choose) {
            1 -> {
                dialogBinding.fromCalender.text = "${day}/${month}/${year}"
                dateOneSelected = "${day}/${month}/${year}"
                calenderFromDate = calenderDate.timeInMillis
            }
            2 -> {
                dialogBinding.toCalender.text = "${day}/${month}/${year}"
                dateTwoSelected = "${day}/${month}/${year}"
                calenderToDate = calenderDate.timeInMillis
            }
        }
    }

    fun deleteAlert(alertData: AlertData) {
        viewModel.deleteOneAlert(alertData)

    }

    private fun setAlarm(type:String) {
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("channel",channelCode)
        intent.putExtra(SharedKey.ALERT_TYPE.name,type)
        intent.putExtra("lat",lat)
        intent.putExtra("lon",lon)

        createNotificationChannel()
        pIntent = PendingIntent.getBroadcast(requireContext(), requestCode.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calenderToTime, pIntent)
    }

    fun trigerTime(Date:Long,Time:Long): Calendar {
        var testCanlender = Calendar.getInstance()
        testCanlender.timeInMillis = Date
        val trigerCalender = Calendar.getInstance()
        trigerCalender.set(Calendar.DAY_OF_MONTH,testCanlender.get(Calendar.DAY_OF_MONTH))
        trigerCalender.set(Calendar.MONTH,testCanlender.get(Calendar.MONTH))
        trigerCalender.set(Calendar.YEAR,testCanlender.get(Calendar.YEAR))
        testCanlender.timeInMillis = Time
        trigerCalender.set(Calendar.HOUR_OF_DAY,testCanlender.get(Calendar.HOUR_OF_DAY))
        trigerCalender.set(Calendar.MINUTE,testCanlender.get(Calendar.MINUTE))
        return trigerCalender
    }
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel display name"
            val description = "Channel from alarm manager"
            var importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelCode, name, importance)
            channel.description = description
            val notificationManager = activity?.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun requestOverAppPermission() {
        startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),20)
    }

    //handling if the user deletes the alert
    override fun onAlarmClick(alertData: AlertData) {
        var noOfDays = alertData.milleDateTo - alertData.milleDateFrom
        val days = TimeUnit.MILLISECONDS.toDays(noOfDays)
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, AlarmReceiver::class.java)

        for (i in 0..days) {
            pIntent =
                PendingIntent.getBroadcast(context, (alertData.requestCode + i).toInt(), intent,
                    PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pIntent)
        }
        deleteAlert(alertData)
    }
}