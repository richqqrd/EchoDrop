package com.example.echodrop.model.dataLayer.datasource.platform.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.echodrop.model.domainLayer.transport.DeviceDiscovery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet die WiFi Direct Peer-Discovery und Verbindungslogik.
 * Verantwortlich für das Auffinden von Peers und das Herstellen von Verbindungen.
 */
@Singleton
class WiFiDirectDiscovery @Inject constructor(
    @ApplicationContext private val context: Context
): DeviceDiscovery {
    companion object {
        private const val TAG = "WiFiDirectDiscovery"
    }

    private val manager: WifiP2pManager by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private val channel: WifiP2pManager.Channel by lazy {
        manager.initialize(context, Looper.getMainLooper(), null)
    }

    private val _discoveredDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<WifiP2pDevice>> = _discoveredDevices.asStateFlow()

    private val _connectionInfo = MutableStateFlow<WifiP2pInfo?>(null)
    override val connectionInfo: StateFlow<WifiP2pInfo?> = _connectionInfo.asStateFlow()

    private val _thisDevice = MutableStateFlow<WifiP2pDevice?>(null)
    override val thisDevice: StateFlow<WifiP2pDevice?> = _thisDevice.asStateFlow()

    private var isDiscoveryActive = false

    private var receiverRegistered = false

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    /**
     * Startet die Peer-Discovery
     */
    override fun startDiscovery() {
        Log.d(TAG, "Starting discovery")

        if (!hasRequiredPermissions()) {
            Log.e(TAG, "Missing required permissions for WiFi Direct")
            return
        }

        isDiscoveryActive = true

        if (!receiverRegistered) {
            try {
                context.registerReceiver(receiver, intentFilter)
                receiverRegistered = true
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error registering receiver", e)
            }
        }

        discoverPeers()

        updateThisDeviceInfo()
    }

    /**
     * Stoppt die Peer-Discovery
     */
    override fun stopDiscovery() {
        Log.d(TAG, "Stopping discovery")

        isDiscoveryActive = false

        if (receiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error unregistering receiver", e)
            } finally {
                receiverRegistered = false
            }
        }

        _discoveredDevices.value = emptyList()

        manager.stopPeerDiscovery(channel, null)
    }

    /**
     * Startet die Peer-Discovery
     */
    private fun discoverPeers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            discoverPeersApi33()
        } else {
            discoverPeersLegacy()
        }
    }

    /**
     * Starten der Peer-Discovery für Android 13+
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun discoverPeersApi33() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing NEARBY_WIFI_DEVICES permission")
            return
        }

        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Discovery started successfully (API 33+)")
            }

            override fun onFailure(reason: Int) {
                val reasonStr = when(reason) {
                    WifiP2pManager.ERROR -> "ERROR"
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                    WifiP2pManager.BUSY -> "BUSY"
                    else -> "UNKNOWN"
                }
                Log.e(TAG, "Discovery failed to start (API 33+): $reasonStr")
            }
        })
    }

    /**
     * Starten der Peer-Discovery für ältere Android-Versionen (
     */
    private fun discoverPeersLegacy() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing ACCESS_FINE_LOCATION permission")
            return
        }

        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Discovery started successfully (Legacy)")
            }

            override fun onFailure(reason: Int) {
                val reasonStr = when(reason) {
                    WifiP2pManager.ERROR -> "ERROR"
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                    WifiP2pManager.BUSY -> "BUSY"
                    else -> "UNKNOWN"
                }
                Log.e(TAG, "Discovery failed to start (Legacy): $reasonStr")
            }
        })
    }

    /**
     * Anfordern der aktuellen Peerliste
     */
    private fun requestPeers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPeersApi33()
        } else {
            requestPeersLegacy()
        }
    }

    /**
     * Peers-Liste anfordern für Android 13+ (API 33+)
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPeersApi33() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing NEARBY_WIFI_DEVICES permission")
            return
        }

        manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
            _discoveredDevices.value = peers.deviceList.toList()
            Log.d(TAG, "Discovered devices: ${peers.deviceList.size}")
        }
    }

    /**
     * Peers-Liste anfordern für ältere Android-Versionen
     */
    private fun requestPeersLegacy() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing ACCESS_FINE_LOCATION permission")
            return
        }

        manager.requestPeers(channel) { peers: WifiP2pDeviceList ->
            _discoveredDevices.value = peers.deviceList.toList()
            Log.d(TAG, "Discovered devices: ${peers.deviceList.size}")
        }
    }

    /**
     * Verbindung zu einem Gerät herstellen
     */
    override fun connectToDevice(deviceAddress: String) {
        Log.d(TAG, "Attempting to connect to device: $deviceAddress")

        if (_connectionInfo.value != null && _connectionInfo.value!!.groupFormed) {
            Log.d(TAG, "Currently in a group – disconnecting before connecting to $deviceAddress")
            disconnectFromCurrentGroup()
        }
        val device = _discoveredDevices.value.find { it.deviceAddress == deviceAddress }

        if (device == null) {
            Log.w(TAG, "Device $deviceAddress nicht in aktueller Peer-Liste – versuche Direktverbindung")

            if (!hasRequiredPermissions()) {
                Log.e(TAG, "Missing required permissions for WiFi Direct")
                return
            }

            val config = WifiP2pConfig().apply { this.deviceAddress = deviceAddress }

            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Direct connection initiated successfully to $deviceAddress")
                }

                override fun onFailure(reason: Int) {
                    val reasonStr = when(reason) {
                        WifiP2pManager.ERROR -> "ERROR"
                        WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                        WifiP2pManager.BUSY -> "BUSY"
                        else -> "UNKNOWN"
                    }
                    Log.e(TAG, "Direct connection failed to $deviceAddress: $reasonStr")
                }
            })
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            connectToDeviceApi33(device)
        } else {
            connectToDeviceLegacy(device)
        }
    }

    /**
     * Verbindung zu Gerät für Android 13+ (API 33+)
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun connectToDeviceApi33(device: WifiP2pDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing NEARBY_WIFI_DEVICES permission")
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection initiated successfully to ${device.deviceName} (API 33+)")
            }

            override fun onFailure(reason: Int) {
                val reasonStr = when(reason) {
                    WifiP2pManager.ERROR -> "ERROR"
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                    WifiP2pManager.BUSY -> "BUSY"
                    else -> "UNKNOWN"
                }
                Log.e(TAG, "Connection failed to ${device.deviceName} (API 33+): $reasonStr")
            }
        })
    }

    /**
     * Verbindung zu Gerät für ältere Android-Versionen
     */
    private fun connectToDeviceLegacy(device: WifiP2pDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing ACCESS_FINE_LOCATION permission")
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            groupOwnerIntent = 0
        }

        try {
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Connection initiated successfully to ${device.deviceName} (Legacy)")
                }

                override fun onFailure(reason: Int) {
                    val reasonStr = when(reason) {
                        WifiP2pManager.ERROR -> "ERROR"
                        WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                        WifiP2pManager.BUSY -> "BUSY"
                        else -> "UNKNOWN"
                    }
                    Log.e(TAG, "Connection failed to ${device.deviceName} (Legacy): $reasonStr")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during connect attempt: ${e.message}", e)

            try {
                manager.cancelConnect(channel, null)
                manager.removeGroup(channel, null)
            } catch (e2: Exception) {
                Log.e(TAG, "Error during cleanup: ${e2.message}")
            }
        }
    }

    /**
     * Prüft, ob alle erforderlichen Berechtigungen vorhanden sind
     */
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_WIFI_STATE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CHANGE_WIFI_STATE
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_WIFI_STATE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CHANGE_WIFI_STATE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * BroadcastReceiver für WiFi Direct Events
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d(TAG, "Connection state changed")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        handleConnectionChangedApi33(intent)
                    } else {
                        handleConnectionChangedLegacy(intent)
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.d(TAG, "This device changed")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            val device = intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE,
                                WifiP2pDevice::class.java
                            )
                            _thisDevice.value = device
                            Log.d(TAG, "This device: ${device?.deviceName} (${device?.deviceAddress})")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting this device info", e)
                        }
                    } else {
                        try {
                            @Suppress("DEPRECATION")
                            val device = intent.getParcelableExtra<WifiP2pDevice>(
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                            )
                            _thisDevice.value = device
                            Log.d(TAG, "This device: ${device?.deviceName} (${device?.deviceAddress})")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting this device info", e)
                        }
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.d(TAG, "Peers list changed")
                    requestPeers()
                }

                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    val enabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    Log.d(TAG, "WiFi P2P is ${if (enabled) "enabled" else "disabled"}")
                }
            }
        }

        /**
         * Verarbeiten von Verbindungsänderungen für Android 13+ (API 33+)
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun handleConnectionChangedApi33(intent: Intent) {
            try {
                val wifiP2pInfo = intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_INFO,
                    WifiP2pInfo::class.java
                )
                val isConnected = wifiP2pInfo?.groupFormed == true

                if (isConnected) {
                    _connectionInfo.value = wifiP2pInfo
                    Log.d(TAG, "Connected to group: ${wifiP2pInfo.groupOwnerAddress?.hostAddress}")
                } else {
                    _connectionInfo.value = null
                    Log.d(TAG, "Disconnected from group")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling connection change", e)
            }
        }

        /**
         * Verarbeiten von Verbindungsänderungen für ältere Android-Versionen
         */
        private fun handleConnectionChangedLegacy(intent: Intent) {
            try {
                @Suppress("DEPRECATION")
                val wifiP2pInfo = intent.getParcelableExtra<WifiP2pInfo>(
                    WifiP2pManager.EXTRA_WIFI_P2P_INFO
                )
                @Suppress("DEPRECATION")
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(
                    WifiP2pManager.EXTRA_NETWORK_INFO
                )
                val isConnected = wifiP2pInfo?.groupFormed == true || networkInfo?.isConnected == true

                if (isConnected) {
                    _connectionInfo.value = wifiP2pInfo
                    Log.d(TAG, "Connected to group: ${wifiP2pInfo?.groupOwnerAddress?.hostAddress}")
                } else {
                    _connectionInfo.value = null
                    Log.d(TAG, "Disconnected from group")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling connection change", e)
            }
        }
    }

    /**
     * Trennt die aktuelle Gruppenverbindung und bereinigt den Verbindungsstatus
     */
    override fun disconnectFromCurrentGroup() {
        Log.d(TAG, "Disconnecting from current group")

        if (_connectionInfo.value == null) {
            Log.d(TAG, "No active connection to disconnect from")
            return
        }

        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Successfully disconnected from group")
                _connectionInfo.value = null
            }

            override fun onFailure(reason: Int) {
                val reasonStr = when(reason) {
                    WifiP2pManager.ERROR -> "ERROR"
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                    WifiP2pManager.BUSY -> "BUSY"
                    else -> "UNKNOWN"
                }
                Log.e(TAG, "Failed to disconnect from group: $reasonStr")
            }
        })
    }

    private fun updateThisDeviceInfo() {
        if (!hasRequiredPermissions()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.requestDeviceInfo(channel) { device ->
                if (device != null) {
                    _thisDevice.value = device
                    Log.d(TAG, "Refreshed thisDevice: ${device.deviceName} (${device.deviceAddress})")
                }
            }
        }
    }
}