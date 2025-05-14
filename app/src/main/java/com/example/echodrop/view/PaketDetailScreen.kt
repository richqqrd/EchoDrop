package com.example.echodrop.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.viewmodel.FileEntryUi
import com.example.echodrop.viewmodel.PaketDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaketDetailScreen(
    paketId: String,
    viewModel: PaketDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    println("PaketDetailScreen opened with ID: $paketId")
    val coroutineScope = rememberCoroutineScope()
    val detail by viewModel.detail.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Lädt die Paket-Details beim Starten der Komponente
    DisposableEffect(paketId) {
        val job = coroutineScope.launch {
            viewModel.load(PaketId(paketId))
        }
        
        onDispose {
            // Job nur abbrechen, wenn er noch läuft
            if (job.isActive) {
                job.cancel()
            }
        }
    }

    // Rücknavigation nach dem Löschen
    LaunchedEffect(detail) {
        if (detail == null) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.title ?: "Paket Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
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
                .background(Color.LightGray)
        ) {
            if (detail == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Test-Screen für ID: $paketId", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Zurück")
                        }
                    }
                }
            }
        }


    // Dialog zum Löschen des Pakets
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Paket löschen") },
            text = { Text("Möchtest du dieses Paket wirklich löschen?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.onDelete()
                        }
                        showDeleteDialog = false
                    }
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

    // Dialog zum Bearbeiten der Metadaten
    if (showEditDialog && detail != null) {
        var ttl by remember { mutableStateOf(detail!!.ttlSeconds) }
        var priority by remember { mutableStateOf(detail!!.priority) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Paket bearbeiten") },
            text = {
                Column {
                    Text("TTL (Sekunden): $ttl")
                    Slider(
                        value = ttl.toFloat(),
                        onValueChange = { ttl = it.toInt() },
                        valueRange = 1800f..86400f,
                        steps = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Priorität: $priority")
                    Slider(
                        value = priority.toFloat(),
                        onValueChange = { priority = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.onUpdateMeta(ttl, priority)
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun FileItem(file: FileEntryUi) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.path.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatFileSize(file.sizeBytes),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = file.mime,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
    }
// Hilfsfunktion zur Formatierung der Dateigröße
fun formatFileSize(sizeBytes: Long): String {
    return when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
        else -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
    }
}


