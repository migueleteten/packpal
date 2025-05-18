package com.dfmiguel.gopack

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button
import android.widget.LinearLayout
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarMain: MaterialToolbar // NUEVA VARIABLE
    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var fabAddTrip: FloatingActionButton
    private lateinit var tripAdapter: TripAdapter // Declara el adaptador

    private lateinit var layoutEmptyStateMain: LinearLayout // NUEVA VARIABLE
    private lateinit var buttonCreateFirstTrip: Button    // NUEVA VARIABLE

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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarMain = findViewById(R.id.toolbarMain) // OBTENER REFERENCIA
        setSupportActionBar(toolbarMain) // ESTABLECER COMO ACTIONBAR

        recyclerViewTrips = findViewById(R.id.recyclerViewTrips)
        fabAddTrip = findViewById(R.id.fabAddTrip)
        layoutEmptyStateMain = findViewById(R.id.layoutEmptyStateMain)
        buttonCreateFirstTrip = findViewById(R.id.buttonCreateFirstTrip)

        setupRecyclerView()
        observeTrips() // Nueva función para observar los viajes desde la BD

        val addTripClickListener = View.OnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            // Considera usar un ActivityResultLauncher aquí también si AddTripActivity
            // necesitara devolver un resultado que MainActivity deba procesar directamente
            // al crear un viaje (aunque el Flow ya actualiza la lista).
            startActivity(intent)
        }

        fabAddTrip.setOnClickListener(addTripClickListener)
        buttonCreateFirstTrip.setOnClickListener(addTripClickListener)
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
        lifecycleScope.launch {
            // tripDao.getAllTrips().collectLatest { tripsFromDb -> // Usar collectLatest es buena práctica aquí
            tripDao.getAllTrips().collect { tripsFromDb -> // O collect si no hay problemas de concurrencia con múltiples emisiones rápidas
                Log.d("MainActivity", "Viajes desde BD: ${tripsFromDb.size}")
                if (tripsFromDb.isEmpty()) {
                    recyclerViewTrips.visibility = View.GONE
                    layoutEmptyStateMain.visibility = View.VISIBLE
                } else {
                    recyclerViewTrips.visibility = View.VISIBLE
                    layoutEmptyStateMain.visibility = View.GONE
                }
                tripAdapter.submitList(tripsFromDb)
            }
        }
    }
}