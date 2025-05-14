package com.example.echodrop.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePaketViewModel @Inject constructor(
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


    fun addFiles(uris: List<Uri>) = _state.update { it.copy(uris = it.uris + uris) }

    fun onSaveClicked() = viewModelScope.launch {
        val id = createPaket(
            PaketMeta(
                title = state.value.title,
                description = state.value.description,
                tags = state.value.tags,
                ttlSeconds = state.value.ttl,
                priority = state.value.priority
            ),
            state.value.uris.map { it.toFileEntry() }
        )
        _state.update { it.copy(saved = true) }

    }

    private fun Uri.toFileEntry(): FileEntry {
        val fileName = this.lastPathSegment ?: "unknown"
        val mime = getMimeType(fileName)
        return FileEntry(
            path = fileName,
            mime = mime,
            sizeBytes = getFileSize(this) ?: 0L,
            orderIdx = state.value.uris.size
        )
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".pdf", true) -> "application/pdf"
            fileName.endsWith(".txt", true) -> "text/plain"
            fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "application/msword"
            fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> "application/vnd.ms-excel"
            fileName.endsWith(".zip", true) -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    //TODO contentResolver einfügen
    private fun getFileSize(uri: Uri): Long? {
        return (1024L..5242880L).random()
    }


    data class CreatePaketState(
        val title: String = "",
        val description: String? = null,
        val tags: List<String> = emptyList(),
        val ttl: Int = 3600,
        val priority: Int = 1,
        val uris: List<Uri> = emptyList(),
        val saved: Boolean = false
    )
}