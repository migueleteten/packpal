package com.dfmiguel.gopack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips_table") // Le damos un nombre a nuestra tabla
data class Trip(
    @PrimaryKey(autoGenerate = true) // El ID será la clave primaria y se autogenerará
    var id: Long = 0, // Cambiamos 'val' por 'var' y damos valor inicial para que Room pueda instanciarlo

    val name: String, // Ejemplo: "Vacaciones en Roma"
    val destination: String, // Ejemplo: "Roma, Italia"
    val startDate: String? = null, // Fecha de inicio (opcional)
    val endDate: String? = null, // Fecha de fin (opcional)
    // Podríamos añadir más campos luego: tipo de viaje, nº personas, etc.
    // Por ejemplo:
    // val tripType: String? = null, // Ocio, Trabajo, etc.
    // val numberOfPeople: Int = 1
)