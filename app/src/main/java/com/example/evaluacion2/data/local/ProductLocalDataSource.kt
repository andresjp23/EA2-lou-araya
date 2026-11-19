package com.example.evaluacion2.data.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.evaluacion2.domain.model.Product

class ProductLocalDataSource(private val databaseHelper: AppDatabaseHelper) : ProductCache {

    override fun getAll(): List<Product> = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null,
        "${AppDatabaseHelper.COLUMN_NAME} COLLATE NOCASE ASC"
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toProduct()) } }

    override fun findById(productId: Int): Product? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_PRODUCTS, null,
        "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(productId.toString()), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toProduct() else null }

    /** Sincroniza la caché con la lista del servidor: actualiza/inserta y elimina lo que ya no existe. */
    override fun upsertAll(products: List<Product>) {
        val db = databaseHelper.writableDatabase
        db.beginTransaction()
        try {
            products.forEach { upsert(db, it) }
            val ids = products.map { it.id }
            if (ids.isNotEmpty()) {
                db.delete(
                    AppDatabaseHelper.TABLE_PRODUCTS,
                    "${AppDatabaseHelper.COLUMN_ID} NOT IN (${ids.joinToString { "?" }})",
                    ids.map(Int::toString).toTypedArray()
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun upsert(product: Product) = upsert(databaseHelper.writableDatabase, product)

    override fun delete(productId: Int) {
        databaseHelper.writableDatabase.delete(AppDatabaseHelper.TABLE_PRODUCTS, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(productId.toString()))
    }

    private fun upsert(db: SQLiteDatabase, product: Product) {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_ID, product.id)
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
        val updated = db.update(AppDatabaseHelper.TABLE_PRODUCTS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(product.id.toString()))
        if (updated == 0) db.insertWithOnConflict(AppDatabaseHelper.TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
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
