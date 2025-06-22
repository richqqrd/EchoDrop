package com.example.echodrop.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.viewmodel.InboxViewModel
import com.example.echodrop.viewmodel.PaketUi
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.DeletePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.UpdatePaketMetaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel(),
    onCreatePaket: () -> Unit,
    onSharePaket: (PaketId) -> Unit,
    onOpenTransferManager: () -> Unit  // Neuer Parameter
) {
    val paketList by viewModel.paketList.collectAsState()
    val transferLogs by viewModel.transferLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EchoDrop") },
                actions = {
                    // Button zum TransferManager hinzufügen
                    IconButton(onClick = onOpenTransferManager) {
                        Icon(
                            Icons.Default.SwapHoriz,  // oder ein anderes passendes Icon
                            contentDescription = "Transfer Manager"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePaket) {
                Icon(Icons.Default.Add, contentDescription = "Create Paket")
            }
        }
        ) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(paketList) { paket ->
                    val isSending = transferLogs.any { it.paketId == paket.id }
                    PaketListItem(
                        paket = paket,
                        isSending = isSending,
                        onShare = { onSharePaket(paket.id)}
                    )
                    }
                }
            }
    }

@Composable
fun PaketListItem(
    paket: PaketUi,
    isSending: Boolean,
    onShare: (PaketId) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = paket.title, style = MaterialTheme.typography.titleLarge)
                paket.description?.let { desc ->
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = "${paket.fileCount} Files",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = {onShare(paket.id)}) {
                Icon(Icons.Default.Share, contentDescription = "Share Paket")
            }
            if (isSending) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

