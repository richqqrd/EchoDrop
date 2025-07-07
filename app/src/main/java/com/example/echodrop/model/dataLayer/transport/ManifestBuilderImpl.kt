package com.example.echodrop.model.dataLayer.transport

import android.util.Log
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.transport.ManifestBuilder
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestBuilderImpl @Inject constructor(
    private val getPaketDetail: GetPaketDetailUseCase
) : ManifestBuilder {

    companion object { private const val TAG = "ManifestBuilder" }

    override suspend fun build(paketId: PaketId): String = withContext(Dispatchers.Default) {
        Log.d(TAG, "Building manifest for paket ${paketId.value}")
        return@withContext try {
            val paket = getPaketDetail(paketId)
            if (paket == null) {
                Log.e(TAG, "Paket not found: ${paketId.value}")
                return@withContext fallbackManifest(paketId)
            }

            val manifestData = JSONObject().apply {
                put("paketId", paketId.value)
                put("title", paket.meta.title)
                put("description", paket.meta.description)

                val tagsArray = JSONArray().also { arr ->
                    paket.meta.tags.forEach { arr.put(it) }
                }
                put("tags", tagsArray)

                put("ttlSeconds", paket.meta.ttlSeconds)
                put("priority", paket.meta.priority)
                put("maxHops", paket.meta.maxHops ?: -1)
                put("currentHopCount", paket.currentHopCount)

                val filesArray = JSONArray()
                paket.files.forEachIndexed { idx, file ->
                    val fileName = File(file.path).name
                    JSONObject().apply {
                        put("id", "file_${idx}_${paketId.value}")
                        put("name", fileName)
                        put("size", file.sizeBytes)
                        put("mimeType", file.mime)
                    }.also { filesArray.put(it) }
                }
                put("files", filesArray)
            }.toString()

            Log.d(TAG, "Created manifest: $manifestData")
            manifestData
        } catch (e: Exception) {
            Log.e(TAG, "Error building manifest", e)
            fallbackManifest(paketId)
        }
    }

    private fun fallbackManifest(paketId: PaketId) = "{\"paketId\": \"${paketId.value}\", \"files\": []}"
} 