package com.example.multicastndsapplication.find_ble_devices

/**
 * The interface to listen for ble device connect
 */
interface BLEDeviceConnectCallback {
    /**
     * Invoked when click the specific device
     *
     * @param deviceData The clicked device data
     */
    fun onDeviceClicked(deviceData: DeviceDetails)
}