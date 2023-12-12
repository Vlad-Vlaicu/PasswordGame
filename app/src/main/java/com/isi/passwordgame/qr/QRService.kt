package com.isi.passwordgame.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRService {
    companion object {
        fun generateQRCode(text: String, width: Int, height: Int): Bitmap {
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix: BitMatrix =
                    multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
                val barcodeEncoder = BarcodeEncoder()
                return barcodeEncoder.createBitmap(bitMatrix)
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Error generating QR code: $e")
            }
        }
    }
}