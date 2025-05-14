package com.dfmiguel.gopack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarMain: MaterialToolbar // NUEVA VARIABLE
    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var fabAddTrip: FloatingActionButton
    private lateinit var tripAdapter: TripAdapter // Declara el adaptador

    private val tripDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Algo pudo haber cambiado en TripDetailActivity (ítems marcados/desmarcados).
            // Forzamos al adapter a re-evaluar la lista actual para que se
            // disparen de nuevo los cálculos de conteo en los ViewHolders visibles.
            if (::tripAdapter.isInitialized) { // Solo si el adapter ya fue creado
                val currentList = tripAdapter.currentList
                tripAdapter.submitList(null) // Truco para limpiar y forzar diff
                tripAdapter.submitList(currentList.toList()) // Pasamos una copia de la lista para asegurar que DiffUtil detecta un cambio
                Log.d("MainActivity", "Refrescando lista de viajes tras volver de detalles.")
            }
        }
    }

    // Obtenemos el DAO de nuestra clase Application
    private val tripDao by lazy { (application as GoPackApplication).tripDao }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarMain = findViewById(R.id.toolbarMain) // OBTENER REFERENCIA
        setSupportActionBar(toolbarMain) // ESTABLECER COMO ACTIONBAR

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
        tripAdapter = TripAdapter(tripDao, lifecycleScope) { trip -> // Pasamos tripDao y lifecycleScope
            val intent = Intent(this, TripDetailActivity::class.java).apply {
                putExtra(TripDetailActivity.EXTRA_TRIP_ID, trip.id)
            }
            tripDetailLauncher.launch(intent) // USAMOS EL LAUNCHER AQUÍ para abrir detalles
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