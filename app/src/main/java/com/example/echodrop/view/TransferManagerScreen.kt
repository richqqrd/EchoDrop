package com.example.echodrop.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echodrop.viewmodel.TransferManagerViewModel
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferManagerScreen(
    onBackClick: () -> Unit,
    viewModel: TransferManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.startDiscovery()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDiscovery()
        }
    }

    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer-Manager") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDiscovery() }) {
                        Icon(
                            if (uiState.isDiscoveryActive) Icons.Default.WifiFind else Icons.Default.WifiOff,
                            contentDescription = if (uiState.isDiscoveryActive) "Discovery beenden" else "Discovery starten"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleDebugMode() }) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Debug-Modus"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isWifiDirectEnabled) Icons.Default.SignalWifi4Bar else Icons.Default.SignalWifiOff,
                            contentDescription = null,
                            tint = if (uiState.isWifiDirectEnabled) Color.Green else Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isWifiDirectEnabled) "WiFi Direct aktiviert" else "WiFi Direct deaktiviert",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isDiscoveryActive) Icons.Default.Search else Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = if (uiState.isDiscoveryActive) Color.Green else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isDiscoveryActive) "Suche nach Geräten" else "Gerätesuche inaktiv",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (uiState.thisDevice != null) {
                        val device = uiState.thisDevice 
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dein Gerät",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = device?.deviceName ?: "Unbekannt",  
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Verfügbare Geräte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${uiState.discoveredDevices.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.discoveredDevices.isEmpty()) {
                        Text(
                            text = "Keine Geräte gefunden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(uiState.discoveredDevices) { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = device.deviceName ?: "Unbekanntes Gerät",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = device.deviceAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Weiterleitungs-Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.forwardLog.isEmpty()) {
                        Text("Keine Ereignisse", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(uiState.forwardLog.reversed()) { event ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (icon, tint) = when (event.stage) {
                                        com.example.echodrop.model.domainLayer.transport.ForwardEvent.Stage.CONNECTING -> Icons.Default.Link to Color(0xFF1976D2)
                                        com.example.echodrop.model.domainLayer.transport.ForwardEvent.Stage.TIMEOUT -> Icons.Default.HourglassEmpty to Color(0xFFFFA000)
                                        com.example.echodrop.model.domainLayer.transport.ForwardEvent.Stage.SENT -> Icons.Default.Send to Color(0xFF388E3C)
                                        com.example.echodrop.model.domainLayer.transport.ForwardEvent.Stage.FAILED -> Icons.Default.Error to Color(0xFFD32F2F)
                                    }
                                    Icon(icon, contentDescription = null, tint = tint)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${event.paketId.value.take(6)}… → ${event.peerId?.value ?: "?"}: ${event.message}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Übertragungen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.sendingTransfers.isNotEmpty()) {
                        Text(
                            text = "Sending",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            uiState.sendingTransfers.forEach { transfer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Package ${transfer.paketId.value}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Text(
                                        text = "${(transfer.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { viewModel.pauseTransfer(transfer.id) }
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    }

                                    IconButton(
                                        onClick = { viewModel.cancelTransfer(transfer.id) }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Abbrechen")
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { transfer.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    } else {
                        Text("Keine sendenden Transfers", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.receivingTransfers.isNotEmpty()) {
                        Text(
                            text = "Receiving",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            uiState.receivingTransfers.forEach { transfer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Package ${transfer.paketId.value}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Text(
                                        text = "${(transfer.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { viewModel.pauseTransfer(transfer.id) }
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    }

                                    IconButton(
                                        onClick = { viewModel.cancelTransfer(transfer.id) }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Abbrechen")
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { transfer.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    } else {
                        Text("Keine eingehenden Transfers", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState.completedTransfers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Finished",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            uiState.completedTransfers.take(5).forEach { transfer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Package ${transfer.paketId.value}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Green
                                    )
                                }

                                Divider()
                            }
                        }
                    } else {
                        Text("Keine abgeschlossenen Transfers", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }

                    if (uiState.isDebugMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Debug-Bereich",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.generateTestTransfer(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Test Sending")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.generateTestTransfer(false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Test Receiving")
                            }
                        }
                    }
                }
            }
        }
    }
}