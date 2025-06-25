package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.ConnectionState
import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import kotlinx.coroutines.flow.Flow

interface TransportManager {

    fun startBeaconing()

    fun stopBeaconing()

    suspend fun sendManifest(paketId: String, manifestJson: String)

    suspend fun sendChunk(chunkId: String, data: ByteArray)

    fun observeIncoming(): Flow<IncomingFrame>

    suspend fun startDiscovery()
    suspend fun stopDiscovery()
    suspend fun connectToDevice(deviceAddress: String)
    fun observeDiscoveredDevices(): Flow<List<DeviceInfo>>
    fun observeThisDevice(): Flow<DeviceInfo?>
    fun observeConnectionState(): Flow<ConnectionState>

    suspend fun sendPaket(paketId: PaketId, peerId: PeerId)

    suspend fun disconnectDevice()

    suspend fun forwardPaket(paketId: PaketId)



}