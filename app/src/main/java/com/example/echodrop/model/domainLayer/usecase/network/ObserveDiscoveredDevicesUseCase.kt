package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing discovered devices.
 *
 * @property transportManager The manager that handles device discovery.
 */
class ObserveDiscoveredDevicesUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    /**
     * Invokes the use case to observe discovered devices.
     *
     * @return A flow of discovered devices.
     */
    operator fun invoke(): Flow<List<DeviceInfo>> = transportManager.observeDiscoveredDevices()
}