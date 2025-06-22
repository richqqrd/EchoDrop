package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.TransportManager
import javax.inject.Inject

/**
 * Use case for connecting to a specific device.
 *
 * @property transportManager The manager that handles connections.
 */
class ConnectToDeviceUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    /**
     * Invokes the use case to connect to a specific device.
     *
     * @param deviceAddress The MAC address of the device to connect to.
     */
    suspend operator fun invoke(deviceAddress: String) = transportManager.connectToDevice(deviceAddress)
}