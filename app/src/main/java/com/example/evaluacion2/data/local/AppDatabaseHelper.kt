package com.example.evaluacion2.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_ALIAS TEXT NOT NULL UNIQUE,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        db.execSQL(
            "DROP TABLE IF EXISTS $TABLE_USERS"
        )

        onCreate(db)
    }

    companion object {

        private const val DATABASE_NAME =
            "lou_araya.db"

        private const val DATABASE_VERSION =
            1

        const val TABLE_USERS =
            "users"

        const val COLUMN_ID =
            "id"

        const val COLUMN_NAME =
            "name"

        const val COLUMN_ALIAS =
            "alias"

        const val COLUMN_EMAIL =
            "email"

        const val COLUMN_PHONE =
            "phone"

        const val COLUMN_PASSWORD =
            "password"

        const val COLUMN_ROLE =
            "role"
    }
}