package com.example.evaluacion2.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.Favorite

class FavoriteRepository(private val databaseHelper: AppDatabaseHelper) {
    fun getForUser(userId: Int): List<Favorite> = readFavorites(AppDatabaseHelper.TABLE_FAVORITES, userId)
    fun getHomeFavorites(userId: Int): List<Favorite> = readFavorites(AppDatabaseHelper.TABLE_HOME_FAVORITES, userId)

    fun add(userId: Int, productId: Int): Boolean = insertFavorite(AppDatabaseHelper.TABLE_FAVORITES, userId, productId)
    fun remove(userId: Int, productId: Int): Boolean = removeFavorite(AppDatabaseHelper.TABLE_FAVORITES, userId, productId)

    fun isHomeFavorite(userId: Int, productId: Int): Boolean = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_HOME_FAVORITES,
        arrayOf(AppDatabaseHelper.COLUMN_ID),
        "${AppDatabaseHelper.COLUMN_USER_ID} = ? AND ${AppDatabaseHelper.COLUMN_PRODUCT_ID} = ?",
        arrayOf(userId.toString(), productId.toString()), null, null, null
    ).use { it.moveToFirst() }

    fun toggleHomeFavorite(userId: Int, productId: Int): Boolean = if (isHomeFavorite(userId, productId)) {
        removeFavorite(AppDatabaseHelper.TABLE_HOME_FAVORITES, userId, productId)
    } else {
        insertFavorite(AppDatabaseHelper.TABLE_HOME_FAVORITES, userId, productId)
    }

    private fun insertFavorite(table: String, userId: Int, productId: Int): Boolean {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_USER_ID, userId)
            put(AppDatabaseHelper.COLUMN_PRODUCT_ID, productId)
        }
        return databaseHelper.writableDatabase.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    private fun removeFavorite(table: String, userId: Int, productId: Int): Boolean = databaseHelper.writableDatabase.delete(
        table,
        "${AppDatabaseHelper.COLUMN_USER_ID} = ? AND ${AppDatabaseHelper.COLUMN_PRODUCT_ID} = ?",
        arrayOf(userId.toString(), productId.toString())
    ) > 0

    private fun readFavorites(table: String, userId: Int): List<Favorite> = databaseHelper.readableDatabase.query(
        table, null, "${AppDatabaseHelper.COLUMN_USER_ID} = ?", arrayOf(userId.toString()), null, null,
        "${AppDatabaseHelper.COLUMN_ID} DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(
                Favorite(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USER_ID)),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PRODUCT_ID))
                )
            )
        }
    }
}
