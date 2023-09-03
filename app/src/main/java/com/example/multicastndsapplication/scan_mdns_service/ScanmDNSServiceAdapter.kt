package com.example.multicastndsapplication.scan_mdns_service

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.multicastndsapplication.ServiceModel
import com.example.multicastndsapplication.databinding.LayoutScanMdnsResultsBinding

/**
 * The adapter class to scan and show the available services
 *
 * @param serviceList The available services list
 */
class ScanmDNSServiceAdapter(private val serviceList: MutableList<ServiceModel>) : RecyclerView.Adapter<ScanmDNSServiceAdapter.ScanmDNSViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanmDNSViewHolder {
        return ScanmDNSViewHolder(LayoutScanMdnsResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ScanmDNSViewHolder, position: Int) {
        holder.bind(serviceList[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    /**
     * The view holder class for Scan mDNS results
     *
     * @property scanmDNSResultsBinding The Sacn mDNS results layout
     */
    inner class ScanmDNSViewHolder(private val scanmDNSResultsBinding: LayoutScanMdnsResultsBinding) : ViewHolder(scanmDNSResultsBinding.root) {
        /**
         * To bind the mDNS Scanned services info to the view
         *
         * @param serviceModel The services info
         */
        fun bind(serviceModel: ServiceModel) {
            with(scanmDNSResultsBinding) {
                tvServiceName.text = serviceModel.serviceName
                tvServiceType.text = serviceModel.serviceType
                viewSeparator.isVisible = adapterPosition != serviceList.size - 1
            }
        }
    }
}