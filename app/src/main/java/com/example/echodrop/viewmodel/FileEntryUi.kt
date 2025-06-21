package com.example.echodrop.viewmodel

/**
 * UI-Repräsentation einer Datei im Paket für die Anzeige in der UI.
 *
 * @property path Der Dateipfad
 * @property mime Der MIME-Typ der Datei
 * @property sizeBytes Die Größe der Datei in Bytes
 * @property orderIdx Der Index für die Reihenfolge innerhalb des Pakets
 */
data class FileEntryUi(
    val path: String,
    val mime: String,
    val sizeBytes: Long,
    val orderIdx: Int
)