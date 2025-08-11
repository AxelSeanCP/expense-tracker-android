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

class DatabaseFileManager(private val context: Context) {
    private val dbName = "expense_tracker_database"
    private val databasePath: String
        get() = context.getDatabasePath(dbName).absolutePath

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportDatabase(): Boolean {
        return try {
            val dbFile = File(databasePath)
            if (!dbFile.exists()) {
                return false
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "$dbName-$timestamp.db"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(uri: Uri): Boolean {
        return try {
            val currentDbFile = File(databasePath)
            if (currentDbFile.exists()) {
                currentDbFile.delete()
            }

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