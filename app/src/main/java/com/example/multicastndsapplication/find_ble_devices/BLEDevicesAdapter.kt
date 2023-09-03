package com.example.multicastndsapplication.find_ble_devices

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.multicastndsapplication.databinding.LayoutBleDevicesBinding

/**
 * The adapter class to show the nearby BLE devices
 */
class BLEDevicesAdapter() : RecyclerView.Adapter<BLEDevicesAdapter.BLEDevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLEDevicesViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: BLEDevicesViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    /**
     * The view holder class for nearby BLE devices
     *
     * @property bleDevicesBinding The BLE devices layout
     */
    inner class BLEDevicesViewHolder(private val bleDevicesBinding: LayoutBleDevicesBinding) : ViewHolder(bleDevicesBinding.root) {
        /**
         * To bind the ble devices info to the view
         */
        fun bind() {
            TODO("Not yet implemented")
        }
    }
}