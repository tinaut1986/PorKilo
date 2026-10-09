package com.tinaut1986.porkilo.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.tinaut1986.porkilo.model.ProductTemplate

@Database(
    entities = [ProductTemplate::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = AppDatabase.DropTemplateCompareQuantity::class)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao

    // How many items to buy depends on the offer of the day, so it is no longer stored per format
    @DeleteColumn(tableName = "product_templates", columnName = "compareQuantity")
    class DropTemplateCompareQuantity : AutoMigrationSpec

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "porkilo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
