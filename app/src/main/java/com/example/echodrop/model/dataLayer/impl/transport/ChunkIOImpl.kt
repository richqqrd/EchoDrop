package com.example.echodrop.model.dataLayer.impl.transport

import android.content.Context
import android.util.Log
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.transport.ChunkIO
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChunkIOImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getPaketDetail: GetPaketDetailUseCase,
    private val savePaket: SavePaketUseCase
) : ChunkIO {

    companion object { const val DEFAULT_CHUNK_SIZE = 64 * 1024 }

    override val chunkSize: Int = DEFAULT_CHUNK_SIZE

    override fun splitFile(file: File, chunkIdPrefix: String): Sequence<Pair<String, ByteArray>> = sequence {
        file.inputStream().use { input ->
            val buffer = ByteArray(chunkSize)
            var bytesRead: Int
            var idx = 0
            while (input.read(buffer).also { bytesRead = it } != -1) {
                val data = if (bytesRead == chunkSize) buffer.copyOf() else buffer.copyOf(bytesRead)
                val chunkId = "chunk${idx}_$chunkIdPrefix"
                yield(chunkId to data)
                idx++
            }
        }
    }

    override suspend fun appendChunk(paketId: PaketId, fileId: String, data: ByteArray): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val paket = getPaketDetail(paketId)
                val fileEntry = paket?.files?.firstOrNull { it.path.contains(fileId) }

                val targetFile = if (fileEntry != null) {
                    File(fileEntry.path)
                } else {
                    val dir = File(context.filesDir, "received_files")
                    if (!dir.exists()) dir.mkdirs()
                    File(dir, "${paketId.value}_${fileId.replace('/', '_')}.part")
                }

                targetFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

                FileOutputStream(targetFile, true).use { it.write(data) }

                if (fileEntry == null) {
                    // update DB with correct path once we know it
                    paket?.let {
                        val updated = it.copy(files = it.files.map { fe ->
                            if (fe.path.contains(fileId)) fe.copy(path = targetFile.absolutePath) else fe
                        })
                        savePaket(updated)
                    }
                }
                true
            } catch (e: Exception) {
                Log.e("ChunkIO", "Error appending chunk", e)
                false
            }
        }
}