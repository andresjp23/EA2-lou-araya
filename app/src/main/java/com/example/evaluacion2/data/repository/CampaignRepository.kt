package com.example.evaluacion2.data.repository

import android.content.ContentValues
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.domain.model.Campaign

class CampaignRepository(private val databaseHelper: AppDatabaseHelper) {
    fun getAll(activeOnly: Boolean = false): List<Campaign> {
        val selection = if (activeOnly) "${AppDatabaseHelper.COLUMN_ACTIVE} = ?" else null
        val args = if (activeOnly) arrayOf("1") else null
        return databaseHelper.readableDatabase.query(
            AppDatabaseHelper.TABLE_CAMPAIGNS,
            null,
            selection,
            args,
            null,
            null,
            "${AppDatabaseHelper.COLUMN_START_DATE} DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Campaign(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ID)),
                            title = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_TITLE)),
                            description = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_DESCRIPTION)),
                            startDate = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_START_DATE)),
                            endDate = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_END_DATE)),
                            active = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_ACTIVE)) != 0
                        )
                    )
                }
            }
        }
    }

    fun save(campaign: Campaign): Long {
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COLUMN_TITLE, campaign.title)
            put(AppDatabaseHelper.COLUMN_DESCRIPTION, campaign.description)
            put(AppDatabaseHelper.COLUMN_START_DATE, campaign.startDate)
            put(AppDatabaseHelper.COLUMN_END_DATE, campaign.endDate)
            put(AppDatabaseHelper.COLUMN_ACTIVE, if (campaign.active) 1 else 0)
        }
        val db = databaseHelper.writableDatabase
        return if (campaign.id == 0) {
            db.insertOrThrow(AppDatabaseHelper.TABLE_CAMPAIGNS, null, values)
        } else {
            db.update(AppDatabaseHelper.TABLE_CAMPAIGNS, values, "${AppDatabaseHelper.COLUMN_ID} = ?", arrayOf(campaign.id.toString())).toLong()
                .takeIf { it > 0 } ?: -1L
        }
    }
}
