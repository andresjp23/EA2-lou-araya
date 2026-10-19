package com.example.evaluacion2.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.User

class UserRepository(private val databaseHelper: AppDatabaseHelper) {
    fun registerUser(user: User): Result<Long> = runCatching {
        val database = databaseHelper.writableDatabase
        database.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(AppDatabaseHelper.COLUMN_NAME, user.name)
                put(AppDatabaseHelper.COLUMN_ALIAS, user.alias)
                put(AppDatabaseHelper.COLUMN_EMAIL, user.email)
                put(AppDatabaseHelper.COLUMN_PHONE, user.phone)
                put(AppDatabaseHelper.COLUMN_PASSWORD, user.password)
                put(AppDatabaseHelper.COLUMN_ROLE, user.role)
                put(AppDatabaseHelper.COLUMN_AVATAR_URI, user.avatarUri)
            }
            val userId = database.insertOrThrow(AppDatabaseHelper.TABLE_USERS, null, values)
            database.insertWithOnConflict(AppDatabaseHelper.TABLE_PREFERENCES, null, ContentValues().apply {
                put(AppDatabaseHelper.COLUMN_USER_ID, userId)
                put(AppDatabaseHelper.COLUMN_NOTIFICATIONS_ENABLED, 1)
            }, SQLiteDatabase.CONFLICT_IGNORE)
            database.setTransactionSuccessful()
            userId
        } finally { database.endTransaction() }
    }

    fun emailExists(email: String): Boolean = exists(AppDatabaseHelper.COLUMN_EMAIL, email.trim())
    fun aliasExists(alias: String): Boolean = exists(AppDatabaseHelper.COLUMN_ALIAS, alias.trim())

    private fun exists(column: String, value: String): Boolean = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_USERS, arrayOf(AppDatabaseHelper.COLUMN_ID), "$column = ? COLLATE NOCASE", arrayOf(value), null, null, null
    ).use { it.moveToFirst() }

    fun getUserById(userId: Int): User? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_USERS, null, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString()), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toUser() else null }

    fun login(email: String, password: String): User? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_USERS, null, "${AppDatabaseHelper.COLUMN_EMAIL} = ? COLLATE NOCASE AND ${AppDatabaseHelper.COLUMN_PASSWORD} = ?", arrayOf(email.trim(), password), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toUser() else null }

    fun updateProfile(userId: Int, name: String, alias: String, phone: String): Result<Boolean> = runCatching {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_NAME, name.trim())
            put(AppDatabaseHelper.COLUMN_ALIAS, alias.trim())
            put(AppDatabaseHelper.COLUMN_PHONE, phone.trim())
        }
        val database = databaseHelper.writableDatabase
        database.update(AppDatabaseHelper.TABLE_USERS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString())) > 0
    }

    private fun Cursor.toUser() = User(
        id = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
        name = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_NAME)),
        alias = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ALIAS)),
        email = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_EMAIL)),
        phone = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PHONE)),
        password = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PASSWORD)),
        role = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ROLE)),
        avatarUri = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_AVATAR_URI))
    )
}
