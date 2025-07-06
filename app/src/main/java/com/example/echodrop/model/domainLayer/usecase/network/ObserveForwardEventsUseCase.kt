package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveForwardEventsUseCase @Inject constructor(
    private val transportManager: TransportManager
) {
    operator fun invoke(): Flow<ForwardEvent> = transportManager.observeForwardEvents()
} 