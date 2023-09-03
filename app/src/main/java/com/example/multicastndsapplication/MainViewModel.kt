package com.example.multicastndsapplication

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * The view model class for publish, Scan mDNS services and connect with them. It handles the business logic to communicate with the
 * services and provides the data to the observing UI component.
 */
@HiltViewModel
open class MainViewModel @Inject constructor() : ViewModel()