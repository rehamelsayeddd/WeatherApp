package com.example.weatherapp.Alert.View

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.Home.View.HomeFragment.Companion.API_KEY
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.OneCall.Model.OneCallApi
import com.example.weatherapp.R
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.SharedKey
import com.example.weatherapp.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AlarmReceiver : BroadcastReceiver(){

    private lateinit var CHANEL:String
    private lateinit var type:String
    private lateinit var  repository: WeatherRepository
    private lateinit var alertResults:OneCallApi
    private lateinit var sharedPreferencesManager: SharedPreferenceManager

    lateinit var remoteDataSource: RemoteData
    lateinit var localDataSourceInte: LocalData
    var language : String =""

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        sharedPreferencesManager = SharedPreferenceManager.getInstance(context!!)
        CHANEL = intent?.getStringExtra("channel").toString()
        type = intent?.getStringExtra(SharedKey.ALERT_TYPE.name).toString()
        remoteDataSource = RemoteData()
        localDataSourceInte = context?.let { LocalData(it) }!!

        val lat = intent?.getStringExtra("lat").toString().toDouble()
        val lon = intent?.getStringExtra("lon").toString().toDouble()
        language = sharedPreferencesManager.getLanguage(SharedKey.LANGUAGE.name, "")
        var alertMessage= getString(context,R.string.nowarnings)
        repository = WeatherRepository.getInstance(remoteDataSource,localDataSourceInte)

        //fetching from api
        CoroutineScope(Dispatchers.IO).launch {
            repository.getAlerts(lat,lon,"metric", API_KEY,language).collect{ alertsData->
                alertResults = alertsData!!
            }

            if(!alertResults.alerts.isNullOrEmpty())
                alertMessage = (alertResults.alerts!!.get(0).event)

            if (type.equals("notification")){
                val destinationIntent = Intent(context, AlertFragment::class.java)
                intent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                val pendingIntent = PendingIntent.getActivity(context, 0,destinationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
                val expandedText = NotificationCompat.BigTextStyle()
                if(language=="ar"){
                    expandedText.bigText("الطقس الآن: ${ alertResults.current.weather[0].description}")

                }else {
                    expandedText.bigText("Current: ${ alertResults.current.weather[0].description}")
                }
                val builder = NotificationCompat.Builder(context!!, CHANEL)
                    .setSmallIcon(R.drawable.alert)
                    .setContentTitle(alertResults.timezone)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setStyle(expandedText)
                    .setAutoCancel(true)
                    .setContentText(alertMessage )


                val notificationManager = NotificationManagerCompat.from(context)

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {}
                notificationManager.notify(1, builder.build())
            }
            else{
                alarmBuilder(context,alertMessage)
            }

        }

    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private suspend fun alarmBuilder(context: Context, msgInfo:String) {
        val LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI)

        val view = LayoutInflater.from(context).inflate(R.layout.alarm_window, null, false)
        val message = view.findViewById<TextView>(R.id.message)
        val btnDimiss = view.findViewById<Button>(R.id.btnDismiss)
        val description = view.findViewById<TextView>(R.id.descAlert)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        withContext(Dispatchers.Main) {
            windowManager.addView(view, layoutParams)
            message.text = msgInfo
            if (language =="ar"){
                description.text = "الطقس الآن: ${alertResults.current.weather[0].description}"
                btnDimiss.text="تجاهل"
            }else {
                description.text = "Current: ${alertResults.current.weather[0].description}"
                btnDimiss.text="Dismiss"

            }
            view.visibility = View.VISIBLE


        }
        mediaPlayer.start()
        mediaPlayer.isLooping = true
        btnDimiss.setOnClickListener {
            mediaPlayer?.release()
            windowManager.removeView(view)
        }
    }
}
