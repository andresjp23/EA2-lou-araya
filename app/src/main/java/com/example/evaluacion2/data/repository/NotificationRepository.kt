package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper

data class LocalNotification(val id: Int, val category: String, val title: String, val description: String, val createdAt: String, val read: Boolean)

class NotificationRepository(private val database: AppDatabaseHelper) {
    fun getForUser(userId: Int): List<LocalNotification> = database.readableDatabase.query(
        AppDatabaseHelper.TABLE_NOTIFICATIONS, null,
        "${AppDatabaseHelper.COLUMN_USER_ID} = ? OR ${AppDatabaseHelper.COLUMN_USER_ID} IS NULL", arrayOf(userId.toString()),
        null, null, "${AppDatabaseHelper.COLUMN_ID} DESC"
    ).use { cursor -> buildList {
        while (cursor.moveToNext()) add(LocalNotification(
            cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_CATEGORY)),
            cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_TITLE)),
            cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DESCRIPTION)),
            cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_CREATED_AT)),
            cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_READ)) != 0
        ))
    } }

    fun hasAny(): Boolean = database.readableDatabase.rawQuery("SELECT COUNT(*) FROM ${AppDatabaseHelper.TABLE_NOTIFICATIONS}", null).use { it.moveToFirst() && it.getInt(0) > 0 }

    fun add(userId: Int?, category: String, title: String, description: String, createdAt: String) {
        database.writableDatabase.insert(AppDatabaseHelper.TABLE_NOTIFICATIONS, null, ContentValues().apply {
            if (userId == null) putNull(AppDatabaseHelper.COLUMN_USER_ID) else put(AppDatabaseHelper.COLUMN_USER_ID, userId)
            put(AppDatabaseHelper.COLUMN_CATEGORY, category)
            put(AppDatabaseHelper.COLUMN_TITLE, title)
            put(AppDatabaseHelper.COLUMN_DESCRIPTION, description)
            put(AppDatabaseHelper.COLUMN_CREATED_AT, createdAt)
        })
    }

    fun markRead(id: Int, userId: Int) {
        database.writableDatabase.update(AppDatabaseHelper.TABLE_NOTIFICATIONS, ContentValues().apply { put(AppDatabaseHelper.COLUMN_READ, 1) },
            "${AppDatabaseHelper.COLUMN_ID} = ? AND (${AppDatabaseHelper.COLUMN_USER_ID} = ? OR ${AppDatabaseHelper.COLUMN_USER_ID} IS NULL)", arrayOf(id.toString(), userId.toString()))
    }
}
