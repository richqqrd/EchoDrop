package com.example.echodrop.domain.transport

import java.io.FileDescriptor

data class Manifest (
    val paketId: String,
    val title: String,
    val description: String?,
    val tag: List<String>,
    val files: List<FileDescriptor>
)

data class FileDescriptor(
    val fileId: String,
    val fileName: String,
    val sizeBytes: Long,
    val mime: String,
    val chunkCount: Int
)