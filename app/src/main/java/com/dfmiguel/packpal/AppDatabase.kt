package com.dfmiguel.packpal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Trip::class, PackingItem::class], // AÑADIMOS PackingItem::class AQUÍ
    version = 2, // INCREMENTAMOS LA VERSIÓN DE 1 A 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun packingItemDao(): PackingItemDao // AÑADIMOS ESTA FUNCIÓN ABSTRACTA

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "packpal_database"
                    )
                        // ¡RECUERDA! Esto borrará los datos si hay un cambio de versión sin migración explícita.
                        // Perfecto para desarrollo inicial.
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}