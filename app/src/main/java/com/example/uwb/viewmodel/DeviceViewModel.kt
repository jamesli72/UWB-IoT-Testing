package com.example.uwb.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.uwb.model.DeviceModel

class DeviceViewModel : ViewModel() {

    private val _scannedDeviceLiveData: MutableLiveData<List<DeviceModel>> by lazy {
        MutableLiveData<List<DeviceModel>>()
    }
    val scannedDeviceLiveData : LiveData<List<DeviceModel>> = _scannedDeviceLiveData

    private val _uuidOfUWB : MutableLiveData<Array<String>> by lazy {
        MutableLiveData<Array< String>>()
    }
    val uuidOfUWB : LiveData<Array< String>> = _uuidOfUWB

    fun getUuidArray(parcelUuids: Array< String>) {
        _uuidOfUWB.postValue(parcelUuids)
    }


    fun handleScanResult(deviceModel: DeviceModel) {
        //Get the current list of scanned devices
        val currentList = _scannedDeviceLiveData.value.orEmpty().toMutableList()
        //Create a new DeviceModel object with the scanned device data
        val existingDevice = currentList.find { it.deviceAddress == deviceModel.deviceAddress }

        if (existingDevice == null) {
            // Device does not exist in the list, add it
            currentList.add(deviceModel)
            _scannedDeviceLiveData.value = currentList
        }
    }

    fun resetResult() {
        _scannedDeviceLiveData.value = listOf()
    }
}