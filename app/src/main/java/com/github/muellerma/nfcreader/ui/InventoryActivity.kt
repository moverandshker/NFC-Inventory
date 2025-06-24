package com.github.muellerma.nfcreader.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.muellerma.nfcreader.App
import com.github.muellerma.nfcreader.R
import com.github.muellerma.nfcreader.data.ToteEntity
import com.github.muellerma.nfcreader.repository.InventoryRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {
    private lateinit var repository: InventoryRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ToteAdapter
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        // Initialize repository
        repository = (application as App).repository

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Inventory"

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        adapter = ToteAdapter { tote ->
            // Handle tote item click - show details
            showToteDetails(tote)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup FAB for sync
        fab = findViewById(R.id.fab_sync)
        fab.setOnClickListener {
            syncData()
        }

        // Observe totes from database
        repository.getAllTotes().observe(this) { totes ->
            adapter.submitList(totes)
            updateEmptyState(totes.isEmpty())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_inventory, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_search -> {
                // TODO: Implement search functionality
                Snackbar.make(recyclerView, "Search coming soon", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_export -> {
                // TODO: Implement export functionality
                Snackbar.make(recyclerView, "Export coming soon", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun syncData() {
        fab.isEnabled = false
        lifecycleScope.launch {
            try {
                val result = repository.syncToSupabase()
                when (result) {
                    is com.github.muellerma.nfcreader.repository.SyncResult.Success -> {
                        val message = "✅ Synced ${result.syncedTotes} totes, ${result.syncedScans} scans"
                        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
                    }
                    is com.github.muellerma.nfcreader.repository.SyncResult.Error -> {
                        Snackbar.make(recyclerView, "❌ Sync failed: ${result.message}", Snackbar.LENGTH_LONG).show()
                    }
                    is com.github.muellerma.nfcreader.repository.SyncResult.AlreadySyncing -> {
                        Snackbar.make(recyclerView, "🔄 Sync already in progress", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(recyclerView, "❌ Sync error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                fab.isEnabled = true
            }
        }
    }

    private fun showToteDetails(tote: ToteEntity) {
        val syncStatus = if (tote.synced) "✅ Synced" else "⏳ Pending sync"
        val message = "Tote: ${tote.toteId}\nStatus: $syncStatus\nCreated: ${java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date(tote.createdAt))}"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tote Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        // TODO: Show/hide empty state view
        if (isEmpty) {
            supportActionBar?.subtitle = "No totes scanned yet"
        } else {
            supportActionBar?.subtitle = "${adapter.itemCount} totes"
        }
    }
}
