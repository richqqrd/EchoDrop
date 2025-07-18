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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echodrop.viewmodel.FileEntryUi
import com.example.echodrop.viewmodel.PaketDetailViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.echodrop.model.dataLayer.datasource.platform.file.FileUtils
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.runtime.DisposableEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaketDetailScreen(
    paketId: String,
    onBackClick: () -> Unit,
    onOpenTransferManager: () -> Unit,
    viewModel: PaketDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(paketId) {
        viewModel.loadPaketDetail(paketId)
    }



    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var peerIdInput by remember { mutableStateOf("") }


    var editTtl by remember { mutableStateOf(3600) }
    var editPriority by remember { mutableStateOf(1) }
    var editMaxHops by remember { mutableStateOf<Int?>(3) }


    LaunchedEffect(state.paket) {
        state.paket?.let {
            editTtl = it.ttlSeconds
            editPriority = it.priority
                    editMaxHops = it.maxHops

        }
    }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.paket?.createdUtc) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.isDiscoveryActive.value) {
                viewModel.toggleDiscovery()
            }
        }
    }

    LaunchedEffect(state.navigateToManager) {
        if (state.navigateToManager) {
            onOpenTransferManager()
            viewModel.clearNavigationFlag()
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

                    IconButton(
                        onClick = { viewModel.toggleEditMode() }
                    ) {
                        Icon(
                            if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (state.isEditing) "Abbrechen" else "Bearbeiten"
                        )
                    }


                    val canShare = state.paket?.let { it.maxHops == null || it.currentHopCount < it.maxHops } ?: true
                    IconButton(onClick = { if (canShare) showShareDialog = true }, enabled = canShare) {
                        Icon(Icons.Default.Share, contentDescription = "Teilen")
                    }


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
                FullscreenLoading("Übertrage Dateien …")
            } else if (state.error != null) {

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

val paket = state.paket!!
val expiresAt = paket.createdUtc + paket.ttlSeconds * 1000
val ttlLeft = ((expiresAt - now) / 1000).coerceAtLeast(0)
val ttlProgress = ttlLeft.toFloat() / paket.ttlSeconds.toFloat()

Text(
    text = "TTL verbleibend:",
    style = MaterialTheme.typography.bodyMedium
)
Spacer(modifier = Modifier.height(4.dp))
LinearProgressIndicator(
    progress = ttlProgress.coerceIn(0f, 1f),
    modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
)
Spacer(modifier = Modifier.height(4.dp))
Text(
    text = formatTtl(ttlLeft.toInt()),
    style = MaterialTheme.typography.labelSmall,
    color = Color.Gray
)
Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Priorität: ${state.paket!!.priority}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.paket!!.maxHops != null) {
                                    val remaining = (state.paket!!.maxHops!! - state.paket!!.currentHopCount).coerceAtLeast(0)
                                    Text(
                                        text = "Weiterleitungen verbleibend: $remaining / ${state.paket!!.maxHops}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        text = "Weiterleitungen: Unbegrenzt",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Text(
                                    text = "Anzahl Dateien: ${state.paket!!.fileCount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    
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
                
                Text(
                    text = "Kein Paket gefunden",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        
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

        
        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Paket teilen") },
                text = {
                    Column {
                        
                        Text("Wähle ein Gerät zum Teilen des Pakets:")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
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
            
            val fileName = remember(file.path) {
                val raw = File(file.path).name
                raw.substringAfterLast('_', raw)
            }
            
            
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                
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

private fun formatTtl(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02dh %02dm %02ds".format(hours, minutes, secs)
}



