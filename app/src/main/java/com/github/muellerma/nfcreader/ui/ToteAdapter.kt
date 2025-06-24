package com.github.muellerma.nfcreader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.muellerma.nfcreader.R
import com.github.muellerma.nfcreader.data.ToteEntity
import java.text.SimpleDateFormat
import java.util.*

class ToteAdapter(
    private val onToteClick: (ToteEntity) -> Unit
) : ListAdapter<ToteEntity, ToteAdapter.ToteViewHolder>(ToteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tote, parent, false)
        return ToteViewHolder(view, onToteClick)
    }

    override fun onBindViewHolder(holder: ToteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ToteViewHolder(
        itemView: View,
        private val onToteClick: (ToteEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val toteIdText: TextView = itemView.findViewById(R.id.tote_id)
        private val syncStatusText: TextView = itemView.findViewById(R.id.sync_status)
        private val timestampText: TextView = itemView.findViewById(R.id.timestamp)
        private val syncIndicator: View = itemView.findViewById(R.id.sync_indicator)

        fun bind(tote: ToteEntity) {
            toteIdText.text = tote.toteId
            
            // Sync status
            if (tote.synced) {
                syncStatusText.text = "Synced"
                syncStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                syncIndicator.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
            } else {
                syncStatusText.text = "Pending"
                syncStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                syncIndicator.setBackgroundColor(itemView.context.getColor(android.R.color.holo_orange_light))
            }
            
            // Timestamp
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            timestampText.text = dateFormat.format(Date(tote.createdAt))
            
            // Click listener
            itemView.setOnClickListener {
                onToteClick(tote)
            }
        }
    }
}

class ToteDiffCallback : DiffUtil.ItemCallback<ToteEntity>() {
    override fun areItemsTheSame(oldItem: ToteEntity, newItem: ToteEntity): Boolean {
        return oldItem.toteId == newItem.toteId
    }

    override fun areContentsTheSame(oldItem: ToteEntity, newItem: ToteEntity): Boolean {
        return oldItem == newItem
    }
}
