package com.sarrawi.mytranslate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sarrawi.mytranslate.dao.HistoryDao
import com.sarrawi.mytranslate.db.Dao.FavDao
import com.sarrawi.mytranslate.model.FavModel
import com.sarrawi.mytranslate.model.History

@Database(entities = [History::class, FavModel::class], version = 2)  // زيادة رقم الإصدار
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favDao(): FavDao  // إضافة الـ DAO الخاص بالـ fav

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration من الإصدار 1 إلى الإصدار 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // إضافة الجدول الجديد fav
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `fav_table` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `word` TEXT NOT NULL, 
                `meaning` TEXT NOT NULL, 
                `sourceLang` TEXT NOT NULL, 
                `targetLang` TEXT NOT NULL,
                `is_fav` INTEGER NOT NULL DEFAULT 1 
            )
        """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "translate_database"
                )
                    .addMigrations(MIGRATION_1_2)  // إضافة الـ Migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
