package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.PaketId

data class PaketUi (
    val id: PaketId,
    val title: String,
    val description: String?,
    val fileCount: Int,
    val isSending: Boolean,
    val files: List<FileEntryUi> = emptyList(),
    val ttlSeconds: Int = 3600,
    val priority: Int = 1
    )