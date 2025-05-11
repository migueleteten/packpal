package com.dfmiguel.packpal
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Importante para datos reactivos

@Dao // Le dice a Room que esto es un Data Access Object
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Si insertamos un viaje con un ID que ya existe, lo reemplazará
    suspend fun insertTrip(trip: Trip) // 'suspend' para usarlo con Coroutines (asíncrono)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips_table ORDER BY name ASC") // Consulta SQL para obtener todos los viajes, ordenados por nombre
    fun getAllTrips(): Flow<List<Trip>> // Devuelve un Flow, que permite observar cambios en los datos de forma reactiva

    @Query("SELECT * FROM trips_table WHERE id = :tripId") // Consulta para obtener un viaje por su ID
    fun getTripById(tripId: Long): Flow<Trip?> // Puede devolver un viaje o null si no se encuentra, envuelto en un Flow

    @Query("DELETE FROM trips_table") // Consulta para borrar todos los viajes (útil para pruebas o reset)
    suspend fun deleteAllTrips()
}