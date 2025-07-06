package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.TransportManager
import javax.inject.Inject

/**
 * Use case to stop beaconing (device discovery + Wi-Fi Direct service).
 */
class StopBeaconingUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    operator fun invoke() = transportManager.stopBeaconing()
} 