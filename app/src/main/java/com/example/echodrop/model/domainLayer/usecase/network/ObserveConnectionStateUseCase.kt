package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.model.ConnectionState
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing connection state.
 *
 * @property transportManager The manager that handles connections.
 */
class ObserveConnectionStateUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    /**
     * Invokes the use case to observe the connection state.
     *
     * @return A flow with connection state information.
     */
    operator fun invoke(): Flow<ConnectionState> = transportManager.observeConnectionState()
}