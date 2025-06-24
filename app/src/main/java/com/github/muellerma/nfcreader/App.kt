package com.github.muellerma.nfcreader

import android.app.Application
import com.github.muellerma.nfcreader.data.InventoryDatabase
import com.github.muellerma.nfcreader.repository.InventoryRepository
import com.google.android.material.color.DynamicColors
import io.supabase.gotrue.GoTrue
import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.postgrest

class App : Application() {
    // Room database and repository
    private val database by lazy { InventoryDatabase.getDatabase(this) }
    val repository by lazy { InventoryRepository(database.toteDao(), database.scanHistoryDao()) }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        supabase = postgrest(
            url = Config.SUPABASE_URL,
            headers = mapOf(
                "apikey" to Config.SUPABASE_ANON_KEY
            )
        )
    }

    companion object {
        lateinit var supabase: Postgrest
            private set
    }
}