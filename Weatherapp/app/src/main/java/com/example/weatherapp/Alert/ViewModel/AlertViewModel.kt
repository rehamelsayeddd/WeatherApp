package com.example.weatherapp.Alert.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.Repository.IWeatherRepository
import com.example.weatherapp.Repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertViewModel (private val repo: IWeatherRepository): ViewModel() {
    private var _alertData: MutableLiveData<List<AlertData>> = MutableLiveData<List<AlertData>>()
    val alerts: LiveData<List<AlertData>> = _alertData
    init {
        getAlerts()
    }


    fun insertOneALert(alertData: AlertData){
        viewModelScope.launch (Dispatchers.IO){
            repo.insertAlert(alertData)
            getAlerts()
        }

    }
    fun getAlerts() {
        viewModelScope.launch (Dispatchers.IO){
            repo.getAlertsData()?.collect{
                _alertData.postValue(it)
            }
        }
    }
    fun deleteOneAlert(alertData: AlertData){
        viewModelScope.launch (Dispatchers.IO){
            repo.deleteAlert(alertData)
            getAlerts()
        }
    }


}
