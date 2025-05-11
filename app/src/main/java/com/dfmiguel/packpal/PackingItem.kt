package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "packing_items_table",
    foreignKeys = [ForeignKey(
        entity = Trip::class, // La entidad padre
        parentColumns = ["id"], // La clave primaria de la entidad padre (Trip)
        childColumns = ["tripIdOwner"], // La clave foránea en esta entidad (PackingItem)
        onDelete = ForeignKey.CASCADE // ¡IMPORTANTE! Si se borra un Viaje, se borran sus ítems asociados.
    )],
    indices = [Index(value = ["tripIdOwner"])] // Crear un índice en tripIdOwner para búsquedas más rápidas
)
data class PackingItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var tripIdOwner: Long, // El ID del Viaje al que pertenece este ítem

    var name: String, // Ejemplo: "Camiseta Azul"
    var category: String = "General", // Ejemplo: "Ropa", "Aseo", "Electrónica"
    var quantity: Int = 1,
    var isChecked: Boolean = false // Para marcar si ya está en la maleta
)