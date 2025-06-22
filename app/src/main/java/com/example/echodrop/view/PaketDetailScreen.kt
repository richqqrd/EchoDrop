package com.example.echodrop.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.viewmodel.FileEntryUi
import com.example.echodrop.viewmodel.PaketDetailViewModel
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Search



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaketDetailScreen(
    paketId: String,
    viewModel: PaketDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // Load package details when the screen is displayed
    LaunchedEffect(paketId) {
        viewModel.loadPaketDetail(paketId)
    }

    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var peerIdInput by remember { mutableStateOf("") }

    // Temporäre Werte für Bearbeitung
    var editTtl by remember { mutableStateOf(3600) }
    var editPriority by remember { mutableStateOf(1) }

    // Initialisiere die Bearbeitungswerte, wenn das Paket geladen wurde
    LaunchedEffect(state.paket) {
        state.paket?.let {
            editTtl = it.ttlSeconds
            editPriority = it.priority
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paket Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    // Bearbeiten-Button
                    IconButton(
                        onClick = { viewModel.toggleEditMode() }
                    ) {
                        Icon(
                            if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (state.isEditing) "Abbrechen" else "Bearbeiten"
                        )
                    }

                    // Teilen-Button
                    IconButton(
                        onClick = { showShareDialog = true }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Teilen")
                    }

                    // Löschen-Button
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Löschen")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                // Show error message
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fehler: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Erneut versuchen")
                    }
                }
            } else if (state.paket != null) {
                // Show package details
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = state.paket!!.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Tags anzeigen, falls vorhanden
                        if (state.paket!!.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                state.paket!!.tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item {
                        state.paket!!.description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Bearbeitungsmodus
                    if (state.isEditing) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Paket bearbeiten",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("TTL (Sekunden): $editTtl")
                                    Slider(
                                        value = editTtl.toFloat(),
                                        onValueChange = { editTtl = it.toInt() },
                                        valueRange = 1800f..86400f,
                                        steps = 5
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Priorität: $editPriority")
                                    Slider(
                                        value = editPriority.toFloat(),
                                        onValueChange = { editPriority = it.toInt() },
                                        valueRange = 1f..5f,
                                        steps = 3
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updatePaketSettings(editTtl, editPriority)
                                            }
                                        ) {
                                            Text("Speichern")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Metadaten-Karte
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Paket ID: ${state.paket!!.id.value}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "TTL: ${state.paket!!.ttlSeconds} Sekunden",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Priorität: ${state.paket!!.priority}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Anzahl Dateien: ${state.paket!!.fileCount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Dateien anzeigen
                    if (state.paket!!.files.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Dateien:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(state.paket!!.files) { file ->
                            FileItem(file)
                        }
                    }
                }

                // Loading-Overlay für das Löschen
                if (state.isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .align(Alignment.Center)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                // No package loaded
                Text(
                    text = "Kein Paket gefunden",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Löschen-Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Paket löschen") },
                text = { Text("Möchtest du dieses Paket wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onDeletePaket()
                            showDeleteDialog = false
                            // Nach dem Löschen würde man normalerweise zurücknavigieren,
                            // dies ist aber bereits im ViewModel implementiert
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Löschen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            )
        }

        // Share-Dialog
// Share-Dialog
        if (showShareDialog) {
            var selectedTab by remember { mutableStateOf(0) }

            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Paket teilen") },
                text = {
                    Column {
                        // Tabs für verschiedene Teilen-Methoden
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("WiFi Direct") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Peer-ID") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (selectedTab) {
                            // WiFi Direct Tab
                            0 -> {
                                val nearbyDevices by viewModel.nearbyDevices.collectAsState()
                                val isDiscoveryActive by viewModel.isDiscoveryActive.collectAsState()

                                Column {
                                    // Status und Suchknopf
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isDiscoveryActive) "Suche nach Geräten..." else "Gerätesuche starten",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        IconButton(onClick = { viewModel.toggleDiscovery() }) {
                                            Icon(
                                                imageVector = if (isDiscoveryActive) Icons.Default.Stop else Icons.Default.Search,
                                                contentDescription = if (isDiscoveryActive) "Suche stoppen" else "Suche starten"
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    // Geräteliste
                                    if (nearbyDevices.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDiscoveryActive) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(
                                                            32.dp
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Suche nach Geräten...")
                                                }
                                            } else {
                                                Text("Keine Geräte gefunden. Starte die Suche.")
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                        ) {
                                            items(nearbyDevices) { device ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.shareWithDevice(device.deviceAddress)
                                                            showShareDialog = false
                                                        }
                                                        .padding(vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Smartphone,
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(end = 16.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = device.deviceName
                                                                ?: "Unbekanntes Gerät",
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

                            // Manuelle Peer-ID Tab (bestehende Funktionalität)
                            1 -> {
                                Column {
                                    Text("Gib die Peer-ID ein, mit der du dieses Paket teilen möchtest:")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = peerIdInput,
                                        onValueChange = { peerIdInput = it },
                                        label = { Text("Peer-ID") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (selectedTab == 1 && peerIdInput.isNotBlank()) {
                                viewModel.onSharePaket(PeerId(peerIdInput))
                            }
                            showShareDialog = false
                        }
                    ) {
                        Text("Schließen")
                    }
                }
            )
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FileItem(file: FileEntryUi) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon je nach Dateityp
                Icon(
                    imageVector = when {
                        file.mime.startsWith("image/") -> Icons.Default.Image
                        file.mime.startsWith("video/") -> Icons.Default.Movie
                        file.mime.startsWith("audio/") -> Icons.Default.MusicNote
                        file.mime.startsWith("text/") -> Icons.Default.Description
                        file.mime.contains("pdf") -> Icons.Default.Description
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = file.path.substringAfterLast('/'),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatFileSize(file.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }


private fun formatFileSize(sizeBytes: Long): String {
    return when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
        else -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
    }
}



