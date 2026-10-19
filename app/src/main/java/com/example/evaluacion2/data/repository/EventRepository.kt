package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.FairEvent

class EventRepository(private val databaseHelper: AppDatabaseHelper) {
    fun getAll(): List<FairEvent> = databaseHelper.readableDatabase.query(
        AppDatabaseHelper.TABLE_EVENTS,
        null,
        null,
        null,
        null,
        null,
        "${AppDatabaseHelper.COLUMN_DATE} ASC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    FairEvent(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_NAME)),
                        place = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PLACE)),
                        address = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ADDRESS)),
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_LATITUDE)),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_LONGITUDE)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DATE))
                    )
                )
            }
        }
    }

    fun save(event: FairEvent): Long {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_NAME, event.name)
            put(AppDatabaseHelper.COLUMN_PLACE, event.place)
            put(AppDatabaseHelper.COLUMN_ADDRESS, event.address)
            put(AppDatabaseHelper.COLUMN_LATITUDE, event.latitude)
            put(AppDatabaseHelper.COLUMN_LONGITUDE, event.longitude)
            put(AppDatabaseHelper.COLUMN_DATE, event.date)
        }
        return databaseHelper.writableDatabase.insertOrThrow(AppDatabaseHelper.TABLE_EVENTS, null, values)
    }
}
