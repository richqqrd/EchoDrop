package com.example.echodrop.model.domainLayer.transport

/**
 * Periodische Hintergrundaufgaben zur Wartung der Datenbank:
 *  - Löschen alter ConnectionAttempts
 *  - Bereinigen veralteter Peers
 */
interface MaintenanceScheduler {
    /** Startet die wiederkehrenden Wartungsjobs (idempotent). */
    fun start()

    /** Stoppt alle laufenden Wartungsjobs. */
    fun stop()
} 