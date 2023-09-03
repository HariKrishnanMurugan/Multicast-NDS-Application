package com.example.multicastndsapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.multicastndsapplication.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * This is the main basic activity to initiate the mDNS services
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        setListeners()
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
                TODO("Not yet implemented")
            }

            btnFindBle.setOnClickListener {
                TODO("Not yet implemented")
            }
        }
    }

    /**
     * To Show the toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}