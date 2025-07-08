package com.example.echodrop.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.viewmodel.FileEntryUi
import java.io.File
import java.io.IOException


object FileUtils {
    /**
     * Erstellt ein Content-URI für eine Datei über den FileProvider
     */
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    /**
     * Öffnet eine Datei mit einer passenden App (für UI-Modell)
     */
    fun openFile(context: Context, fileEntryUi: FileEntryUi) {
        val file = File(fileEntryUi.path)
        if (!file.exists()) {
            return
        }

        val uri = getFileUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, fileEntryUi.mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Teilt eine Datei mit anderen Apps (für UI-Modell)
     */
    fun shareFile(context: Context, fileEntryUi: FileEntryUi) {
        val file = File(fileEntryUi.path)
        if (!file.exists()) {
            return
        }

        val uri = getFileUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = fileEntryUi.mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Teile Datei via"))
    }

    /**
     * Exportiert eine Datei ins Downloads-Verzeichnis (für UI-Modell)
     */
fun exportToDownloads(context: Context, fileEntryUi: FileEntryUi): Uri? {
    val sourceFile = File(fileEntryUi.path)
    Log.d("FileUtils", "Attempting to export file: ${sourceFile.absolutePath}")
    
    if (!sourceFile.exists()) {
        Log.e("FileUtils", "Source file does not exist")
        Toast.makeText(context, "Datei existiert nicht", Toast.LENGTH_SHORT).show()
        return null
    }
    
    if (sourceFile.length() == 0L) {
        Log.e("FileUtils", "Source file is empty")
        Toast.makeText(context, "Datei ist leer", Toast.LENGTH_SHORT).show()
        return null
    }
    
    Log.d("FileUtils", "File exists, size: ${sourceFile.length()} bytes")
    
    try {
        val fileName = sourceFile.name
        Log.d("FileUtils", "Creating MediaStore entry for file: $fileName")
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, fileEntryUi.mime)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        
        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        
        if (uri == null) {
            Log.e("FileUtils", "Failed to create MediaStore entry")
            Toast.makeText(context, "Konnte keinen Download-Eintrag erstellen", Toast.LENGTH_SHORT).show()
            return null
        }
        
        Log.d("FileUtils", "MediaStore entry created: $uri")
        
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            try {
                sourceFile.inputStream().use { inputStream ->
                    val bytesCopied = inputStream.copyTo(outputStream)
                    Log.d("FileUtils", "Successfully copied $bytesCopied bytes")
                }
            } catch (e: IOException) {
                Log.e("FileUtils", "Error during file copy", e)
                contentResolver.delete(uri, null, null)
                throw e
            }
        } ?: run {
            Log.e("FileUtils", "Failed to open output stream")
            contentResolver.delete(uri, null, null)
            Toast.makeText(context, "Konnte keine Ausgabedatei öffnen", Toast.LENGTH_SHORT).show()
            return null
        }
        
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        contentResolver.update(uri, contentValues, null, null)
        
        Log.d("FileUtils", "File successfully exported to Downloads")
        Toast.makeText(context, "Datei in Downloads gespeichert", Toast.LENGTH_SHORT).show()
        return uri
    } catch (e: Exception) {
        Log.e("FileUtils", "Error exporting file", e)
        Toast.makeText(context, "Fehler beim Speichern: ${e.message}", Toast.LENGTH_SHORT).show()
        return null
    }
}

/**
 * Kopiert den Inhalt einer URI in eine lokale Datei und gibt den Dateipfad zurück
 */
fun copyUriToAppFile(context: Context, uri: Uri, nameHint: String? = null): String? {
    try {
        // Dateinamen aus URI extrahieren oder Hinweis verwenden
        val fileName = nameHint ?: getFileNameFromUri(context, uri) ?: "file_${System.currentTimeMillis()}"
        
        // Zielverzeichnis erstellen
        val filesDir = File(context.filesDir, "app_files")
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        
        // Datei mit eindeutigem Namen erstellen
        val uniqueFileName = "file_${System.currentTimeMillis()}_$fileName"
        val destinationFile = File(filesDir, uniqueFileName)
        
        // Inhalt kopieren
        context.contentResolver.openInputStream(uri)?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        
        Log.d("FileUtils", "File copied from URI to ${destinationFile.absolutePath}")
        return destinationFile.absolutePath
    } catch (e: Exception) {
        Log.e("FileUtils", "Error copying URI content to file", e)
        return null
    }
}

/**
 * Extrahiert den Dateinamen aus einer URI
 */
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(
        uri, null, null, null, null
    ) ?: return null
    
    cursor.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                return it.getString(displayNameIndex)
            }
        }
    }
    
    // Fallback: Versuche den letzten Pfadteil zu extrahieren
    return uri.lastPathSegment
}
}