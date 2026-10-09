package com.tinaut1986.pricesmart.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tinaut1986.pricesmart.model.ProductTemplate

@Database(entities = [ProductTemplate::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Some version 1 databases already contain compareQuantity, so each column is added only if missing
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn("product_templates", "compareQuantity")) {
                    db.execSQL("ALTER TABLE product_templates ADD COLUMN compareQuantity INTEGER NOT NULL DEFAULT 1")
                }
                if (!db.hasColumn("product_templates", "rating")) {
                    db.execSQL("ALTER TABLE product_templates ADD COLUMN rating REAL NOT NULL DEFAULT 0")
                }
            }
        }

        private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean {
            query("PRAGMA table_info($table)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == column) return true
                }
            }
            return false
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pricesmart_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
