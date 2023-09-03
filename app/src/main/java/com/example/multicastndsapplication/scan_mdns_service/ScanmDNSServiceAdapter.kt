package com.example.multicastndsapplication.scan_mdns_service

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.multicastndsapplication.databinding.LayoutScanMdnsResultsBinding

/**
 * The adapter class to scan and show the available services
 */
class ScanmDNSServiceAdapter() : RecyclerView.Adapter<ScanmDNSServiceAdapter.ScanmDNSViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanmDNSViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ScanmDNSViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    /**
     * The view holder class for Scan mDNS results
     *
     * @property scanmDNSResults The Sacn mDNS results layout
     */
    inner class ScanmDNSViewHolder(private val scanmDNSResultsBinding: LayoutScanMdnsResultsBinding) : ViewHolder(scanmDNSResultsBinding.root) {
        /**
         * To bind the mDNS Scanned services info to the view
         */
        fun bind() {
            TODO("Not yet implemented")
        }
    }
}