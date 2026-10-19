package com.example.evaluacion2.data.repository

import android.content.ContentValues
import android.database.Cursor
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.Product

class ProductRepository(private val databaseHelper: AppDatabaseHelper) {
    fun updateAvailable(productId: Int, available: Boolean): Boolean {
        val values = ContentValues().apply { put(AppDatabaseHelper.COLUMN_AVAILABLE, if (available) 1 else 0) }
        return databaseHelper.writableDatabase.update(AppDatabaseHelper.TABLE_PRODUCTS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(productId.toString())) > 0
    }

    fun getHomeFavoriteProducts(userId: Int): List<Product> = databaseHelper.readableDatabase.rawQuery(
        "SELECT p.* FROM ${AppDatabaseHelper.TABLE_PRODUCTS} p " +
            "INNER JOIN ${AppDatabaseHelper.TABLE_HOME_FAVORITES} f " +
            "ON p.${AppDatabaseHelper.COLUMN_ID} = f.${AppDatabaseHelper.COLUMN_PRODUCT_ID} " +
            "WHERE f.${AppDatabaseHelper.COLUMN_USER_ID} = ? ORDER BY f.${AppDatabaseHelper.COLUMN_ID} DESC",
        arrayOf(userId.toString())
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toProduct()) } }

    fun getProducts(): List<Product> = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null,
        "${AppDatabaseHelper.COLUMN_NAME} COLLATE NOCASE ASC"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toProduct()) } }

    fun findById(productId: Int): Product? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_PRODUCTS, null,
        "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(productId.toString()), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toProduct() else null }

    fun delete(productId: Int): Int = databaseHelper.writableDatabase.delete(
        AppDatabaseHelper.TABLE_PRODUCTS,
        "${AppDatabaseHelper.COLUMN_ID} = ?",
        arrayOf(productId.toString())
    )

    fun save(product: Product): Long {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_NAME, product.name)
            put(AppDatabaseHelper.COLUMN_DESCRIPTION, product.description)
            put(AppDatabaseHelper.COLUMN_DIMENSIONS, product.dimensions)
            put(AppDatabaseHelper.COLUMN_MATERIALS, product.materials)
            put(AppDatabaseHelper.COLUMN_CATEGORY, product.category)
            put(AppDatabaseHelper.COLUMN_PRICE, product.price)
            put(AppDatabaseHelper.COLUMN_AVAILABLE, if (product.available) 1 else 0)
            put(AppDatabaseHelper.COLUMN_IMAGE_URI, product.imageUri)
            put(AppDatabaseHelper.COLUMN_COLLECTION, product.collection)
        }
        val db = databaseHelper.writableDatabase
        return if (product.id == 0) db.insertOrThrow(AppDatabaseHelper.TABLE_PRODUCTS, null, values)
        else db.update(AppDatabaseHelper.TABLE_PRODUCTS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(product.id.toString())).toLong().takeIf { it > 0 } ?: -1L
    }

    private fun Cursor.toProduct() = Product(
        id = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
        name = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_NAME)),
        description = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DESCRIPTION)),
        dimensions = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DIMENSIONS)),
        materials = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_MATERIALS)),
        category = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_CATEGORY)),
        price = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PRICE)),
        available = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_AVAILABLE)) != 0,
        imageUri = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_IMAGE_URI)),
        collection = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_COLLECTION))
    )
}
