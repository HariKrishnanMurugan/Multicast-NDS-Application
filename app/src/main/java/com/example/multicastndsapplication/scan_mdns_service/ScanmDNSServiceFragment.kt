package com.example.multicastndsapplication.scan_mdns_service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.multicastndsapplication.databinding.FragmentScanServiceBinding

/**
 * The Fragment class represents the results of the mDNS scan
 */
class ScanmDNSServiceFragment : Fragment() {
    private var _binding: FragmentScanServiceBinding? = null
    private val scanServiceBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanServiceBinding.inflate(inflater, container, false)
        return scanServiceBinding.root
    }
}