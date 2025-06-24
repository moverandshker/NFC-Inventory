package com.github.muellerma.nfcreader.repository

import androidx.lifecycle.LiveData
import com.github.muellerma.nfcreader.App
import com.github.muellerma.nfcreader.Tote
import com.github.muellerma.nfcreader.ScanHistory
import com.github.muellerma.nfcreader.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class InventoryRepository(
    private val toteDao: ToteDao,
    private val scanHistoryDao: ScanHistoryDao
) {
    private val isSyncing = AtomicBoolean(false)

    // LiveData for UI observation
    fun getAllTotes(): LiveData<List<ToteEntity>> = toteDao.getAllTotes()
    fun getAllScans(): LiveData<List<ScanHistoryEntity>> = scanHistoryDao.getAllScans()

    suspend fun scanTote(toteId: String): ScanResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if tote exists locally
                var toteEntity = toteDao.getToteByToteId(toteId)
                var toteDbId: Long

                if (toteEntity == null) {
                    // Create new tote locally
                    toteDbId = toteDao.insertTote(ToteEntity(toteId = toteId))
                    toteEntity = ToteEntity(id = toteDbId, toteId = toteId)
                } else {
                    toteDbId = toteEntity.id ?: 0L
                }

                // Record scan locally
                val scanId = scanHistoryDao.insertScan(
                    ScanHistoryEntity(toteId = toteDbId, action = "scanned")
                )

                // Try to sync immediately if online
                syncToSupabase()

                ScanResult.Success(toteEntity, scanId)
            } catch (e: Exception) {
                ScanResult.Error("Failed to record scan: ${e.message}")
            }
        }
    }

    suspend fun syncToSupabase(): SyncResult {
        if (isSyncing.getAndSet(true)) {
            return SyncResult.AlreadySyncing
        }

        return withContext(Dispatchers.IO) {
            try {
                var syncedTotes = 0
                var syncedScans = 0

                // Sync unsynced totes
                val unsyncedTotes = toteDao.getUnsyncedTotes()
                for (toteEntity in unsyncedTotes) {
                    try {
                        val serverTote = App.supabase.from("totes")
                            .insert(Tote(tote_id = toteEntity.toteId)) { select() }
                            .decodeSingle<Tote>()
                        
                        serverTote.id?.let { serverId ->
                            toteDao.markToteSynced(toteEntity.toteId, serverId)
                            syncedTotes++
                        }
                    } catch (e: Exception) {
                        // Continue with other totes if one fails
                        e.printStackTrace()
                    }
                }

                // Sync unsynced scans
                val unsyncedScans = scanHistoryDao.getUnsyncedScans()
                for (scanEntity in unsyncedScans) {
                    try {
                        App.supabase.from("scan_history")
                            .insert(ScanHistory(tote_id = scanEntity.toteId, action = scanEntity.action))
                        
                        scanHistoryDao.markScanSynced(scanEntity.id)
                        syncedScans++
                    } catch (e: Exception) {
                        // Continue with other scans if one fails
                        e.printStackTrace()
                    }
                }

                SyncResult.Success(syncedTotes, syncedScans)
            } catch (e: Exception) {
                SyncResult.Error("Sync failed: ${e.message}")
            } finally {
                isSyncing.set(false)
            }
        }
    }

    suspend fun cleanupOldScans(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        scanHistoryDao.deleteOldScans(cutoffTime)
    }
}

sealed class ScanResult {
    data class Success(val tote: ToteEntity, val scanId: Long) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

sealed class SyncResult {
    data class Success(val syncedTotes: Int, val syncedScans: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    object AlreadySyncing : SyncResult()
}
