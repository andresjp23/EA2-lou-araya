package com.example.evaluacion2.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.evaluacion2.domain.validation.ProductQrPayload
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ProductQrCode(
    productId: Int,
    productName: String,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(productId) {
        val matrix = QRCodeWriter().encode(
            ProductQrPayload.forProduct(productId),
            BarcodeFormat.QR_CODE,
            QR_SIZE,
            QR_SIZE,
            mapOf(EncodeHintType.MARGIN to QUIET_ZONE_SIZE)
        )
        val pixels = IntArray(QR_SIZE * QR_SIZE) { index ->
            val x = index % QR_SIZE
            val y = index / QR_SIZE
            if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
        Bitmap.createBitmap(pixels, QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    Image(
        bitmap = qrBitmap,
        contentDescription = "Código QR de $productName",
        modifier = modifier,
        contentScale = ContentScale.FillBounds
    )
}

private const val QR_SIZE = 512
private const val QUIET_ZONE_SIZE = 2
