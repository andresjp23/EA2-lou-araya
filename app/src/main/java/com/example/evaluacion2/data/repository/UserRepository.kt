package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.User

class UserRepository(
    private val databaseHelper: AppDatabaseHelper
) {

    fun registerUser(
        user: User
    ): Result<Long> {

        return try {

            val database =
                databaseHelper.writableDatabase

            val values =
                ContentValues().apply {

                    put(
                        AppDatabaseHelper.COLUMN_NAME,
                        user.name
                    )

                    put(
                        AppDatabaseHelper.COLUMN_ALIAS,
                        user.alias
                    )

                    put(
                        AppDatabaseHelper.COLUMN_EMAIL,
                        user.email
                    )

                    put(
                        AppDatabaseHelper.COLUMN_PHONE,
                        user.phone
                    )

                    put(
                        AppDatabaseHelper.COLUMN_PASSWORD,
                        user.password
                    )

                    put(
                        AppDatabaseHelper.COLUMN_ROLE,
                        user.role
                    )
                }

            val result =
                database.insert(
                    AppDatabaseHelper.TABLE_USERS,
                    null,
                    values
                )

            if (result == -1L) {

                Result.failure(
                    Exception(
                        "No fue posible registrar el usuario"
                    )
                )

            } else {

                Result.success(result)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    fun emailExists(
        email: String
    ): Boolean {

        val database =
            databaseHelper.readableDatabase

        val cursor =
            database.query(
                AppDatabaseHelper.TABLE_USERS,
                arrayOf(
                    AppDatabaseHelper.COLUMN_ID
                ),
                "${AppDatabaseHelper.COLUMN_EMAIL} = ?",
                arrayOf(email),
                null,
                null,
                null
            )

        val exists =
            cursor.moveToFirst()

        cursor.close()

        return exists
    }


    fun aliasExists(
        alias: String
    ): Boolean {

        val database =
            databaseHelper.readableDatabase

        val cursor =
            database.query(
                AppDatabaseHelper.TABLE_USERS,
                arrayOf(
                    AppDatabaseHelper.COLUMN_ID
                ),
                "${AppDatabaseHelper.COLUMN_ALIAS} = ?",
                arrayOf(alias),
                null,
                null,
                null
            )

        val exists =
            cursor.moveToFirst()

        cursor.close()

        return exists
    }
}