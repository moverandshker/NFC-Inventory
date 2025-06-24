package com.github.muellerma.nfcreader.model

import kotlinx.serialization.Serializable

@Serializable
data class Tote(val id: Long? = null, val tote_id: String)

@Serializable
data class ScanHistory(val tote_id: Long, val action: String)
