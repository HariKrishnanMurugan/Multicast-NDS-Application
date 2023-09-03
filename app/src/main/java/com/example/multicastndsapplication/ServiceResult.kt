package com.example.multicastndsapplication

/**
 * The service result
 */
sealed class ServiceResult {
    data class Success<out T>(val data: T) : ServiceResult()
    data class Error(val serviceName: String, val errorMessage: String?) : ServiceResult()
}