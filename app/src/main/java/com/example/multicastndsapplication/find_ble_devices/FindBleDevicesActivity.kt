package com.example.multicastndsapplication.find_ble_devices

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.multicastndsapplication.R
import com.example.multicastndsapplication.ServiceResult
import com.example.multicastndsapplication.databinding.ActivityFindBleDevicesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The Fragment class represents to find the nearby ble devices and try to pair with them
 */
@AndroidEntryPoint
class FindBleDevicesActivity : AppCompatActivity() {
    private lateinit var findBleDevicesBinding: ActivityFindBleDevicesBinding
    private val findBleViewModel: FindBleDevicesViewModel by viewModels()

    companion object {
        private val TAG = this::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findBleDevicesBinding = ActivityFindBleDevicesBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        startScan()
        observeViewModel()
    }

    /**
     * To start the scan
     */
    private fun startScan() {
        with(findBleViewModel) {
            findBleViewModel.scanBLEDevices()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    stopBLEDeviceScan()
                    val list = findBleViewModel.distinctList
                    showEmptyView(!(list != null && list.isNotEmpty()))
                },
                5000
            )
            showToast(getString(R.string.scanning))
        }
    }

    /**
     * To observe view model
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            findBleViewModel.scanResult.collect { result ->
                when (result) {
                    is ServiceResult.Success<*> -> {
                        with(findBleViewModel) {
                            val resultData = result.data as ScanResult
                            distinctList(resultData)
                        }
                        setAdapter()
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
    }

    /**
     * To set the adapter
     */
    private fun setAdapter() {
        with(findBleDevicesBinding) {
            val deviceList = findBleViewModel.distinctList
            val isValidList = deviceList != null && deviceList.isNotEmpty()
            showEmptyView(!isValidList)
            if (isValidList) {
                val bleDeviceAdapter = BLEDevicesAdapter(deviceList!!)
                rvBleDevices.apply {
                    layoutManager = LinearLayoutManager(this@FindBleDevicesActivity)
                    adapter = bleDeviceAdapter
                }
            }
        }
    }

    /**
     * To Show the toast message
     *
     * @param message The toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * To show the empty view
     *
     * @param isEmptyViewToShow Whether need to show the empty view or not
     */
    private fun showEmptyView(isEmptyViewToShow: Boolean) {
        with(findBleDevicesBinding) {
            rvBleDevices.isVisible = !isEmptyViewToShow
            tvEmptyView.isVisible = isEmptyViewToShow
        }
    }
}