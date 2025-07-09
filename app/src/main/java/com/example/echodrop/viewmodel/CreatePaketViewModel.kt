package com.example.echodrop.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import com.example.echodrop.model.dataLayer.datasource.platform.file.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePaketViewModel @Inject constructor(
    private val application: Application,
    private val createPaket: CreatePaketUseCase,
    private val insertFiles: InsertFilesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePaketState())
    val state: StateFlow<CreatePaketState> = _state

    fun setTitle(text: String) = _state.update { it.copy(title = text) }
    fun setDescription(text: String) = _state.update { it.copy(description = text) }
    fun setTtl(value: Int) = _state.update { it.copy(ttl = value) }
    fun setPriority(value: Int) = _state.update { it.copy(priority = value) }

    fun setTags(text: String) = _state.update {
        it.copy(
            tags = if (text.isBlank()) emptyList() else text.split(",").map { tag -> tag.trim() })
    }

    fun addFiles(uris: List<Uri>) {
        viewModelScope.launch {
            // Verarbeite nur gültige URIs
            val processedUris = uris.filter { uri ->
                // URI ist gültig, wenn wir sie lesen können
                try {
                    application.contentResolver.openInputStream(uri)?.close()
                    true
                } catch (e: Exception) {
                    Log.e("CreatePaketViewModel", "Cannot open URI: $uri", e)
                    false
                }
            }
            
            _state.update { currentState ->
                currentState.copy(uris = currentState.uris + processedUris)
            }
        }
    }

    fun onSaveClicked() = viewModelScope.launch {
        val currentState = _state.value
        
        // Überprüfe, ob es Dateien gibt
        if (currentState.uris.isEmpty()) {
            _state.update { it.copy(error = "Bitte füge mindestens eine Datei hinzu") }
            return@launch
        }
        
        // Dateien in den App-Speicher kopieren und Metadaten abrufen
        val files = currentState.uris.mapIndexed { index, uri ->
            // Kopiere die Datei in den internen Speicher
            val localPath = FileUtils.copyUriToAppFile(application, uri) 
                ?: return@mapIndexed null
            
            Log.d("CreatePaketViewModel", "Copied file to local path: $localPath")
            
            // Ermittle den MIME-Typ
            val mime = getMimeType(uri) ?: "application/octet-stream"
            
            // Ermittle die tatsächliche Dateigröße
            val size = File(localPath).length()
            
            FileEntry(
                path = localPath,
                mime = mime,
                sizeBytes = size,
                orderIdx = index
            )
        }.filterNotNull()
        
        if (files.isEmpty()) {
            _state.update { it.copy(error = "Fehler beim Speichern der Dateien") }
            return@launch
        }
        
        // Hier wird das Paket mit den tatsächlichen FileEntry-Objekten erstellt,
        // die auf lokale Dateien verweisen, nicht auf URIs
        val id = createPaket(
            PaketMeta(
                title = state.value.title,
                description = state.value.description,
                tags = state.value.tags,
                ttlSeconds = state.value.ttl,
                priority = state.value.priority,
                maxHops = state.value.maxHops
            ),
            files  // Verwende die FileEntry-Objekte mit lokalen Pfaden
        )
        
        _state.update { it.copy(saved = true) }
    }

    private fun getMimeType(uri: Uri): String? {
        val contentResolver = application.contentResolver
        return contentResolver.getType(uri) ?: run {
            // Fallback: Bestimme MIME-Typ anhand des Dateinamens
            val fileName = getFileName(uri)
            when {
                fileName?.endsWith(".jpg", true) == true || fileName?.endsWith(".jpeg", true) == true -> "image/jpeg"
                fileName?.endsWith(".png", true) == true -> "image/png"
                fileName?.endsWith(".pdf", true) == true -> "application/pdf"
                fileName?.endsWith(".txt", true) == true -> "text/plain"
                fileName?.endsWith(".doc", true) == true || fileName?.endsWith(".docx", true) == true -> "application/msword"
                fileName?.endsWith(".xls", true) == true || fileName?.endsWith(".xlsx", true) == true -> "application/vnd.ms-excel"
                fileName?.endsWith(".zip", true) == true -> "application/zip"
                else -> "application/octet-stream"
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val contentResolver = application.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    fun setMaxHops(value: Int?) {
    _state.update { it.copy(maxHops = value) }
}

data class CreatePaketState(
    val title: String = "",
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val ttl: Int = 3600, // Lebensdauer in Sekunden (bereits vorhanden)
    val priority: Int = 1,
    val maxHops: Int? = 3, // Default-Wert für max. Weiterleitungen
    val uris: List<Uri> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)

}