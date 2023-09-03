package com.example.multicastndsapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.multicastndsapplication.databinding.ActivityMainBinding
import com.example.multicastndsapplication.find_ble_devices.FindBleDevicesActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.PermissionUtils
import permissions.dispatcher.ktx.LocationPermission
import permissions.dispatcher.ktx.constructLocationPermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest

/**
 * This is the main basic activity to initiate the mDNS services
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var bluetoothAdapter: BluetoothAdapter

    companion object {
        private val TAG = this::class.java.simpleName
        private const val ONE_SECOND_IN_MILLISECONDS = 1000L
    }

    /**
     * The location settings result
     */
    private val locationSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { _result ->
        when (_result.resultCode) {
            Activity.RESULT_OK -> {
                showToast(getString(R.string.gps_successfully_enables))
                startActivity(Intent(this@MainActivity, FindBleDevicesActivity::class.java))
            }
            Activity.RESULT_CANCELED -> checkLocationSettings()
        }
    }

    /**
     * The activity result for the bluetooth permission
     */
    private val bluetoothPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null && result.resultCode == RESULT_OK) {
            checkNeededPermissionAndAsk()
        } else {
            showToast(getString(R.string.try_again))
            enableBluetooth()
        }
    }

    /**
     * The activity permission result for location
     */
    private val settingsPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            checkNeededPermissionAndAsk()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        setListeners()
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.tearDownListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.tearDownListener()
    }

    /**
     * To set the listeners
     */
    private fun setListeners() {
        with(mainActivityBinding) {
            btnPublish.setOnClickListener {
                lifecycleScope.launch {
                    mainViewModel.publishResult.collect { result ->
                        when (result) {
                            is ServiceResult.Success<*> -> {
                                showToast("${result.data} is Successfully registered")
                            }

                            is ServiceResult.Error -> {
                                showToast("${result.serviceName} is unable to register due to this ${result.errorMessage}")
                            }
                            else -> {
                                // No need to handle
                            }
                        }
                    }
                }
                mainViewModel.publishService()
            }

            btnScan.setOnClickListener {
                lifecycleScope.launch {
                    mainViewModel.scanResults.collect { result ->
                        when (result) {
                            is ServiceResult.Success<*> -> {
                                mainViewModel.serviceList = result.data as MutableList<ServiceModel>
                                if (mainViewModel.serviceList.isNotEmpty()) {
                                    startActivity(
                                        Intent(this@MainActivity, ServiceActivity::class.java).putExtras(
                                            bundleOf("ServiceList" to Gson().toJson(mainViewModel.serviceList))
                                        )
                                    )
                                } else showToast("No Devices are available now")
                            }

                            is ServiceResult.Error -> {
                                showToast("${result.serviceName} is unable to scan due to this ${result.errorMessage}")
                            }
                            else -> {
                                // No need to handle
                            }
                        }
                    }
                }
                mainViewModel.scanDevices()
            }

            btnFindBle.setOnClickListener {
                enableBluetooth()
            }
        }
    }

    /**
     * To Show the toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * To enable the bluetooth device
     */
    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            if (checkSDKVersionAbove31()) checkNeededPermissionAndAsk()
            else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothPermissionResult.launch(intent)
            }
        } else {
            checkNeededPermissionAndAsk()
        }
    }

    /**
     * To check the SDK version above android 12
     *
     * @return Whether the device sdk is 31 and above
     */
    private fun checkSDKVersionAbove31(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * To check and ask the needed permission
     */
    private fun checkNeededPermissionAndAsk() {
        if (checkSDKVersionAbove31()) {
            if (haveBluetoothScanPermissionGranted()) askLocationPermissions()
            else askBluetoothPermissions()
        } else askLocationPermissions()
    }

    /**
     * The necessary bluetooth permissions are granted or not
     *
     * @return Whether the bluetooth permissions are granted or not
     */
    private fun haveBluetoothScanPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && PermissionUtils.hasSelfPermissions(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) && PermissionUtils.hasSelfPermissions(this, Manifest.permission.BLUETOOTH_CONNECT)
    }

    /**
     * To ask the location permissions
     */
    private fun askLocationPermissions() {
        val constructLocationPermissionResult = constructLocationPermissionRequest(
            permissions = arrayOf(LocationPermission.COARSE, LocationPermission.FINE),
            requiresPermission = ::checkLocationSettings,
            onNeverAskAgain = { goToAppPermissionSettings(R.string.need_location_permission) },
            onShowRationale = ::showRationaleDialog,
        )
        constructLocationPermissionResult.launch()
    }

    /**
     * To ask the bluetooth permission
     */
    @SuppressLint("InlinedApi")
    private fun askBluetoothPermissions() {
        val constructBluetoothPermissionResult = constructPermissionsRequest(
            permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
            requiresPermission = ::askLocationPermissions,
            onNeverAskAgain = { goToAppPermissionSettings(R.string.need_bluetooth_permission) },
            onShowRationale = ::showRationaleDialog,
        )
        constructBluetoothPermissionResult.launch()
    }

    /**
     * To check if the device's location settings are adequate for the app's needs using
     */
    private fun checkLocationSettings() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val isGpsProviderEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        if (!isGpsProviderEnabled) {
            if (BaseApplication.isGpsEnabled) {
                startActivity(Intent(this@MainActivity, FindBleDevicesActivity::class.java))
            } else {
                BaseApplication.isGpsEnabled = true
                val locationRequest = LocationRequest.create().apply {
                    interval = ONE_SECOND_IN_MILLISECONDS
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                }
                val builder = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest)
                    setAlwaysShow(true)
                }
                val locationSettingsRequest = builder.build()
                val result = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest)
                result.addOnCompleteListener { task ->
                    try {
                        //If this line is successfully executed( without exception) all location settings are satisfied.
                        if (task.isComplete) {
                            task.getResult(ApiException::class.java)
                            startActivity(Intent(this@MainActivity, FindBleDevicesActivity::class.java))
                            Log.d(TAG, "All location settings are satisfied.")
                        }
                    } catch (exception: ApiException) {
                        when (exception.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                //Open the settings page to change the settings.
                                val resolvable = exception as ResolvableApiException
                                val intentSenderRequest = IntentSenderRequest.Builder(resolvable.resolution).build()
                                locationSettingsLauncher.launch(intentSenderRequest)
                            } catch (e: IntentSender.SendIntentException) {
                                val sendIntentExceptionMessage = "checkLocationSettings: SendIntentException: ${e.message}"
                                Log.e(TAG, sendIntentExceptionMessage, e)
                            } catch (e: ClassCastException) {
                                val classCastExceptionMessage = "checkLocationSettings: ClassCastException: ${e.message}"
                                Log.e(TAG, classCastExceptionMessage, e)
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                // Settings page not available
                                val apiExceptionMessage = "checkLocationSettings: ApiException: ${exception.message}"
                                Log.e(TAG, apiExceptionMessage, exception)
                            }
                        }
                    }
                }
            }
        } else startActivity(Intent(this@MainActivity, FindBleDevicesActivity::class.java))
    }

    /**
     * To go to app permission settings page
     */
    private fun goToAppPermissionSettings(toastMessageId: Int) {
        try {
            showToast(getString(toastMessageId))
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            settingsPermissionResult.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "goToAppPermissionSettings: Caught exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "goToAppPermissionSettings: Caught exception: ${e.message}")
        }
    }

    /**
     * To show the rationale dialog for location permission
     *
     * @param request The permission request
     */
    private fun showRationaleDialog(request: PermissionRequest) {
        AlertDialog.Builder(this).setPositiveButton(R.string.allow) { _, _ -> request.proceed() }.setCancelable(false)
            .setMessage(R.string.app_permission_rationale).show()
    }
}