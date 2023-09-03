package com.example.multicastndsapplication.find_ble_devices

import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.multicastndsapplication.Repository
import com.example.multicastndsapplication.ServiceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The view model class for Scan Nearby BLE devices and connect with them. It handles the business logic to communicate with the
 * services and provides the data to the observing UI component.
 */
@HiltViewModel
class FindBleDevicesViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private val result = MutableStateFlow<ServiceResult?>(null)
    val scanResult = result.asStateFlow()
    var scannedDevicesList = mutableListOf<DeviceDetails>()
    var distinctList: MutableList<DeviceDetails>? = null
    private val pairResult = MutableStateFlow<ServiceResult?>(null)
    val pairResultFlow = pairResult.asStateFlow()

    /**
     * To scan the nearby BLE devices
     */
    fun scanBLEDevices() {
        viewModelScope.launch {
            repo.findBLEDevices().collect {
                // Scan result value
                result.value = it
            }
        }
    }

    /**
     * To stop the scan
     */
    fun stopBLEDeviceScan() {
        viewModelScope.launch {
            repo.stopBLEDevicesScan()
        }
    }

    /**
     * To connect the selected BLE device
     *
     * @param deviceData The selected device details
     */
    fun connectBLEDevice(deviceData: DeviceDetails) {
        viewModelScope.launch {
            repo.pairBLEDevices(deviceData).collect {
                pairResult.value = it
            }
        }
    }

    /**
     * To distinct list
     *
     * @param resultData The scan result
     */
    fun distinctList(resultData: ScanResult) {
        val list = DeviceDetails(resultData.device)
        scannedDevicesList.add(list)
        distinctList = scannedDevicesList.distinct().toMutableList()
    }
}