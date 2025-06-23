package com.example.echodrop.view

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.util.FileUtils
import java.io.File


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
// Share-Dialog
if (showShareDialog) {
    AlertDialog(
        onDismissRequest = { showShareDialog = false },
        title = { Text("Paket teilen") },
        text = {
            Column {
                // Zeige nur den WiFi Direct-Inhalt an (ohne Tabs)
                Text("Wähle ein Gerät zum Teilen des Pakets:")
                Spacer(modifier = Modifier.height(8.dp))
                
                // WiFi Direct Gerätesuche aktivieren
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (viewModel.isDiscoveryActive.collectAsState().value)
                            "Gerätesuche aktiv..." else "Gerätesuche starten",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = viewModel.isDiscoveryActive.collectAsState().value,
                        onCheckedChange = { viewModel.toggleDiscovery() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Gerätelist anzeigen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val devices by viewModel.nearbyDevices.collectAsState()
                    
                    if (devices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Geräte gefunden",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(devices) { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.shareWithDevice(device.deviceAddress)
                                            showShareDialog = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
        },
        confirmButton = {
            TextButton(
                onClick = { showShareDialog = false }
            ) {
                Text("Schließen")
            }
        }
    )
}
    }
}
@Composable
fun FileItem(file: FileEntryUi) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Dateinamen extrahieren
            val fileName = remember(file.path) {
                File(file.path).name
            }
            
            // Dateigröße formatieren
            val formattedSize = remember(file.sizeBytes) {
                when {
                    file.sizeBytes < 1024 -> "${file.sizeBytes} B"
                    file.sizeBytes < 1024 * 1024 -> "${file.sizeBytes / 1024} KB"
                    else -> "${file.sizeBytes / (1024 * 1024)} MB"
                }
            }
            
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$formattedSize • ${file.mime}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nur Download-Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Download-Button
                IconButton(
                    onClick = { 
                        val uri = FileUtils.exportToDownloads(context, file)
                        if (uri != null) {
                            Toast.makeText(
                                context, 
                                "Datei in Downloads gespeichert", 
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context, 
                                "Fehler beim Speichern der Datei", 
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Herunterladen"
                    )
                }
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



