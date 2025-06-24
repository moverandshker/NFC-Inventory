package com.github.muellerma.nfcreader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "totes")
data class ToteEntity(
    @PrimaryKey val id: Long? = null,
    val toteId: String,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toteId: Long,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
