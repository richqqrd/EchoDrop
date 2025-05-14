package com.example.echodrop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.DeletePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.UpdatePaketMetaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaketDetailViewModel @Inject constructor(
    private val getDetail: GetPaketDetailUseCase,
    private val getFiles: GetFilesForPaketUseCase,
    private val updateMeta: UpdatePaketMetaUseCase,
    private val deletePaket: DeletePaketUseCase
) : ViewModel() {

    // Ändere den Typ zu PaketDetailUi
    private val _detail = MutableStateFlow<PaketDetailUi?>(null)
    val detail: StateFlow<PaketDetailUi?> = _detail

    fun load(paketId: PaketId) = viewModelScope.launch {
    println("DEBUG: Loading paket details for ID: ${paketId.value}")
    
    // Speichern der aktuellen ID, um Mehrfachaufrufe zu erkennen
    val currentId = paketId.value
    
    try {
        val paket = getDetail(paketId)
        println("DEBUG: GetDetail result: $paket")
        
        // Prüfe, ob die ID noch aktuell ist
        if (currentId != paketId.value) {
            println("DEBUG: ID changed during loading, cancelling")
            return@launch
        }
        
        if (paket == null) {
            println("DEBUG: Paket with ID ${paketId.value} not found!")
            
            // TEMPORÄR: Mock-Daten laden, damit man die UI sieht
            _detail.value = PaketDetailUi(
                id = paketId,
                title = "Test-Paket (Mock)",
                description = "Ein temporäres Test-Paket für Entwicklungszwecke",
                ttlSeconds = 3600,
                priority = 3,
                tags = listOf("test", "entwicklung"),
                files = emptyList()
            )
            return@launch
        }
        
        println("DEBUG: Paket found: $paket")
        
        try {
            val files = getFiles(paketId)
            println("DEBUG: Found ${files.size} files for this paket")
            
            // Nochmal prüfen, ob die ID noch aktuell ist
            if (currentId != paketId.value) {
                println("DEBUG: ID changed during loading, cancelling")
                return@launch
            }
            
            _detail.value = PaketDetailUi(
                id = paket.id,
                title = paket.meta.title,
                description = paket.meta.description,
                ttlSeconds = paket.meta.ttlSeconds,
                priority = paket.meta.priority,
                tags = paket.meta.tags,
                files = files.map { it.toFileEntryUi() }
            )
            
            println("DEBUG: Detail loaded successfully: ${_detail.value}")
        } catch (e: Exception) {
            println("DEBUG: Error loading files: ${e.message}")
            e.printStackTrace()
        }
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) {
            println("DEBUG: Load job was cancelled, ignoring")
        } else {
            println("DEBUG: Error in load method: ${e.message}")
            e.printStackTrace()
        }
    }
}

    fun onUpdateMeta(ttl: Int, priority: Int) = viewModelScope.launch {
        val currentId = detail.value?.id
        if (currentId != null) {
            updateMeta(currentId, ttl, priority)
            load(currentId)
        }
    }

    fun onDelete() = viewModelScope.launch {
        val currentId = detail.value?.id
        if (currentId != null) {
            deletePaket(currentId)
            _detail.value = null
        }
    }
    
    private fun FileEntry.toFileEntryUi() = FileEntryUi(
        path = this.path,
        mime = this.mime,
        sizeBytes = this.sizeBytes,
        orderIdx = this.orderIdx
    )
}

// Diese Klassen direkt hier definieren, damit PaketDetailScreen sie verwenden kann
data class PaketDetailUi(
    val id: PaketId,
    val title: String,
    val description: String?,
    val ttlSeconds: Int,
    val priority: Int,
    val tags: List<String>,
    val files: List<FileEntryUi>
)

data class FileEntryUi(
    val path: String,
    val mime: String,
    val sizeBytes: Long,
    val orderIdx: Int
)