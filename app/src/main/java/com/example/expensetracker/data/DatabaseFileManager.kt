package com.example.expensetracker.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class DatabaseFileManager(private val context: Context) {
    private val dbName = "expense_tracker_database"
    private val databasePath: String
        get() = context.getDatabasePath(dbName).absolutePath

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportDatabase(): Boolean {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                Log.e("DB_EXPORT", "Database file not found at ${dbFile.absolutePath}")
                return false
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "$dbName-$timestamp.db"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                AppDatabase.getDatabase(context).openHelper.readableDatabase.close()
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("DB_EXPORT", "Database exported to $uri")
                true
            } else {
                Log.e("DB_EXPORT", "Failed to get output stream for file.")
                false
            }
        } catch (e: Exception) {
            Log.e("DB_EXPORT", "Export failed with exception: ${e.message}", e)
            false
        }
    }

    fun importDatabase(uri: Uri): Boolean {
        return try {
            AppDatabase.closeDatabase()

            val currentDbFile = File(databasePath)
            val shmFile = File("$databasePath-shm")
            val walFile = File("$databasePath-wal")

            if (currentDbFile.exists()) currentDbFile.delete()
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(currentDbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}