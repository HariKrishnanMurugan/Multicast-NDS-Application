package com.example.multicastndsapplication.scan_mdns_service

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import com.example.multicastndsapplication.ServiceModel
import com.example.multicastndsapplication.databinding.FragmentScanServiceBinding
import com.google.gson.Gson

/**
 * The Fragment class represents the results of the mDNS scan
 */
class ScanmDNSServiceFragment : Fragment() {
    private var _binding: FragmentScanServiceBinding? = null
    private val scanServiceBinding get() = _binding!!
    private var serviceList: MutableList<ServiceModel>? = null
    private lateinit var mContext: Context

    companion object {
        fun newInstance(bundle: Bundle?) = ScanmDNSServiceFragment().apply { arguments = bundle }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceList = Gson().fromJson(it.getString("ServiceList"), object : TypeToken<List<ServiceModel>>() {}.type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanServiceBinding.inflate(inflater, container, false)
        return scanServiceBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    /**
     * To set the adapter
     */
    private fun setAdapter() {
        with(scanServiceBinding) {
            val isValidList = serviceList != null && serviceList!!.isNotEmpty()
            showEmptyView(!isValidList)
            if (isValidList) {
                val serviceAdapter = ScanmDNSServiceAdapter(serviceList!!)
                rvServices.apply {
                    layoutManager = LinearLayoutManager(mContext)
                    adapter = serviceAdapter
                }
            }
        }
    }

    /**
     * To show the empty view
     *
     * @param isEmptyViewToShow Whether need to show the empty view or not
     */
    private fun showEmptyView(isEmptyViewToShow: Boolean) {
        with(scanServiceBinding) {
            rvServices.isVisible = !isEmptyViewToShow
            tvEmptyView.isVisible = isEmptyViewToShow
        }
    }
}