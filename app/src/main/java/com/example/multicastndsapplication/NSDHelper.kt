package com.example.multicastndsapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.multicastndsapplication.find_ble_devices.DeviceDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * To publish the mDNS service
 */
class NSDHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val TAG: String = this::class.java.simpleName
    private val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private var scanCallback: ScanCallback? = null

    companion object {
        private const val SERVICE_NAME = "MyService001"
        private const val SERVICE_NAME2 = "MyService002"
        private const val SERVICE_TYPE = "_http._tcp."
    }

    /**
     * To register the service
     *
     * @param portValue The port value
     * @return The service result
     */
    fun registerService(portValue: Int): Flow<ServiceResult> {
        return callbackFlow {
            tearDown()
            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                    Log.d(TAG, "initializeRegistrationListener: Service ${NsdServiceInfo.serviceName} registered")
                    trySend(ServiceResult.Success(NsdServiceInfo.serviceName))
                    close()
                }

                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    trySend(ServiceResult.Error(serviceInfo.serviceName, "Registration Failed"))
                    close()
                }

                override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                    trySend(ServiceResult.Error(arg0.serviceName, "Service Unregistered"))
                    close()
                }

                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    trySend(ServiceResult.Error(serviceInfo.serviceName, "UnRegistration Failed"))
                    close()
                }
            }
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = SERVICE_NAME
                serviceType = SERVICE_TYPE
                port = portValue
            }
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            awaitClose()
        }
    }

    /**
     * To discover the service
     *
     * @param serviceList The service model list
     * @return The service result
     */
    fun discoverServices(serviceList: MutableList<ServiceModel>): Flow<ServiceResult> {
        return callbackFlow {
            tearDown()
            resolveListener = object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    // Called when the resolve fails. Use the error code to debug.
                    trySend(ServiceResult.Error(serviceInfo.serviceName, "Resolve Failed"))
                    close()
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {

                    if (SERVICE_NAME2 == serviceInfo.serviceName) {
                        trySend(ServiceResult.Error(serviceInfo.serviceName, "Same IP"))
                        close()
                        return
                    }

                    with(serviceInfo) {
                        serviceList.add(ServiceModel(serviceName, serviceType, host.toString(), port.toString()))
                    }

                    trySend(ServiceResult.Success(serviceList))
                    close()
                }
            }
            val discoveryListener = object : NsdManager.DiscoveryListener {
                // Called as soon as service discovery begins.
                override fun onDiscoveryStarted(regType: String) {
                    Log.d(TAG, "Service discovery started")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    // A service was found! Do something with it.
                    try {
                        val serviceName = service.serviceName
                        when {
                            service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and transport layer for this service.
                                Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                            SERVICE_NAME2 == serviceName -> // The name of the service tells the user what they'd be connecting to.
                                Log.d(TAG, "Same machine: $serviceName")

                            service.serviceName == SERVICE_NAME -> nsdManager.resolveService(service, resolveListener)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onServiceFound: Caught Exception: ${e.message}")
                        trySend(ServiceResult.Error(service.serviceName, "Service Already found"))
                        close()
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    // When the network service is no longer available.
                    // Internal bookkeeping code goes here.
                    trySend(ServiceResult.Error(service.serviceName, "Service Lost"))
                    close()
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    Log.d(TAG, "Discovery stopped: $serviceType")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Discovery failed: Error code:$errorCode")
                    nsdManager.stopServiceDiscovery(this)
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Discovery failed: Error code:$errorCode")
                    nsdManager.stopServiceDiscovery(this)
                }
            }
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            awaitClose()
        }
    }

    /**
     * To tear down the NSD method
     */
    fun tearDown() {
        nsdManager.apply {
            registrationListener?.let {
                unregisterService(registrationListener)
                registrationListener = null
            }
            discoveryListener?.let {
                stopServiceDiscovery(discoveryListener)
                resolveListener = null
                discoveryListener = null
            }
        }
    }

    /**
     * To scan te BLE devices
     *
     * @return The BLE results
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevices(): Flow<ServiceResult> {
        return callbackFlow {
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    // Handle the discovered Bluetooth device
                    trySend(ServiceResult.Success(result))
                    close()
                }

                override fun onBatchScanResults(results: List<ScanResult?>?) {
                    // No need to handle
                }

                override fun onScanFailed(errorCode: Int) {
                    trySend(ServiceResult.Error("", "Scan Failed"))
                    close()
                }
            }
            bluetoothLeScanner?.startScan(scanCallback)
            awaitClose()
        }
    }

    /**
     * To stop the BLE scan
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    /**
     * To connect the bluetooth device
     *
     * @param deviceData The device data
     */
    @SuppressLint("MissingPermission")
    fun connectBluetoothDevice(deviceData: DeviceDetails): Flow<ServiceResult> {
        return callbackFlow {
            val bluetoothDeviceInfo = deviceData.bluetoothDeviceInfo
            val pairingBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        when (it.action) {
                            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                                val bondState = it.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                                val previousBondState = it.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                                when (bondState) {
                                    BluetoothDevice.BOND_BONDING -> {
                                        // Device is currently bonding
                                        trySend(ServiceResult.Success("Pairing"))
                                        close()
                                    }
                                    BluetoothDevice.BOND_BONDED -> {
                                        // Device has successfully bonded
                                        trySend(ServiceResult.Success("Paired"))
                                        close()
                                    }
                                    BluetoothDevice.BOND_NONE -> {
                                        // Bonding failed or canceled
                                        trySend(ServiceResult.Success("Pairing Cancelled"))
                                        close()
                                    }
                                    else -> {
                                        trySend(ServiceResult.Error(bluetoothDeviceInfo.name, "Unable to pair"))
                                        close()
                                    }
                                }
                            }
                            else -> {
                                trySend(ServiceResult.Error(bluetoothDeviceInfo.name, "Unable to pair"))
                                close()
                            }
                        }
                    }
                }
            }
            // Device is already bonded
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(pairingBroadcastReceiver, filter)
            if (BluetoothDevice.BOND_BONDED == bluetoothDeviceInfo.bondState) trySend(ServiceResult.Success("Already Paired"))
            // Initiate the pairing process
            bluetoothDeviceInfo.createBond()
            awaitClose()
        }
    }
}