package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.impl.transport.ChunkIOImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class ChunkIOImplTest {

    private lateinit var ctx: Context
    private lateinit var chunkIO: ChunkIOImpl

    @Before
    fun setUp() {
        ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val getPaket = mockk<GetPaketDetailUseCase>(relaxed = true)
        val savePaket = mockk<SavePaketUseCase>(relaxed = true)
        chunkIO = ChunkIOImpl(ctx, getPaket, savePaket)
    }

    @Test
    fun splitFile_splitsIntoExpectedChunks() {
        val tmp = File.createTempFile("split", ".bin", ctx.cacheDir)
        tmp.writeBytes(ByteArray(150 * 1024) { Random.nextBytes(1)[0] }) // 150 KiB

        val chunks = chunkIO.splitFile(tmp, "pref").toList()

        assertEquals(3, chunks.size)
        assertEquals(chunkIO.chunkSize, chunks[0].second.size)
        assertEquals(chunkIO.chunkSize, chunks[1].second.size)
        assertEquals(150 * 1024 - 2 * chunkIO.chunkSize, chunks[2].second.size)
    }

    @Test
    fun appendChunk_createsFileAndReturnsTrue() = runTest {
        val ok = chunkIO.appendChunk(PaketId("p1"), "fileA", "hello".encodeToByteArray())
        assertTrue(ok)

        val expected = File(ctx.filesDir, "received_files/p1_fileA.part")
        assertTrue("File should exist", expected.exists())
        assertEquals("hello", expected.readText())
    }
} 