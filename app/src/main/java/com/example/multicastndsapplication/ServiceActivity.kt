package com.example.multicastndsapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.multicastndsapplication.databinding.ActivityServiceBinding

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
        // Need to handle
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