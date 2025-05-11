package com.dfmiguel.packpal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var fabAddTrip: FloatingActionButton
    private lateinit var tripAdapter: TripAdapter // Declara el adaptador

    // Obtenemos el DAO de nuestra clase Application
    private val tripDao by lazy { (application as PackPalApplication).tripDao }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewTrips = findViewById(R.id.recyclerViewTrips)
        fabAddTrip = findViewById(R.id.fabAddTrip)

        setupRecyclerView()
        observeTrips() // Nueva función para observar los viajes desde la BD

        fabAddTrip.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter { trip ->
            Toast.makeText(this, "Viaje pulsado: ${trip.name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TripDetailActivity::class.java).apply {
                putExtra(TripDetailActivity.EXTRA_TRIP_ID, trip.id)
            }
            startActivity(intent)
        }
        recyclerViewTrips.adapter = tripAdapter
        recyclerViewTrips.layoutManager = LinearLayoutManager(this)
    }

    private fun observeTrips() {
        // Usamos lifecycleScope para que la coroutine se cancele automáticamente
        // cuando la Activity se destruya, evitando fugas de memoria.
        lifecycleScope.launch {
            tripDao.getAllTrips().collect { tripsFromDb ->
                Log.d("MainActivity", "Viajes desde BD: ${tripsFromDb.size}") // Log para depurar
                tripAdapter.submitList(tripsFromDb)  // Notificamos al adaptador que los datos cambiaron
                // Más adelante, usaremos DiffUtil para actualizaciones más eficientes
            }
        }
    }
}