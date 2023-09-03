package com.example.multicastndsapplication.publish_mdns_service

/**
 * The class representing the publish service name, type, ip and port
 */
data class PublishServiceModel(val serviceName: String, val serviceType: String, val ip: String, val port: String)