package com.example.echodrop.model.domainLayer.transport

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo


interface DeviceDiscovery {
    val discoveredDevices: StateFlow<List<WifiP2pDevice>>
    val connectionInfo: StateFlow<WifiP2pInfo?>
    val thisDevice: StateFlow<WifiP2pDevice?>

    fun startDiscovery()
    fun stopDiscovery()
    fun connectToDevice(deviceAddress: String)
    fun disconnectFromCurrentGroup()
} 