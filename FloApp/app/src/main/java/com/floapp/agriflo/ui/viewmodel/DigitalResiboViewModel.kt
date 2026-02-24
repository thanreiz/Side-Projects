package com.floapp.agriflo.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.data.local.dao.FertilizerReceiptDao
import com.floapp.agriflo.data.local.entity.FertilizerReceiptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Lightweight display model for the receipt list
data class ReceiptDisplayModel(
    val id: String,
    val productName: String,
    val purchaseDate: String,
    val costPhp: Double,
    val validated: Boolean,
    val pdfExportUri: String?
)

@HiltViewModel
class DigitalResiboViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fertilizerReceiptDao: FertilizerReceiptDao
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    val receipts: StateFlow<List<ReceiptDisplayModel>> = fertilizerReceiptDao.getAllReceipts()
        .map { entities ->
            entities.map { e ->
                ReceiptDisplayModel(
                    id = e.id,
                    productName = e.productName,
                    purchaseDate = e.purchaseDate,
                    costPhp = e.costPhp,
                    validated = e.validated,
                    pdfExportUri = e.pdfExportUri
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Launches the device camera for image capture.
     * OCR processing is handled after the image is returned in the Activity result.
     * For this initial build, we insert a stub receipt for demonstration.
     */
    fun startOcrCapture() {
        viewModelScope.launch {
            _isScanning.value = true
            // TODO: Wire to ML Kit TextRecognition via Activity result contract
            // For now, insert a demo receipt so the UI shows something
            val stubReceipt = FertilizerReceiptEntity(
                id = UUID.randomUUID().toString(),
                cropId = null,
                productName = "14-14-14 NPK (Demo)",
                supplierName = "Mabuhay Agri-Supply Store",
                quantityKg = 50.0,
                costPhp = 1900.0,
                purchaseDate = java.time.LocalDate.now().toString(),
                imageUri = "",
                rawOcrText = "(Demo receipt - replace with ML Kit output)",
                validated = false,
                pdfExportUri = null
            )
            fertilizerReceiptDao.insertReceipt(stubReceipt)
            _isScanning.value = false
        }
    }

    fun generatePdf(receiptId: String) {
        viewModelScope.launch {
            // TODO: Implement iText7 PDF generation
            // For now, stub the URI update
            fertilizerReceiptDao.updatePdfUri(receiptId, "/storage/emulated/0/Flo/receipts/$receiptId.pdf")
        }
    }

    fun openPdf(receiptId: String) {
        viewModelScope.launch {
            val receipt = fertilizerReceiptDao.getReceiptById(receiptId) ?: return@launch
            val pdfUri = receipt.pdfExportUri ?: return@launch
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri.toUri(), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
