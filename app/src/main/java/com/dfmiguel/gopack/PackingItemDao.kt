package com.dfmiguel.gopack // Asegúrate que coincida con tu paquete

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItem)

    @Update
    suspend fun updatePackingItem(item: PackingItem)

    @Delete
    suspend fun deletePackingItem(item: PackingItem)

    // Obtener todos los ítems para un ID de viaje específico, ordenados por nombre
    // Usamos :tripId para pasar el parámetro a la consulta SQL
    @Query("SELECT * FROM packing_items_table WHERE tripIdOwner = :tripId ORDER BY category ASC, name ASC")
    fun getItemsForTrip(tripId: Long): Flow<List<PackingItem>>

    // Opcional: Una forma de obtener un ítem específico por su ID (útil si queremos editar un ítem individual)
    @Query("SELECT * FROM packing_items_table WHERE id = :itemId")
    fun getPackingItemById(itemId: Long): Flow<PackingItem?>

    // Opcional: Borrar todos los ítems de un viaje específico (aunque CASCADE lo haría al borrar el viaje)
    // Podría ser útil si queremos una función "limpiar lista de equipaje"
    // @Query("DELETE FROM packing_items_table WHERE tripIdOwner = :tripId")
    // suspend fun deleteAllItemsForTrip(tripId: Long)
}