package com.example.expensetracker.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.DatabaseFileManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ExportImportViewModel(private val databaseFileManager: DatabaseFileManager) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    fun exportDatabase() {
        viewModelScope.launch {
            if (databaseFileManager.exportDatabase()) {
                _uiEvent.emit("Database exported successfully! Check your device's storage")
            } else {
                _uiEvent.emit("Failed to export database")
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            if (databaseFileManager.importDatabase(uri)) {
                _uiEvent.emit("Database imported successfully! Please restart the app.")
            } else {
                _uiEvent.emit("Failed to import database")
            }
        }
    }
}

class ExportImportViewModelFactory(
    private val databaseFileManager: DatabaseFileManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportImportViewModel(databaseFileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}