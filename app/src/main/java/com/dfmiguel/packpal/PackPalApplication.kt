package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.app.Application

class PackPalApplication : Application() {
    // Usamos 'lazy' para que la base de datos y el DAO solo se creen cuando se necesiten por primera vez.
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val tripDao: TripDao by lazy { database.tripDao() }

    override fun onCreate() {
        super.onCreate()
        // Aquí podrías inicializar otras cosas globales si fuera necesario en el futuro
        // como librerías de logging, analytics, etc.
        // Por ahora, la inicialización 'lazy' de la base de datos es suficiente.
    }
}