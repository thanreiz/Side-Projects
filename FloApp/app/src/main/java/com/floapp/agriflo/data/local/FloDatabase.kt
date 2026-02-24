package com.floapp.agriflo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.floapp.agriflo.data.local.dao.*
import com.floapp.agriflo.data.local.entity.*
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Main Room database for Flo (Agri-Flo).
 *
 * Encrypted with SQLCipher using a key managed by Android Keystore via [KeystoreHelper].
 * The database is a singleton; Hilt manages the lifecycle via [DatabaseModule].
 *
 * Schema version history:
 * v1 - Initial schema: crops, crop_logs, weather_cache, fertilizer_receipts, harvest_forecasts
 */
@Database(
    entities = [
        CropEntity::class,
        CropLogEntity::class,
        WeatherCacheEntity::class,
        FertilizerReceiptEntity::class,
        HarvestForecastEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FloDatabase : RoomDatabase() {

    abstract fun cropDao(): CropDao
    abstract fun cropLogDao(): CropLogDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun fertilizerReceiptDao(): FertilizerReceiptDao
    abstract fun harvestForecastDao(): HarvestForecastDao

    companion object {
        private const val DATABASE_NAME = "flo_database.db"

        /**
         * Builds the encrypted [FloDatabase] instance.
         * Must be called on a background thread; SQLCipher initialization is blocking.
         *
         * @param context Application context
         * @param passphrase Raw AES passphrase bytes from [KeystoreHelper.getOrCreateDatabasePassphrase]
         */
        fun build(context: Context, passphrase: ByteArray): FloDatabase {
            System.loadLibrary("sqlcipher")
            val factory = SupportOpenHelperFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                FloDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigrationOnDowngrade()
                // Add migrations here as the schema evolves:
                // .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
