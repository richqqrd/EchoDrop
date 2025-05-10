package com.example.echodrop.model.domainLayer.transport

import kotlinx.coroutines.flow.Flow

interface TransportManager {

    fun startBeaconing()

    fun stopBeaconing()

    suspend fun sendManifest(paketId: String, manifestJson: String)

    suspend fun sendChunk(chunkId: String, data: ByteArray)

    fun observeIncoming(): Flow<IncomingFrame>
}