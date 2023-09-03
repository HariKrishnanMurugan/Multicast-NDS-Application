package com.example.multicastndsapplication.find_ble_devices

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.multicastndsapplication.databinding.LayoutBleDevicesBinding

/**
 * The adapter class to show the nearby BLE devices
 *
 * @param deviceList The bluetooth devices info
 */
class BLEDevicesAdapter(private val deviceList: MutableList<DeviceDetails>, private val deviceConnectCallback: BLEDeviceConnectCallback) :
    RecyclerView.Adapter<BLEDevicesAdapter.BLEDevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLEDevicesViewHolder {
        return BLEDevicesViewHolder(LayoutBleDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BLEDevicesViewHolder, position: Int) {
        holder.bind(deviceList[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    /**
     * The view holder class for nearby BLE devices
     *
     * @property bleDevicesBinding The BLE devices layout
     */
    inner class BLEDevicesViewHolder(private val bleDevicesBinding: LayoutBleDevicesBinding) : ViewHolder(bleDevicesBinding.root) {
        /**
         * To bind the ble devices info to the view
         *
         * @param deviceDetail The bluetooth details
         */
        @SuppressLint("MissingPermission")
        fun bind(deviceDetail: DeviceDetails) {
            with(bleDevicesBinding) {
                tvDeviceName.text = deviceDetail.bluetoothDeviceInfo.name
                tvDeviceAddress.text = deviceDetail.bluetoothDeviceInfo.address
                viewSeparator.isVisible = adapterPosition != deviceList.size - 1
                root.setOnClickListener {
                    deviceConnectCallback.onDeviceClicked(deviceDetail)
                }
            }
        }
    }
}