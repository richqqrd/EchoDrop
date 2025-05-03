package com.example.echodrop.model.domain

/**
 * data class for meta data of a package
 */
data class PaketMeta(
    val title: String,
    val description: String?,
    val tags: List<String>,
    val ttlSeconds: Int,
    val priority: Int,
)
