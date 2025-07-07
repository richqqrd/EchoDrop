package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.PaketId
import kotlinx.coroutines.flow.Flow

/**
 * Abstraktion der low-level Socket-Kommunikation.
 */
interface DirectSocketService {
    fun startService()
    fun stopService()

    suspend fun sendData(data: ByteArray, targetAddress: String, paketId: PaketId)

    fun observeIncomingData(): Flow<Pair<String, ByteArray>>
    fun observeTransferProgress(): Flow<Triple<PaketId, String, Int>>

    /**
     * Muss aufgerufen werden, sobald WifiP2P-Connection hergestellt wurde, damit
     * der Service Rolle (GroupOwner/Client) kennt.
     */
    fun onConnectionEstablished(info: android.net.wifi.p2p.WifiP2pInfo)
} 