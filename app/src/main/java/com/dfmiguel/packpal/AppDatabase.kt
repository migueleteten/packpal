package com.dfmiguel.packpal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Trip::class], version = 1, exportSchema = false)
// - entities: Array de todas las clases Entidad que pertenecen a esta base de datos.
// - version: Número de versión de la base de datos. Importante para las migraciones.
// - exportSchema: Por ahora false para evitar un warning y la necesidad de configurar
//   un directorio de esquemas. Para producción y migraciones complejas, se pondría a true.
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao // Room implementará esto por nosotros

    companion object {
        // La anotación @Volatile asegura que el valor de INSTANCE sea siempre actualizado
        // y visible para todos los hilos de ejecución.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // synchronized asegura que solo un hilo pueda ejecutar este bloque a la vez,
            // previniendo la creación de múltiples instancias de la base de datos
            // si varios hilos intentan acceder a ella simultáneamente.
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, // Contexto de la aplicación
                        AppDatabase::class.java,    // Nuestra clase de base de datos
                        "packpal_database"      // Nombre del archivo de la base de datos
                    )
                        .fallbackToDestructiveMigration() // SOLO PARA DESARROLLO INICIAL: Si cambiamos el esquema, borrará y recreará la BD.
                        // Para producción, necesitaríamos estrategias de migración adecuadas.
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}