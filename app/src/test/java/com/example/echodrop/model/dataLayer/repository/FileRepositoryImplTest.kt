package com.example.echodrop.model.dataLayer.repository

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.impl.repository.FileRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test class for the `FileRepositoryImpl` implementation.
 */
@DisplayName("FileRepositoryImpl Tests")
class FileRepositoryImplTest {

 private lateinit var mockFileEntryDao: FileEntryDao

 private lateinit var repository: FileRepositoryImpl

 private val testPaketId = PaketId("paket-123")
 private val testFileEntries = listOf(
  FileEntryEntity(
   fileId = "file-1",
   paketOwnerId = testPaketId.value,
   path = "documents/report.pdf",
   mime = "application/pdf",
   sizeBytes = 1024L,
   orderIdx = 0
  ),
  FileEntryEntity(
   fileId = "file-2",
   paketOwnerId = testPaketId.value,
   path = "images/photo.jpg",
   mime = "image/jpeg",
   sizeBytes = 2048L,
   orderIdx = 1
  )
 )

 private val testDomainFiles = listOf(
     FileEntry(
      "documents/report.pdf",
      "application/pdf",
      1024L,
      0
     ),
     FileEntry("images/photo.jpg", "image/jpeg", 2048L, 1)
 )

 @BeforeEach
 fun setup() {
  mockFileEntryDao = mock(FileEntryDao::class.java)
  repository = FileRepositoryImpl(mockFileEntryDao)
 }

 @Test
 @DisplayName("getFilesFor returns mapped domain models from DAO")
 fun getFilesForReturnsMappedDomainModels() = runTest {
  whenever(mockFileEntryDao.findByPaket(testPaketId.value)).thenReturn(testFileEntries)

  val result = repository.getFilesFor(testPaketId)

  assertEquals(2, result.size, "Should return two files")
  assertEquals("documents/report.pdf", result[0].path)
  assertEquals("application/pdf", result[0].mime)
  assertEquals(1024L, result[0].sizeBytes)
  assertEquals(0, result[0].orderIdx)

  assertEquals("images/photo.jpg", result[1].path)
  assertEquals("image/jpeg", result[1].mime)
  assertEquals(2048L, result[1].sizeBytes)
  assertEquals(1, result[1].orderIdx)
 }

 @Test
 @DisplayName("getFilesFor returns empty list when no files found")
 fun getFilesForReturnsEmptyListWhenNoFilesFound() = runTest {
  whenever(mockFileEntryDao.findByPaket(testPaketId.value)).thenReturn(emptyList())

  val result = repository.getFilesFor(testPaketId)

  assertTrue(result.isEmpty(), "Should return empty list when no files found")
 }

 @Test
 @DisplayName("insertAll creates file entities with correct data")
 fun insertAllCreatesFileEntitiesWithCorrectData() = runTest {
  val filesCaptor = argumentCaptor<List<FileEntryEntity>>()

  repository.insertAll(testPaketId, testDomainFiles)

  verify(mockFileEntryDao).insertAll(filesCaptor.capture())

  val capturedFiles = filesCaptor.firstValue
  assertEquals(2, capturedFiles.size, "Should create two file entities")

  assertEquals(testPaketId.value, capturedFiles[0].paketOwnerId)
  assertEquals(testPaketId.value, capturedFiles[1].paketOwnerId)

  assertEquals("documents/report.pdf", capturedFiles[0].path)
  assertEquals("application/pdf", capturedFiles[0].mime)
  assertEquals(1024L, capturedFiles[0].sizeBytes)
  assertEquals(0, capturedFiles[0].orderIdx)

  assertEquals("images/photo.jpg", capturedFiles[1].path)
  assertEquals("image/jpeg", capturedFiles[1].mime)
  assertEquals(2048L, capturedFiles[1].sizeBytes)
  assertEquals(1, capturedFiles[1].orderIdx)

  assertNotNull(capturedFiles[0].fileId)
  assertNotNull(capturedFiles[1].fileId)
  assertNotEquals(capturedFiles[0].fileId, capturedFiles[1].fileId)
 }

 @Test
 @DisplayName("insertAll handles empty file list")
 fun insertAllHandlesEmptyFileList() = runTest {
  repository.insertAll(testPaketId, emptyList())

  val filesCaptor = argumentCaptor<List<FileEntryEntity>>()
  verify(mockFileEntryDao).insertAll(filesCaptor.capture())

  assertTrue(filesCaptor.firstValue.isEmpty(), "Should insert empty list when no files provided")
 }

 @Test
 @DisplayName("deleteFor calls DAO with correct paket ID")
 fun deleteForCallsDaoWithCorrectPaketId() = runTest {
  repository.deleteFor(testPaketId)

  verify(mockFileEntryDao).deleteByPaket(testPaketId.value)
 }
}