package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.TransferLog

fun Paket.toUi(isSending: Boolean = false): PaketUi = PaketUi(
    id = this.id,
    title = this.meta.title,
    description = this.meta.description,
    tags = this.meta.tags,
    fileCount = this.fileCount,
    isSending = isSending,
    files = emptyList(), 
    ttlSeconds = this.meta.ttlSeconds,
    priority = this.meta.priority,
    maxHops = this.meta.maxHops,
    currentHopCount = this.currentHopCount,
    createdUtc = this.createdUtc
)

fun TransferLog.toUi(): TransferLogUi = TransferLogUi(
    paketId = this.paketId,
    peerId = this.peerId,
    progressPct = this.progressPct,
    state = this.state.name)

fun FileEntry.toUi(): FileEntryUi = FileEntryUi(
    path = this.path,
    mime = this.mime,
    sizeBytes = this.sizeBytes,
    orderIdx = this.orderIdx
)

fun Paket.toDetailUi(files: List<FileEntryUi>): PaketUi = PaketUi(
    id = this.id,
    title = this.meta.title,
    description = this.meta.description,
    fileCount = this.fileCount,
    tags = this.meta.tags,
    files = files,
    ttlSeconds = this.meta.ttlSeconds,
    priority = this.meta.priority,
    maxHops = this.meta.maxHops,
    currentHopCount = this.currentHopCount,
    isSending = false,
    createdUtc = this.createdUtc 
)