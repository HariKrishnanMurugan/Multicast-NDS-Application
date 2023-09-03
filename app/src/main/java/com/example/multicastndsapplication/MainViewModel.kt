package com.example.multicastndsapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The view model class for publish, Scan mDNS services and connect with them. It handles the business logic to communicate with the
 * services and provides the data to the observing UI component.
 */
@HiltViewModel
open class MainViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    private val result = MutableStateFlow<ServiceResult?>(null)
    val publishResult = result.asStateFlow()

    /**
     * To publish the service
     */
    fun publishService() {
        viewModelScope.launch {
            repo.publishmDNSService(80).collect {
                // Set the result value to the flow
                result.value = it
            }
        }
    }
}