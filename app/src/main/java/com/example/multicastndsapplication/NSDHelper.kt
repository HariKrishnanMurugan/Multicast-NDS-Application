package com.example.multicastndsapplication

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
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
                    val serviceName = service.serviceName
                    when {
                        service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and transport layer for this service.
                            Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                        SERVICE_NAME2 == serviceName -> // The name of the service tells the user what they'd be connecting to.
                            Log.d(TAG, "Same machine: $serviceName")

                        service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(service, resolveListener)
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
    private fun tearDown() {
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
}