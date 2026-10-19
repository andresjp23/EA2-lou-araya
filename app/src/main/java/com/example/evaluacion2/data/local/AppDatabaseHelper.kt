package com.example.evaluacion2.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.example.evaluacion2.domain.model.User

class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=ON")
        db.transaction {
            createUsersTable(db)
            createProductsTable(db)
            createFavoritesTable(db)
            createHomeFavoritesTable(db)
            createRequestsTable(db)
            createQrScansTable(db)
            createEventsTable(db)
            createCampaignsTable(db)
            createPreferencesTable(db)
            createNotificationInboxTable(db)
            seedLocalEventsIfEmpty(db)
            insertAdminIfMissing(db)
            seedCampaignsIfEmpty(db)
            seedProductsIfEmpty(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("PRAGMA foreign_keys=ON")
        db.transaction {
            if (oldVersion < 2) {
                createProductsTable(db)
                createFavoritesTable(db)
                createRequestsTable(db)
                createQrScansTable(db)
                createEventsTable(db)
                createCampaignsTable(db)
                createPreferencesTable(db)
            }
            if (oldVersion < 3) db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_AVATAR_URI TEXT")
            if (oldVersion < 4) createQrScansTable(db)
            if (oldVersion < 5) insertAdminIfMissing(db)
            if (oldVersion < 6) createHomeFavoritesTable(db)
            if (oldVersion < 7) {
                createNotificationInboxTable(db)
                seedLocalEventsIfEmpty(db)
            }
            if (oldVersion < 8) {
                seedCampaignsIfEmpty(db)
                seedProductsIfEmpty(db)
            }
        }
    }

    private fun insertAdminIfMissing(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, "Administrador")
            put(COLUMN_ALIAS, "admin")
            put(COLUMN_EMAIL, "admin@louaraya.cl")
            put(COLUMN_PHONE, "0000000000")
            put(COLUMN_PASSWORD, "Admin123")
            put(COLUMN_ROLE, User.ROLE_ADMIN)
        }
        val adminId = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        val resolvedAdminId = if (adminId != -1L) adminId else {
            db.rawQuery(
                "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? COLLATE NOCASE",
                arrayOf("admin@louaraya.cl")
            ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else return }
        }
        db.insertWithOnConflict(
            TABLE_PREFERENCES,
            null,
            ContentValues().apply {
                put(COLUMN_USER_ID, resolvedAdminId)
                put(COLUMN_NOTIFICATIONS_ENABLED, 1)
            },
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_ALIAS TEXT NOT NULL UNIQUE,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL,
                $COLUMN_AVATAR_URI TEXT
            )
        """.trimIndent())
    }

    private fun createProductsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL DEFAULT '',
                $COLUMN_DIMENSIONS TEXT NOT NULL DEFAULT '',
                $COLUMN_MATERIALS TEXT NOT NULL DEFAULT '',
                $COLUMN_CATEGORY TEXT NOT NULL DEFAULT '',
                $COLUMN_PRICE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_AVAILABLE INTEGER NOT NULL DEFAULT 1,
                $COLUMN_IMAGE_URI TEXT,
                $COLUMN_COLLECTION TEXT
            )
        """.trimIndent())
    }

    private fun createFavoritesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_PRODUCT_ID INTEGER NOT NULL,
                UNIQUE($COLUMN_USER_ID, $COLUMN_PRODUCT_ID),
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_user_id ON $TABLE_FAVORITES($COLUMN_USER_ID)")
    }

    private fun createHomeFavoritesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_HOME_FAVORITES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_PRODUCT_ID INTEGER NOT NULL,
                UNIQUE($COLUMN_USER_ID, $COLUMN_PRODUCT_ID),
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    private fun createRequestsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_REQUESTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_PRODUCT_ID INTEGER,
                $COLUMN_EVENT_TYPE TEXT NOT NULL DEFAULT '',
                $COLUMN_DIMENSIONS TEXT NOT NULL DEFAULT '',
                $COLUMN_MATERIAL TEXT NOT NULL DEFAULT '',
                $COLUMN_COLORS TEXT NOT NULL DEFAULT '',
                $COLUMN_REQUIRED_DATE TEXT NOT NULL DEFAULT '',
                $COLUMN_OBSERVATIONS TEXT NOT NULL DEFAULT '',
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'PENDIENTE',
                $COLUMN_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_ID) ON DELETE SET NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_requests_user_id ON $TABLE_REQUESTS($COLUMN_USER_ID)")
    }

    private fun createQrScansTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_QR_SCANS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_PRODUCT_ID INTEGER,
                $COLUMN_SCANNED_AT TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE SET NULL,
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_ID) ON DELETE SET NULL
            )
        """.trimIndent())
    }

    private fun createEventsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_EVENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PLACE TEXT NOT NULL DEFAULT '',
                $COLUMN_ADDRESS TEXT NOT NULL DEFAULT '',
                $COLUMN_LATITUDE REAL NOT NULL DEFAULT 0,
                $COLUMN_LONGITUDE REAL NOT NULL DEFAULT 0,
                $COLUMN_DATE TEXT NOT NULL
            )
        """.trimIndent())
    }

    private fun createCampaignsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CAMPAIGNS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL DEFAULT '',
                $COLUMN_START_DATE TEXT NOT NULL,
                $COLUMN_END_DATE TEXT NOT NULL,
                $COLUMN_ACTIVE INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }

    private fun createNotificationInboxTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NOTIFICATIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL DEFAULT '',
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_READ INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_user_id ON $TABLE_NOTIFICATIONS($COLUMN_USER_ID)")
    }

    private fun seedProductsIfEmpty(db: SQLiteDatabase) {
        val hasProducts = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PRODUCTS", null).use { it.moveToFirst() && it.getInt(0) > 0 }
        if (hasProducts) return
        val products = listOf(
            arrayOf("Arco de ceremonia", "Estructura artesanal para ceremonias y celebraciones.", "220 x 180 cm", "Algodón natural", "Matrimonios", "120000", "Bodas de Ensueño"),
            arrayOf("Tapiz mural", "Pieza tejida a mano inspirada en la naturaleza andina.", "90 x 70 cm", "Algodón natural", "Interiores", "65000", "Hogares con Alma"),
            arrayOf("Colgante de plantas", "Macetero colgante de fibras naturales.", "45 cm", "Yute y algodón", "Interiores", "28000", "Hogares con Alma"),
            arrayOf("Cortina decorativa", "Cortina artesanal para ambientes cálidos.", "200 x 180 cm", "Algodón", "Festivales", "85000", "Festivales Libres"),
            arrayOf("Atrapasueños", "Decoración artesanal liviana.", "40 cm", "Algodón y madera", "Festivales", "32000", "Viajes que Inspiran")
        )
        products.forEach { sample ->
            db.insert(TABLE_PRODUCTS, null, ContentValues().apply {
                put(COLUMN_NAME, sample[0]); put(COLUMN_DESCRIPTION, sample[1]); put(COLUMN_DIMENSIONS, sample[2]); put(COLUMN_MATERIALS, sample[3])
                put(COLUMN_CATEGORY, sample[4]); put(COLUMN_PRICE, sample[5].toInt()); put(COLUMN_AVAILABLE, 1); put(COLUMN_COLLECTION, sample[6])
            })
        }
    }

    private fun seedCampaignsIfEmpty(db: SQLiteDatabase) {
        val hasCampaigns = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CAMPAIGNS", null).use { it.moveToFirst() && it.getInt(0) > 0 }
        if (hasCampaigns) return
        listOf(
            arrayOf("Bodas de Ensueño", "Diseños para un día inolvidable", "2025-01-01", "2025-12-31", "1"),
            arrayOf("Festivales Libres", "Arte textil para grandes encuentros", "2025-01-01", "2025-12-31", "1"),
            arrayOf("Hogares con Alma", "Decora tus espacios", "2026-01-01", "2026-12-31", "1"),
            arrayOf("Viajes que Inspiran", "Piezas livianas y únicas", "2024-01-01", "2024-12-31", "0")
        ).forEach { sample -> db.insert(TABLE_CAMPAIGNS, null, ContentValues().apply {
            put(COLUMN_TITLE, sample[0]); put(COLUMN_DESCRIPTION, sample[1]); put(COLUMN_START_DATE, sample[2]); put(COLUMN_END_DATE, sample[3]); put(COLUMN_ACTIVE, sample[4].toInt())
        }) }
    }

    private fun seedLocalEventsIfEmpty(db: SQLiteDatabase) {
        val hasEvents = db.rawQuery("SELECT COUNT(*) FROM $TABLE_EVENTS", null).use { it.moveToFirst() && it.getInt(0) > 0 }
        if (hasEvents) return
        val samples = listOf(
            arrayOf("Feria Pueblo Artesanal", "Feria", "Parque Bicentenario, Vitacura", "-33.4006", "-70.5996", "12–14 Abr 2025"),
            arrayOf("Showroom Las Condes", "Showroom", "Av. Apoquindo, Las Condes", "-33.4140", "-70.5860", "26 Abr 2025"),
            arrayOf("Encuentro de Diseño Ñuñoa", "Eventos", "Plaza Ñuñoa", "-33.4540", "-70.5970", "10 May 2025")
        )
        samples.forEach { sample ->
            db.insert(TABLE_EVENTS, null, ContentValues().apply {
                put(COLUMN_NAME, sample[0]); put(COLUMN_PLACE, sample[1]); put(COLUMN_ADDRESS, sample[2])
                put(COLUMN_LATITUDE, sample[3].toDouble()); put(COLUMN_LONGITUDE, sample[4].toDouble()); put(COLUMN_DATE, sample[5])
            })
        }
    }

    private fun createPreferencesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PREFERENCES (
                $COLUMN_USER_ID INTEGER PRIMARY KEY,
                $COLUMN_NOTIFICATIONS_ENABLED INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    companion object {
        private const val DATABASE_NAME = "lou_araya.db"
        private const val DATABASE_VERSION = 8

        const val TABLE_USERS = "users"
        const val TABLE_PRODUCTS = "products"
        const val TABLE_FAVORITES = "favorites"
        const val TABLE_HOME_FAVORITES = "home_favorites"
        const val TABLE_REQUESTS = "requests"
        const val TABLE_QR_SCANS = "qr_scans"
        const val TABLE_EVENTS = "events"
        const val TABLE_CAMPAIGNS = "campaigns"
        const val TABLE_PREFERENCES = "preferences"
        const val TABLE_NOTIFICATIONS = "notifications"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ALIAS = "alias"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_ROLE = "role"
        const val COLUMN_AVATAR_URI = "avatar_uri"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_PRODUCT_ID = "product_id"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DIMENSIONS = "dimensions"
        const val COLUMN_MATERIALS = "materials"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_PRICE = "price"
        const val COLUMN_AVAILABLE = "available"
        const val COLUMN_IMAGE_URI = "image_uri"
        const val COLUMN_COLLECTION = "collection"
        const val COLUMN_EVENT_TYPE = "event_type"
        const val COLUMN_MATERIAL = "material"
        const val COLUMN_COLORS = "colors"
        const val COLUMN_REQUIRED_DATE = "required_date"
        const val COLUMN_OBSERVATIONS = "observations"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_SCANNED_AT = "scanned_at"
        const val COLUMN_PLACE = "place"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_DATE = "date"
        const val COLUMN_TITLE = "title"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val COLUMN_READ = "read"
    }
}
