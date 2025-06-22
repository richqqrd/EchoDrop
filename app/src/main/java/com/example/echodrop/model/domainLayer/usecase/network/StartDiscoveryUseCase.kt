package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.TransportManager
import javax.inject.Inject

/**
 * Use case for starting device discovery.
 *
 * @property transportManager The manager that handles device discovery.
 */
class StartDiscoveryUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    /**
     * Invokes the use case to start device discovery.
     */
    suspend operator fun invoke() = transportManager.startDiscovery()
}