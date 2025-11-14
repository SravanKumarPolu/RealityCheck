package com.realitycheck.app.ui.viewmodel

import com.realitycheck.app.data.DataExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    val exportService: DataExportService
) : androidx.lifecycle.ViewModel()

