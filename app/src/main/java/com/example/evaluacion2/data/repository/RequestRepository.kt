package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.QuoteRequest

class RequestRepository(private val databaseHelper: AppDatabaseHelper) {
    fun getForUser(userId: Int): List<QuoteRequest> = query(
        "${AppDatabaseHelper.COLUMN_USER_ID} = ?",
        arrayOf(userId.toString())
    )

    fun getOwnerName(userId: Int): String = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_USERS, arrayOf(AppDatabaseHelper.COLUMN_NAME), "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString()), null, null, null
    ).use { if (it.moveToFirst()) it.getString(0) else "Cliente" }

    fun getAll(): List<QuoteRequest> = query(null, null)

    fun getAllWithOwners(): List<Pair<QuoteRequest, String>> = databaseHelper.readableDatabase.rawQuery(
        "SELECT r.*, u.${AppDatabaseHelper.COLUMN_NAME} AS owner_name FROM ${AppDatabaseHelper.TABLE_REQUESTS} r INNER JOIN ${AppDatabaseHelper.TABLE_USERS} u ON r.${AppDatabaseHelper.COLUMN_USER_ID} = u.${AppDatabaseHelper.COLUMN_ID} ORDER BY r.${AppDatabaseHelper.COLUMN_CREATED_AT} DESC", null
    ).use { cursor -> buildList {
        while (cursor.moveToNext()) {
            val productIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PRODUCT_ID)
            add(QuoteRequest(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USER_ID)),
                productId = if (cursor.isNull(productIndex)) null else cursor.getInt(productIndex),
                eventType = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_EVENT_TYPE)),
                dimensions = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DIMENSIONS)),
                material = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_MATERIAL)),
                colors = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_COLORS)),
                requiredDate = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_REQUIRED_DATE)),
                observations = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_OBSERVATIONS)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_STATUS)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_CREATED_AT))
            ) to cursor.getString(cursor.getColumnIndexOrThrow("owner_name")))
        }
    } }

    fun updateStatus(requestId: Int, status: String): Boolean {
        require(status in VALID_STATUSES) { "Estado de solicitud no válido" }
        val values = ContentValues().apply { put(AppDatabaseHelper.COLUMN_STATUS, status) }
        return databaseHelper.writableDatabase.update(
            AppDatabaseHelper.TABLE_REQUESTS,
            values,
            "${AppDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(requestId.toString())
        ) > 0
    }

    fun save(request: QuoteRequest): Long {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_USER_ID, request.userId)
            putNullable(AppDatabaseHelper.COLUMN_PRODUCT_ID, request.productId)
            put(AppDatabaseHelper.COLUMN_EVENT_TYPE, request.eventType)
            put(AppDatabaseHelper.COLUMN_DIMENSIONS, request.dimensions)
            put(AppDatabaseHelper.COLUMN_MATERIAL, request.material)
            put(AppDatabaseHelper.COLUMN_COLORS, request.colors)
            put(AppDatabaseHelper.COLUMN_REQUIRED_DATE, request.requiredDate)
            put(AppDatabaseHelper.COLUMN_OBSERVATIONS, request.observations)
            put(AppDatabaseHelper.COLUMN_STATUS, request.status)
            put(AppDatabaseHelper.COLUMN_CREATED_AT, request.createdAt)
        }
        return databaseHelper.writableDatabase.insertOrThrow(AppDatabaseHelper.TABLE_REQUESTS, null, values)
    }

    private fun ContentValues.putNullable(key: String, value: Int?) {
        if (value == null) putNull(key) else put(key, value)
    }

    private fun query(selection: String?, selectionArgs: Array<String>?): List<QuoteRequest> =
        databaseHelper.readableDatabase.query(
            AppDatabaseHelper.TABLE_REQUESTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${AppDatabaseHelper.COLUMN_CREATED_AT} DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val productIdIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PRODUCT_ID)
                    add(
                        QuoteRequest(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
                            userId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USER_ID)),
                            productId = if (cursor.isNull(productIdIndex)) null else cursor.getInt(productIdIndex),
                            eventType = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_EVENT_TYPE)),
                            dimensions = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DIMENSIONS)),
                            material = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_MATERIAL)),
                            colors = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_COLORS)),
                            requiredDate = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_REQUIRED_DATE)),
                            observations = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_OBSERVATIONS)),
                            status = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_STATUS)),
                            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_CREATED_AT))
                        )
                    )
                }
            }
        }

    companion object {
        private val VALID_STATUSES = QuoteRequest.ALL_STATUSES
    }
}
