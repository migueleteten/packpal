package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest // Usaremos collectLatest para el detalle de un solo viaje
import kotlinx.coroutines.launch
import android.content.DialogInterface // Para el diálogo
import androidx.appcompat.app.AlertDialog // Para el diálogo

class TripDetailActivity : AppCompatActivity() {

    private lateinit var toolbarTripDetail: MaterialToolbar
    private lateinit var textViewDetailTripName: TextView
    private lateinit var textViewDetailTripDestination: TextView
    private lateinit var textViewDetailStartDate: TextView
    private lateinit var textViewDetailEndDate: TextView
    private lateinit var recyclerViewPackingItems: RecyclerView // Para la futura lista de ítems
    private lateinit var fabAddPackingItem: FloatingActionButton // Para añadir ítems

    private val tripDao by lazy { (application as PackPalApplication).tripDao }
    private var currentTripId: Long = -1L // Para guardar el ID del viaje actual

    companion object {
        const val EXTRA_TRIP_ID = "extra_trip_id" // Clave para pasar el ID del viaje
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        // Configurar la Toolbar
        toolbarTripDetail = findViewById(R.id.toolbarTripDetail)
        setSupportActionBar(toolbarTripDetail)

        // Habilitar el botón de "Atrás" en la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // supportActionBar?.title = getString(R.string.title_activity_trip_detail)

        // textViewDetailTripName = findViewById(R.id.textViewDetailTripName)
        textViewDetailTripDestination = findViewById(R.id.textViewDetailTripDestination)
        textViewDetailStartDate = findViewById(R.id.textViewDetailStartDate)
        textViewDetailEndDate = findViewById(R.id.textViewDetailEndDate)
        recyclerViewPackingItems = findViewById(R.id.recyclerViewPackingItems)
        fabAddPackingItem = findViewById(R.id.fabAddPackingItem)

        currentTripId = intent.getLongExtra(EXTRA_TRIP_ID, -1L)

        if (currentTripId == -1L) {
            Toast.makeText(this, "Error: ID de viaje no encontrado", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay ID
            return
        }

        observeTripDetails()

        fabAddPackingItem.setOnClickListener {
            Toast.makeText(this, "Añadir ítem para Viaje ID: $currentTripId", Toast.LENGTH_SHORT).show()
            // Aquí luego abriremos la pantalla/dialog para añadir un ítem de equipaje
        }
    }

    private fun observeTripDetails() {
        lifecycleScope.launch {
            tripDao.getTripById(currentTripId).collectLatest { trip ->
                if (trip != null) {
                    // Establecer el título de la Toolbar con el nombre del viaje
                    supportActionBar?.title = trip.name
                    // textViewDetailTripName.text = trip.name
                    textViewDetailTripDestination.text = trip.destination
                    textViewDetailStartDate.text = trip.startDate?.let { "Inicio: $it" } ?: "Fecha inicio no definida"
                    textViewDetailEndDate.text = trip.endDate?.let { "Fin: $it" } ?: "Fecha fin no definida"
                    // Aquí luego configuraríamos el adaptador para recyclerViewPackingItems con los ítems de este viaje
                } else {
                    Log.e("TripDetailActivity", "Viaje con ID $currentTripId no encontrado.")
                    Toast.makeText(this@TripDetailActivity, "Viaje no encontrado", Toast.LENGTH_LONG).show()
                    // Podrías cerrar la actividad o mostrar un estado de error
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_trip_detail, menu)
        return true // Devuelve true para que el menú se muestre
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_trip -> {
                //Toast.makeText(this, "Editar Viaje ID: $currentTripId", Toast.LENGTH_SHORT).show() // Comentamos o borramos el Toast

                // Lanzar AddTripActivity en modo edición
                if (currentTripId != -1L) { // Asegurarnos de que tenemos un ID válido
                    val intent = Intent(this, AddTripActivity::class.java).apply {
                        putExtra(AddTripActivity.EXTRA_EDIT_TRIP_ID, currentTripId)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No se puede editar: ID de viaje inválido", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_delete_trip -> { // NUEVO CASO PARA BORRAR
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        // Asegurarnos de que tenemos un viaje cargado para mostrar su nombre y para borrarlo
        // La variable 'existingTrip' la deberíamos tener de cuando cargamos los datos para mostrar.
        // Si no la tienes como variable de clase, necesitarías cargarla o pasar el nombre.
        // Asumiré que tienes 'existingTrip: Trip?' como variable de clase que se setea en 'observeTripDetails'
        // Si 'existingTrip' no es una variable de clase, necesitarás recuperarlo o pasar el nombre.
        // Por simplicidad, si existingTrip no está como variable de clase aún, carguémoslo para asegurar:

        // Modificación: Asegurémonos que existingTrip (variable de clase) está disponible o la cargamos
        // Si ya tienes existingTrip como variable de clase que se actualiza en observeTripDetails, esto es redundante.
        // Si no, necesitarías una forma de acceder al nombre del viaje actual.
        // Vamos a asumir que 'existingTrip' es una variable de clase que se actualiza en 'observeTripDetails'.
        // Si 'existingTrip' no está definido como variable de clase, deberás hacerlo:
        // private var existingTrip: Trip? = null (y la actualizas en observeTripDetails)

        val tripNameToDelete = supportActionBar?.title.toString() // Usamos el título de la ActionBar que ya tiene el nombre

        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trip_confirmation_title)
            .setMessage(getString(R.string.delete_trip_confirmation_message, tripNameToDelete))
            .setIcon(R.drawable.baseline_delete_outline_24) // O el icono de "warning" @android:drawable/ic_dialog_alert
            .setPositiveButton(R.string.dialog_delete) { dialog, _ ->
                deleteCurrentTrip()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteCurrentTrip() {
        if (currentTripId != -1L) {
            lifecycleScope.launch {
                // Para borrar con tripDao.deleteTrip(trip), necesitamos el objeto Trip.
                // Si no lo tenemos a mano (ej. no es variable de clase 'existingTrip'),
                // podríamos crear un método en el DAO para borrar por ID o cargarlo primero.
                // Asumamos que SÍ tenemos existingTrip como variable de clase. Si no, hay que ajustar.

                // Manera A: Si tenemos el objeto 'existingTrip' como variable de clase y actualizado:
                // tripDao.getTripById(currentTripId).collectLatest { tripToDelete ->
                //    if (tripToDelete != null) {
                //        tripDao.deleteTrip(tripToDelete)
                //        Toast.makeText(this@TripDetailActivity, "Viaje borrado", Toast.LENGTH_SHORT).show()
                //        finish() // Volver a MainActivity
                //    }
                // }
                // Esta forma de arriba es más segura porque siempre borra el objeto más actual.
                // Pero requiere que 'existingTrip' sea una variable de clase bien gestionada o hacer la consulta.

                // Manera B: Crear un Trip solo con el ID (si el DAO lo soportara para delete, pero el nuestro espera un Trip)
                // O, mejor, modificamos el DAO para tener un deleteById o borramos el objeto que ya teníamos.
                // Para usar el tripDao.deleteTrip(trip: Trip) que tenemos, necesitamos el objeto Trip.
                // Si 'existingTrip' es una variable de clase que se actualiza en observeTripDetails:
                tripDao.getTripById(currentTripId).collectLatest { tripNullable -> // Usamos collectLatest para obtener el valor más reciente
                    tripNullable?.let { tripToDelete ->
                        lifecycleScope.launch { // Nueva coroutine para la operación DAO
                            tripDao.deleteTrip(tripToDelete)
                            Toast.makeText(this@TripDetailActivity, "Viaje '${tripToDelete.name}' borrado", Toast.LENGTH_SHORT).show()
                            finish() // Volver a MainActivity
                        }
                    }
                }
                // Si 'existingTrip' no estuviera disponible o actualizado, tendríamos que volver a cargarlo aquí
                // antes de llamar a tripDao.deleteTrip(existingTrip!!).
            }
        } else {
            Toast.makeText(this, "Error: No se pudo borrar el viaje", Toast.LENGTH_SHORT).show()
        }
    }

    // Para manejar el botón de "Atrás" de la ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}