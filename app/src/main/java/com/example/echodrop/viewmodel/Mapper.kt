package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.TransferLog

fun Paket.toUi(isSending: Boolean = false): PaketUi = PaketUi(
    id = this.id,
    title = this.meta.title,
    description = this.meta.description,
    fileCount = this.fileCount,
    isSending = isSending
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
    files = files,
    ttlSeconds = this.meta.ttlSeconds,
    priority = this.meta.priority,
    isSending = false
)