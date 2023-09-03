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
    private lateinit var nsdManager: NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null

    companion object {
        private const val SERVICE_NAME = "MyService001"
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
            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
                registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            }
            awaitClose()
        }
    }

    /**
     * To tear down the NSD method
     */
    private fun tearDown() {
        if (::nsdManager.isInitialized) {
            nsdManager.apply {
                unregisterService(registrationListener)
            }
        }
    }
}