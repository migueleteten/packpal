package com.dfmiguel.gopack // Asegúrate que coincida con tu paquete

import android.os.Bundle
import android.app.DatePickerDialog
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // Para formatear la fecha
import java.util.Calendar // Para obtener la fecha actual y trabajar con fechas
import java.util.Locale // Para el formato de fecha
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class AddTripActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var toolbarAddTrip: MaterialToolbar
    private lateinit var editTextTripName: TextInputEditText
    private lateinit var editTextTripDestination: TextInputEditText
    private lateinit var editTextStartDate: TextInputEditText
    private lateinit var editTextEndDate: TextInputEditText
    private lateinit var buttonSaveTrip: Button

    // Obtenemos el DAO de nuestra clase Application
    private val tripDao by lazy { (application as GoPackApplication).tripDao }

    // Define el formato de fecha que queremos usar
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var currentTripId: Long? = null // Para guardar el ID si estamos editando
    private var existingTrip: Trip? = null // Para guardar el viaje existente si estamos editando

    companion object {
        const val EXTRA_EDIT_TRIP_ID = "extra_edit_trip_id" // Nueva constante para pasar el ID a editar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        setContentView(R.layout.activity_add_trip)

        toolbarAddTrip = findViewById(R.id.toolbarAddTrip) // NUEVO
        setSupportActionBar(toolbarAddTrip)                 // NUEVO
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Cambiar el título de la ActionBar para esta Activity
        title = getString(R.string.title_activity_add_trip) // Usamos el string que definimos

        editTextTripName = findViewById(R.id.editTextTripName)
        editTextTripDestination = findViewById(R.id.editTextTripDestination)
        editTextStartDate = findViewById(R.id.editTextStartDate)
        editTextEndDate = findViewById(R.id.editTextEndDate)
        buttonSaveTrip = findViewById(R.id.buttonSaveTrip)

        // Comprobar si nos han pasado un ID para editar
        if (intent.hasExtra(EXTRA_EDIT_TRIP_ID)) {
            currentTripId = intent.getLongExtra(EXTRA_EDIT_TRIP_ID, -1L)
            if (currentTripId == -1L) { // Seguridad por si algo falla
                currentTripId = null // Aseguramos que sea null si el ID no es válido
            }
        }

        if (currentTripId != null) {
            // Asumiendo que tienes 'existingTrip' cargado para el modo edición
            // supportActionBar?.title = "Editar Viaje: ${existingTrip?.name ?: ""}"
            buttonSaveTrip.text = getString(R.string.title_activity_update_trip)
            loadTripData(currentTripId!!)
        } else {
            supportActionBar?.title = getString(R.string.title_activity_add_trip) // O tu string de aventura
            buttonSaveTrip.text = getString(R.string.button_save_trip)
        }

        buttonSaveTrip.setOnClickListener {
            saveOrUpdateTrip()
        }

        // Configurar los EditText de fecha para que abran el DatePickerDialog
        editTextStartDate.setOnClickListener {
            showDatePickerDialog(isStartDate = true)
        }
        // Hacemos lo mismo para que no se pueda escribir directamente si es focusable false
        editTextStartDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePickerDialog(isStartDate = true)
        }

        editTextEndDate.setOnClickListener {
            showDatePickerDialog(isStartDate = false)
        }
        editTextEndDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePickerDialog(isStartDate = false)
        }

        if (currentTripId == null) {
            editTextTripName.requestFocus()
            editTextTripName.post { // Ejecuta después de que la vista haya sido medida y layoutada
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextTripName, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun loadTripData(tripId: Long) {
        lifecycleScope.launch {
            tripDao.getTripById(tripId).collectLatest { trip -> // Usamos collectLatest por si acaso
                if (trip != null) {
                    existingTrip = trip // Guardamos el viaje cargado
                    editTextTripName.setText(trip.name)
                    editTextTripDestination.setText(trip.destination)
                    editTextStartDate.setText(trip.startDate ?: "") // Ponemos string vacío si es null
                    editTextEndDate.setText(trip.endDate ?: "")   // Ponemos string vacío si es null

                    if (currentTripId != null) { // Doble check por si acaso, aunque ya estamos en modo edición
                        supportActionBar?.title = "Editar viaje: ${trip.name}" // Usamos trip.name directamente
                    }
                } else {
                    Toast.makeText(this@AddTripActivity, "Error al cargar datos del viaje para editar", Toast.LENGTH_LONG).show()
                    finish() // Si no podemos cargar el viaje, cerramos
                }
            }
        }
    }

    private fun saveOrUpdateTrip() {
        val tripName = editTextTripName.text.toString().trim()
        val tripDestination = editTextTripDestination.text.toString().trim()
        val startDate = editTextStartDate.text.toString().trim().ifEmpty { null }
        val endDate = editTextEndDate.text.toString().trim().ifEmpty { null }

        if (tripName.isEmpty()) {
            editTextTripName.error = "El nombre del viaje es obligatorio"
            Toast.makeText(this, "Por favor, introduce un nombre para el viaje", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (currentTripId != null && existingTrip != null) { // Modo Edición
                val updatedTrip = existingTrip!!.copy(
                    name = tripName,
                    destination = tripDestination,
                    startDate = startDate,
                    endDate = endDate
                )
                tripDao.updateTrip(updatedTrip)
                Toast.makeText(this@AddTripActivity, "Viaje '${updatedTrip.name}' actualizado", Toast.LENGTH_LONG).show()
                finish() // En modo edición, simplemente cerramos y volvemos a TripDetailActivity (o donde sea que se llamó)
            } else { // Modo Añadir Nuevo Viaje
                val newTrip = Trip(
                    name = tripName,
                    destination = tripDestination,
                    startDate = startDate,
                    endDate = endDate
                )
                val newTripId = tripDao.insertTrip(newTrip) // Obtener el ID del viaje recién insertado

                if (newTripId != -1L) { // Room devuelve -1 en error, aunque con OnConflictStrategy.REPLACE es raro
                    Toast.makeText(this@AddTripActivity, "Viaje '$tripName' guardado", Toast.LENGTH_LONG).show()

                    // ¡NUESTRO PRIMER EVENTO DE ANALYTICS - FORMA ACTUALIZADA!
                    val params = Bundle().apply {
                        // Opcional: puedes añadir parámetros para dar más contexto al evento
                        putString("destination_name", tripDestination) // Ejemplo de parámetro String
                        putInt("name_length", tripName.length)      // Ejemplo de parámetro Int
                        putBoolean("has_start_date", (startDate != null)) // Ejemplo de parámetro Boolean
                    }
                    firebaseAnalytics.logEvent("trip_created", params) // Pasamos el Bundle

                    // NAVEGAR AL DETALLE DEL VIAJE RECIÉN CREADO
                    val intent = Intent(this@AddTripActivity, TripDetailActivity::class.java).apply {
                        putExtra(TripDetailActivity.EXTRA_TRIP_ID, newTripId)
                    }
                    startActivity(intent)
                    finish() // Cierra AddTripActivity para que al pulsar "atrás" en TripDetailActivity no volvamos aquí
                } else {
                    Toast.makeText(this@AddTripActivity, "Error al guardar el viaje", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        // Si el EditText ya tiene una fecha, intentamos usarla como fecha inicial del diálogo
        val currentEditText = if (isStartDate) editTextStartDate else editTextEndDate
        if (currentEditText.text.toString().isNotEmpty()) {
            calendar.time = dateFormat.parse(currentEditText.text.toString()) ?: Calendar.getInstance().time
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Los meses en Calendar van de 0 a 11, por eso sumamos 1
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val formattedDate = dateFormat.format(selectedDate.time)

                if (isStartDate) {
                    editTextStartDate.setText(formattedDate)
                } else {
                    editTextEndDate.setText(formattedDate)
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}