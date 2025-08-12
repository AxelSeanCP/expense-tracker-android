package com.example.expensetracker.ui

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.data.DatabaseFileManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.example.expensetracker.utils.MessageEvent

sealed class ExportImportUiEvent {
    data class ShowSnackbar(@StringRes val messageResId: Int, val formatArgs: List<Any> = emptyList()) : ExportImportUiEvent()
    object RestartApp : ExportImportUiEvent()
}

class ExportImportViewModel(private val databaseFileManager: DatabaseFileManager) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<ExportImportUiEvent>()
    val uiEvent: SharedFlow<ExportImportUiEvent> = _uiEvent.asSharedFlow()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportDatabase() {
        viewModelScope.launch {
            if (databaseFileManager.exportDatabase()) {
                _uiEvent.emit(ExportImportUiEvent.ShowSnackbar(R.string.database_exported))
            } else {
                _uiEvent.emit(ExportImportUiEvent.ShowSnackbar(R.string.export_failed))
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            if (databaseFileManager.importDatabase(uri)) {
                _uiEvent.emit(ExportImportUiEvent.ShowSnackbar(R.string.database_imported))
                _uiEvent.emit(ExportImportUiEvent.RestartApp)
            } else {
                _uiEvent.emit(ExportImportUiEvent.ShowSnackbar(R.string.import_failed))
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