package com.github.muellerma.nfcreader.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToteDao {
    @Query("SELECT * FROM totes ORDER BY createdAt DESC")
    fun getAllTotes(): LiveData<List<ToteEntity>>

    @Query("SELECT * FROM totes WHERE toteId = :toteId LIMIT 1")
    suspend fun getToteByToteId(toteId: String): ToteEntity?

    @Query("SELECT * FROM totes WHERE synced = 0")
    suspend fun getUnsyncedTotes(): List<ToteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTote(tote: ToteEntity): Long

    @Update
    suspend fun updateTote(tote: ToteEntity)

    @Query("UPDATE totes SET synced = 1, id = :serverId WHERE toteId = :toteId")
    suspend fun markToteSynced(toteId: String, serverId: Long)
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): LiveData<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE synced = 0")
    suspend fun getUnsyncedScans(): List<ScanHistoryEntity>

    @Insert
    suspend fun insertScan(scan: ScanHistoryEntity): Long

    @Query("UPDATE scan_history SET synced = 1 WHERE id = :scanId")
    suspend fun markScanSynced(scanId: Long)

    @Query("DELETE FROM scan_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldScans(cutoffTime: Long)
}
