package com.example.echodrop.model.domainLayer.model

/**
 * Represents metadata for a package.
 *
 * @property title The title of the package.
 * @property description An optional description of the package.
 * @property tags A list of tags associated with the package.
 * @property ttlSeconds The time-to-live (TTL) of the package in seconds.
 * @property priority The priority level of the package.
 */
data class PaketMeta(
    val title: String,
    val description: String?,
    val tags: List<String>,
    val ttlSeconds: Int,
    val priority: Int,
)
