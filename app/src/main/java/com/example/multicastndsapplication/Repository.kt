package com.example.multicastndsapplication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Repository class to handle the publish service, scan and find BLE devices operation
 */
@Singleton
class Repository @Inject constructor(private val nsdHelper: NSDHelper) {

    /**
     * To Publish the mDNS service
     *
     * @param portValue The port value
     * @return The service result
     */
    fun publishmDNSService(portValue: Int): Flow<ServiceResult> {
        return flow {
            emitAll(nsdHelper.registerService(portValue))
        }
    }

    /**
     * To scan the mDNS services
     *
     * @param serviceList The service model list
     * @return The service result
     */
    fun scanmDNSService(serviceList: MutableList<ServiceModel>): Flow<ServiceResult> {
        return flow { emitAll(nsdHelper.discoverServices(serviceList)) }
    }

    /**
     * To find the ble devices
     *
     * @return The scan result
     */
    fun findBLEDevices(): Flow<ServiceResult> {
        return flow { emitAll(nsdHelper.scanBleDevices()) }
    }

    /**
     * To pair the ble devices
     */
    fun pairBLEDevices() {
        TODO("Not yet implemented")
    }

    fun stopListener() {
        nsdHelper.tearDown()
    }

    /**
     * To stop the BLE Device scan
     */
    fun stopBLEDevicesScan() {
        nsdHelper.stopScan()
    }
}