package com.example.scanner

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

data class BarcodeResult(
    val text: String,
    val format: String,
    val timestamp: Long = System.currentTimeMillis()
)

object BarcodeImageDecoder {
    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(
                DecodeHintType.POSSIBLE_FORMATS,
                listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.DATA_MATRIX,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.ITF
                )
            )
            put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
        }
        setHints(hints)
    }

    /**
     * Analyzes a Bitmap image to extract Barcode / QR data
     */
    fun decodeBitmap(bitmap: Bitmap): BarcodeResult? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)
            reader.reset()
            BarcodeResult(
                text = result.text,
                format = result.barcodeFormat.name
            )
        } catch (_: Exception) {
            reader.reset()
            null
        }
    }
}

class CameraBarcodeAnalyzer(
    private val onBarcodeDetected: (BarcodeResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(
                DecodeHintType.POSSIBLE_FORMATS,
                listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.CODE_39
                )
            )
        }
        setHints(hints)
    }

    private var lastAnalyzedTimestamp = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        // Throttle to avoid high CPU usage (process every 250ms)
        if (currentTimestamp - lastAnalyzedTimestamp < 250L) {
            image.close()
            return
        }

        try {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)
            reader.reset()
            lastAnalyzedTimestamp = currentTimestamp
            onBarcodeDetected(
                BarcodeResult(
                    text = result.text,
                    format = result.barcodeFormat.name
                )
            )
        } catch (_: Exception) {
            reader.reset()
        } finally {
            image.close()
        }
    }
}
