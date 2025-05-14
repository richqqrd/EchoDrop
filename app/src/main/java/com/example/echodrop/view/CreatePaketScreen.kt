package com.example.echodrop.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echodrop.viewmodel.CreatePaketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePaketScreen(
    viewModel: CreatePaketViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var saveClicked by remember { mutableStateOf(false) }

    // Datei-Auswahl-Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    // Effect für erfolgreichen Speichervorgang
    LaunchedEffect(saveClicked, state.saved) {
        if (saveClicked && state.saved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neues Paket erstellen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onSaveClicked()
                            saveClicked = true
                        },
                        enabled = state.title.isNotBlank() && state.uris.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Speichern")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.setTitle(it) },
                    label = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.description ?: "",
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Beschreibung (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.tags.joinToString(", "),
                    onValueChange = { viewModel.setTags(it) },
                    label = { Text("Tags (durch Komma getrennt)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TTL (Sekunden): ${state.ttl}")
                    Slider(
                        value = state.ttl.toFloat(),
                        onValueChange = { viewModel.setTtl(it.toInt()) },
                        valueRange = 1800f..86400f,
                        steps = 5,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priorität: ${state.priority}")
                    Slider(
                        value = state.priority.toFloat(),
                        onValueChange = { viewModel.setPriority(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dateien hinzufügen")
                }
            }

            // Anzeige der ausgewählten Dateien
            if (state.uris.isNotEmpty()) {
                item {
                    Text(
                        text = "Ausgewählte Dateien (${state.uris.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(state.uris) { uri ->
                    val fileName = uri.lastPathSegment ?: "Unbekannte Datei"
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = fileName,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}