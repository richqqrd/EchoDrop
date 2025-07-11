package com.example.echodrop.model.dataLayer.repository

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import kotlinx.coroutines.flow.first
import android.util.Log
import io.mockk.mockkStatic
import io.mockk.every
import io.mockk.unmockkAll

/**
 * Test class for the `PaketRepositoryImpl` implementation.
 */
@DisplayName("PaketRepositoryImpl Tests")
class PaketRepositoryImplTest {

 private lateinit var mockPaketDao: PaketDao
 private lateinit var mockFileEntryDao: FileEntryDao

 private lateinit var repository: PaketRepositoryImpl

 private val testPaketId = "paket-123"
 private val testPaketEntity = PaketEntity(
  paketId = testPaketId,
  version = 1,
  title = "Test Package",
  description = "Test description",
  tags = listOf("test", "unit"),
  sizeBytes = 1024L,
  fileCount = 2,
  ttlSeconds = 3600,
  priority = 1,
  hopLimit = null,
  createdUtc = 1620000000000L
 )

 private val testFileEntries = listOf(
  FileEntryEntity(
   fileId = "file-1",
   paketOwnerId = testPaketId,
   path = "test/file1.txt",
   mime = "text/plain",
   sizeBytes = 512L,
   orderIdx = 0
  ),
  FileEntryEntity(
   fileId = "file-2",
   paketOwnerId = testPaketId,
   path = "test/file2.jpg",
   mime = "image/jpeg",
   sizeBytes = 512L,
   orderIdx = 1
  )
 )

 private val testDomainFiles = listOf(
     FileEntry("test/file1.txt", "text/plain", 512L, 0),
     FileEntry("test/file2.jpg", "image/jpeg", 512L, 1)
 )

 @BeforeEach
 fun setup() {
  mockPaketDao = mock(PaketDao::class.java)
  mockFileEntryDao = mock(FileEntryDao::class.java)
  repository = PaketRepositoryImpl(mockPaketDao, mockFileEntryDao)

  mockkStatic(Log::class)
  every { Log.d(any(), any()) } returns 0
 }

 @org.junit.jupiter.api.AfterEach
 fun tearDown() {
  unmockkAll()
 }

 @Test
 @DisplayName("observeInbox returns mapped domain models with empty file lists")
 fun observeInboxReturnsMappedDomainModels() = runTest {
  whenever(mockPaketDao.observeAll()).thenReturn(flowOf(listOf(testPaketEntity)))

  val result = repository.observeInbox().first()

  assertEquals(1, result.size)
  assertEquals(PaketId(testPaketId), result[0].id)
  assertEquals(testPaketEntity.title, result[0].meta.title)
  assertEquals(testPaketEntity.description, result[0].meta.description)
  assertTrue(result[0].files.isEmpty(), "observeInbox should use empty file list")
 }

 @Test
 @DisplayName("getPaket returns null when entity not found")
 fun getPaketReturnsNullWhenEntityNotFound() = runTest {
  whenever(mockPaketDao.findById(any())).thenReturn(null)

  val result = repository.getPaket(PaketId("non-existent"))

  assertNull(result, "Result should be null when entity not found")
 }

 @Test
 @DisplayName("getPaket returns complete domain model with files when found")
 fun getPaketReturnsCompleteDomainModelWithFilesWhenFound() = runTest {
  whenever(mockPaketDao.findById(testPaketId)).thenReturn(testPaketEntity)
  whenever(mockFileEntryDao.findByPaket(testPaketId)).thenReturn(testFileEntries)

  val result = repository.getPaket(PaketId(testPaketId))

  assertNotNull(result, "Result should not be null")
  assertEquals(PaketId(testPaketId), result?.id)
  assertEquals(testPaketEntity.title, result?.meta?.title)
  assertEquals(2, result?.files?.size, "Should have 2 files")
  assertEquals("test/file1.txt", result?.files?.get(0)?.path)
  assertEquals("test/file2.jpg", result?.files?.get(1)?.path)
 }

 @Test
 @DisplayName("insert creates paket and file entries with correct data")
 fun insertCreatesPaketAndFileEntriesWithCorrectData() = runTest {
  val testMeta = PaketMeta(
   title = "New Package",
   description = "New description",
   tags = listOf("new", "test"),
   ttlSeconds = 7200,
   priority = 2
  )

  val paketCaptor = argumentCaptor<PaketEntity>()
  val filesCaptor = argumentCaptor<List<FileEntryEntity>>()

  val resultId = repository.insert(testMeta, testDomainFiles)

  verify(mockPaketDao).upsert(paketCaptor.capture())
  verify(mockFileEntryDao).insertAll(filesCaptor.capture())

  val capturedPaket = paketCaptor.firstValue
  assertEquals(testMeta.title, capturedPaket.title)
  assertEquals(testMeta.description, capturedPaket.description)
  assertEquals(testMeta.tags, capturedPaket.tags)
  assertEquals(testMeta.ttlSeconds, capturedPaket.ttlSeconds)
  assertEquals(testMeta.priority, capturedPaket.priority)
  assertEquals(1024L, capturedPaket.sizeBytes, "Size should be sum of file sizes")
  assertEquals(2, capturedPaket.fileCount)

  val capturedFiles = filesCaptor.firstValue
  assertEquals(2, capturedFiles.size)
  assertEquals(capturedPaket.paketId, capturedFiles[0].paketOwnerId)
  assertEquals(capturedPaket.paketId, capturedFiles[1].paketOwnerId)
  assertEquals("test/file1.txt", capturedFiles[0].path)
  assertEquals("test/file2.jpg", capturedFiles[1].path)

  assertEquals(capturedPaket.paketId, resultId.value)
 }

 @Test
 @DisplayName("updateMeta does nothing when paket not found")
 fun updateMetaDoesNothingWhenPaketNotFound() = runTest {
  whenever(mockPaketDao.findById(any())).thenReturn(null)

  repository.updateMeta(PaketId("non-existent"), 7200, 3)

  verify(mockPaketDao, never()).upsert(any())
 }

 @Test
 @DisplayName("updateMeta updates TTL and priority when paket found")
 fun updateMetaUpdatesTtlAndPriorityWhenPaketFound() = runTest {
  whenever(mockPaketDao.findById(testPaketId)).thenReturn(testPaketEntity)
  val paketCaptor = argumentCaptor<PaketEntity>()

  repository.updateMeta(PaketId(testPaketId), 7200, 3)

  verify(mockPaketDao).upsert(paketCaptor.capture())
  val capturedPaket = paketCaptor.firstValue
  assertEquals(testPaketId, capturedPaket.paketId)
  assertEquals(7200, capturedPaket.ttlSeconds)
  assertEquals(3, capturedPaket.priority)
  assertEquals(testPaketEntity.title, capturedPaket.title)
  assertEquals(testPaketEntity.description, capturedPaket.description)
 }

 @Test
 @DisplayName("delete calls DAO with correct ID")
 fun deleteCallsDaoWithCorrectId() = runTest {
  repository.delete(PaketId(testPaketId))

  verify(mockPaketDao).deleteById(testPaketId)
 }

 @Test
 @DisplayName("purgeExpire calls DAO with correct timestamp")
 fun purgeExpireCallsDaoWithCorrectTimestamp() = runTest {
  val timestamp = 1620000000000L
  whenever(mockPaketDao.purgeExpired(timestamp)).thenReturn(3)

  val result = repository.purgeExpire(timestamp)

  verify(mockPaketDao).purgeExpired(timestamp)
  assertEquals(3, result, "Should return number of deleted items")
 }
}