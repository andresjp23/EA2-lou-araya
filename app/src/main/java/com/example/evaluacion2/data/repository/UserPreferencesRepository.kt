package com.example.evaluacion2.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.UserPreferences

class UserPreferencesRepository(private val databaseHelper: AppDatabaseHelper) {
    fun get(userId: Int): UserPreferences? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_PREFERENCES,
        null,
        "${AppDatabaseHelper.COLUMN_USER_ID} = ?",
        arrayOf(userId.toString()),
        null,
        null,
        null
    ).use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        UserPreferences(
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USER_ID)),
            notificationsEnabled = cursor.getInt(
                cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_NOTIFICATIONS_ENABLED)
            ) != 0
        )
    }

    fun save(preferences: UserPreferences) {
        val database = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_USER_ID, preferences.userId)
            put(AppDatabaseHelper.COLUMN_NOTIFICATIONS_ENABLED, if (preferences.notificationsEnabled) 1 else 0)
        }
        database.insertWithOnConflict(
            AppDatabaseHelper.TABLE_PREFERENCES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}
