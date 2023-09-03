package com.example.multicastndsapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.multicastndsapplication.databinding.ActivityServiceBinding
import com.example.multicastndsapplication.find_ble_devices.FindBleDevicesFragment
import com.example.multicastndsapplication.scan_mdns_service.ScanmDNSServiceFragment

/**
 * The activity class the represents the mDNS service integration
 */
class ServiceActivity : AppCompatActivity() {
    private lateinit var serviceActivityBinding: ActivityServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceActivityBinding = ActivityServiceBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        loadFragment(intent?.extras)
    }

    /**
     * To load the fragment
     *
     * @param bundle The bundle data
     */
    private fun loadFragment(bundle: Bundle?) {
        val fragmentToReplace = when (bundle?.getString("ScreenName")) {
            "scan_screen" -> ScanmDNSServiceFragment.newInstance(bundle)
            else -> FindBleDevicesFragment.newInstance()
        }
        replaceFragment(fragmentToReplace)
    }

    /**
     * To replace the fragments
     *
     * @param fragmentToReplace The fragment to replace
     */
    private fun replaceFragment(fragmentToReplace: Fragment) {
        val fragmentTransaction = this.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(serviceActivityBinding.flServiceContainer.id, fragmentToReplace).commit()
    }
}