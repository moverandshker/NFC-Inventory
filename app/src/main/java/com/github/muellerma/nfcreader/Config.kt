package com.github.muellerma.nfcreader

object Config {
    // TODO: Move these to BuildConfig or environment variables for production
    const val SUPABASE_URL = "https://ojroobxqivrovoiaprch.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9qcm9vYnhxaXZyb3ZvaWFwcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAxMjM5NTYsImV4cCI6MjA2NTY5OTk1Nn0.l_FcKPYBVDYg1R5-asLY8fPBojYikdY1toikoX4TXhY"
    
    // Note: This is currently using a public anonymous key which is safe to commit
    // For production, these should be moved to BuildConfig or environment variables
}
