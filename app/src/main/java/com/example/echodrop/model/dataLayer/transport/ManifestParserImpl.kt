package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import android.util.Log
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.transport.ManifestParser
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Standard-Implementierung, die das Manifest-JSON nach dem bisherigen Schema in ein Paket umwandelt.
 */
@Singleton
class ManifestParserImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ManifestParser {

    companion object { private const val TAG = "ManifestParser" }

    override fun parse(paketId: String, manifestJson: String, senderPeer: PeerId): Paket {
        return try {
            val jsonObject = JSONObject(manifestJson)

            // Metadaten
            val meta = PaketMeta(
                title = jsonObject.optString("title", "Unbekanntes Paket"),
                description = jsonObject.optString("description", ""),
                tags = jsonObject.optJSONArray("tags")?.let { arr ->
                    List(arr.length()) { idx -> arr.getString(idx) }
                } ?: emptyList(),
                ttlSeconds = jsonObject.optInt("ttlSeconds", 3600),
                priority = jsonObject.optInt("priority", 1),
                maxHops = jsonObject.optInt("maxHops", -1).let { if (it == -1) null else it }
            )

            val currentHopCount = jsonObject.optInt("currentHopCount", 0)

            // Dateien
            val filesArray = jsonObject.optJSONArray("files") ?: JSONArray()
            val files = mutableListOf<FileEntry>()
            for (i in 0 until filesArray.length()) {
                val fileObj = filesArray.getJSONObject(i)
                val fileId = fileObj.getString("id")
                val fileName = fileObj.getString("name")
                val fileSize = fileObj.getLong("size")
                val mime = fileObj.optString("mimeType", "application/octet-stream")

                val filesDir = File(context.filesDir, "received_files")
                if (!filesDir.exists()) filesDir.mkdirs()
                val filePath = File(filesDir, "${fileId}_${fileName}").absolutePath

                files += FileEntry(path = filePath, mime = mime, sizeBytes = fileSize, orderIdx = i)
            }

            Paket(
                id = PaketId(paketId),
                meta = meta,
                sizeBytes = files.sumOf { it.sizeBytes },
                sha256 = "",
                fileCount = files.size,
                createdUtc = System.currentTimeMillis(),
                files = files,
                currentHopCount = currentHopCount,
                maxHopCount = meta.maxHops
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing manifest", e)
            // Fallback-Paket minimal
            Paket(
                id = PaketId(paketId),
                meta = PaketMeta(
                    title = "Empfangenes Paket",
                    description = "Fehler beim Parsen des Manifests",
                    tags = emptyList(),
                    ttlSeconds = 3600,
                    priority = 1
                ),
                sizeBytes = 0L,
                sha256 = "",
                fileCount = 0,
                createdUtc = System.currentTimeMillis(),
                files = emptyList()
            )
        }
    }
} 