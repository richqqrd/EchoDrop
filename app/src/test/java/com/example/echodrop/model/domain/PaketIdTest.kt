package com.example.echodrop.model.domain

import com.example.echodrop.model.domainLayer.model.PaketId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `PaketId` value class.
 */
class PaketIdTest {

 @Nested
 @DisplayName("PaketId Construction Tests")
 inner class PaketIdConstructionTest {

  @Test
  @DisplayName("Creates a PaketId with the correct value")
  fun createPaketIdWithCorrectValue() {
   val id = "test-paket-123"
   val paketId = com.example.echodrop.model.domainLayer.model.PaketId(id)

   assertEquals(id, paketId.value)
  }
 }

 @Nested
 @DisplayName("PaketId Equality Tests")
 inner class PaketIdEqualityTest {

  @Test
  @DisplayName("Two PaketIds with the same value are equal")
  fun twoIdenticalPaketIdsAreEqual() {
   val id1 = com.example.echodrop.model.domainLayer.model.PaketId("test-id-123")
   val id2 = com.example.echodrop.model.domainLayer.model.PaketId("test-id-123")

   assertEquals(id1, id2)
   assertEquals(id1.hashCode(), id2.hashCode())
  }

  @Test
  @DisplayName("Two PaketIds with different values are not equal")
  fun twoDifferentPaketIdsAreNotEqual() {
   val id1 = com.example.echodrop.model.domainLayer.model.PaketId("test-id-123")
   val id2 = com.example.echodrop.model.domainLayer.model.PaketId("test-id-456")

   assertNotEquals(id1, id2)
   assertNotEquals(id1.hashCode(), id2.hashCode())
  }
 }

}