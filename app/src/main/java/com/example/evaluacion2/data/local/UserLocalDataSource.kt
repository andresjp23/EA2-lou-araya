package com.example.evaluacion2.data.local

import android.content.ContentValues
import android.database.Cursor
import com.example.evaluacion2.domain.model.User

class UserLocalDataSource(private val databaseHelper: AppDatabaseHelper) : UserCache {

    override fun getById(userId: Int): User? = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_USERS, null, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(userId.toString()), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.toUser() else null }

    /**
     * Guarda el perfil con el mismo id que tiene en el microservicio, para que favoritos,
     * preferencias y notificaciones locales queden asociados al usuario correcto.
     * Nunca se guarda la contraseña.
     */
    override fun upsert(user: User) {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_ID, user.id)
            put(AppDatabaseHelper.COLUMN_NAME, user.name)
            put(AppDatabaseHelper.COLUMN_ALIAS, user.alias)
            put(AppDatabaseHelper.COLUMN_EMAIL, user.email)
            put(AppDatabaseHelper.COLUMN_PHONE, user.phone)
            put(AppDatabaseHelper.COLUMN_PASSWORD, "")
            put(AppDatabaseHelper.COLUMN_ROLE, user.role)
            put(AppDatabaseHelper.COLUMN_AVATAR_URI, user.avatarUri)
        }
        db.beginTransaction()
        try {
            // Si quedó un registro antiguo con el mismo correo o alias pero otro id, se reemplaza.
            db.delete(
                AppDatabaseHelper.TABLE_USERS,
                "${AppDatabaseHelper.COLUMN_ID} != ? AND (${AppDatabaseHelper.COLUMN_EMAIL} = ? COLLATE NOCASE OR ${AppDatabaseHelper.COLUMN_ALIAS} = ? COLLATE NOCASE)",
                arrayOf(user.id.toString(), user.email, user.alias)
            )
            val updated = db.update(AppDatabaseHelper.TABLE_USERS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(user.id.toString()))
            if (updated == 0) db.insertOrThrow(AppDatabaseHelper.TABLE_USERS, null, values)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun Cursor.toUser() = User(
        id = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
        name = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_NAME)),
        alias = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ALIAS)),
        email = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_EMAIL)),
        phone = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PHONE)),
        role = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ROLE)),
        avatarUri = getString(getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_AVATAR_URI))
    )
}
