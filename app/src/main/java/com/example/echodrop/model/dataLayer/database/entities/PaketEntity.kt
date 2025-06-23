package com.example.echodrop.model.dataLayer.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta

/**
 * Represents a package (Paket) stored in the database.
 *
 * @property paketId The unique identifier of the package.
 * @property version The version of the package.
 * @property title The title of the package.
 * @property description An optional description of the package.
 * @property tags A list of tags associated with the package.
 * @property sizeBytes The total size of the package in bytes.
 * @property sha256 The SHA-256 checksum of the package.
 * @property fileCount The number of files contained in the package.
 * @property ttlSeconds The time-to-live (TTL) of the package in seconds.
 * @property priority The priority level of the package.
 * @property hopLimit An optional hop limit for the package.
 * @property manifestHash The hash of the package manifest.
 * @property createdUtc The timestamp (in UTC) when the package was created.
 */
@Entity(tableName = "paket")
data class PaketEntity(
    @PrimaryKey val paketId: String,
    val version: Int,
    val title: String,
    val description: String?,
    val tags: List<String>,
    val sizeBytes: Long,
    val sha256: String,
    val fileCount: Int,
    val ttlSeconds: Int,
    val priority: Int,
    val hopLimit: Int?,
    val currentHopCount: Int = 0,
    val manifestHash: String,
    val createdUtc: Long
    )

/**
 * Converts a `PaketEntity` to its domain representation.
 *
 * @param files The list of files associated with the package.
 * @return The domain representation of the package.
 */
fun PaketEntity.toDomain(files: List<FileEntry>): Paket =
    Paket(
        id = PaketId(paketId),
        meta = PaketMeta(
            title,
            description,
            tags,
            ttlSeconds,
            priority,
            maxHops = hopLimit
        ),
        sizeBytes = sizeBytes,
        sha256 = sha256,
        fileCount = fileCount,
        createdUtc = createdUtc,
        files = files,
                currentHopCount = currentHopCount, // Hier den aktuellen Hop-Count übergeben
        maxHopCount = hopLimit 
    )