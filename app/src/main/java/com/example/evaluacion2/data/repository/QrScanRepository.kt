package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper

class QrScanRepository(private val databaseHelper: AppDatabaseHelper) {
    fun record(userId: Int?, productId: Int?, scannedAt: String): Long {
        val values = ContentValues().apply {
            if (userId == null) putNull(AppDatabaseHelper.COLUMN_USER_ID)
            else put(AppDatabaseHelper.COLUMN_USER_ID, userId)
            if (productId == null) putNull(AppDatabaseHelper.COLUMN_PRODUCT_ID)
            else put(AppDatabaseHelper.COLUMN_PRODUCT_ID, productId)
            put(AppDatabaseHelper.COLUMN_SCANNED_AT, scannedAt)
        }
        return databaseHelper.writableDatabase.insertOrThrow(AppDatabaseHelper.TABLE_QR_SCANS, null, values)
    }
}
