package com.dfmiguel.gopack // Asegúrate que coincida con tu paquete

import android.app.Application

class GoPackApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val tripDao: TripDao by lazy { database.tripDao() }
    val packingItemDao: PackingItemDao by lazy { database.packingItemDao() } // <--- ¡AÑADE ESTA LÍNEA!

    override fun onCreate() {
        super.onCreate()
    }
}