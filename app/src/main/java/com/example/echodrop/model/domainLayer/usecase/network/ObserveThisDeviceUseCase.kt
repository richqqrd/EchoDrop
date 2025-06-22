package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing information about this device.
 *
 * @property transportManager The manager that provides device information.
 */
class ObserveThisDeviceUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    /**
     * Invokes the use case to observe information about this device.
     *
     * @return A flow with information about this device.
     */
    operator fun invoke(): Flow<DeviceInfo?> = transportManager.observeThisDevice()
}