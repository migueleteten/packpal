package com.dfmiguel.gopack // Asegúrate que coincida con tu paquete

import android.os.Bundle
import android.app.DatePickerDialog
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // Para formatear la fecha
import java.util.Calendar // Para obtener la fecha actual y trabajar con fechas
import java.util.Locale // Para el formato de fecha

class AddTripActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_add_trip)

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
            title = "Editar Viaje" // Cambiamos título
            buttonSaveTrip.text = "Actualizar Viaje" // Cambiamos texto del botón
            loadTripData(currentTripId!!) // Cargamos los datos del viaje
        } else {
            title = getString(R.string.title_activity_add_trip) // Título original
            buttonSaveTrip.text = getString(R.string.button_save_trip) // Texto original
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
            if (currentTripId != null && existingTrip != null) {
                // Modo Edición: Actualizar viaje existente
                val updatedTrip = existingTrip!!.copy( // Creamos una copia con los nuevos valores
                    name = tripName,
                    destination = tripDestination,
                    startDate = startDate,
                    endDate = endDate
                )
                tripDao.updateTrip(updatedTrip)
                Toast.makeText(this@AddTripActivity, "Viaje '${updatedTrip.name}' actualizado", Toast.LENGTH_LONG).show()
            } else {
                // Modo Añadir: Insertar nuevo viaje
                val newTrip = Trip(
                    name = tripName,
                    destination = tripDestination,
                    startDate = startDate,
                    endDate = endDate
                )
                tripDao.insertTrip(newTrip)
                Toast.makeText(this@AddTripActivity, "Viaje '$tripName' guardado", Toast.LENGTH_LONG).show()
            }
            finish() // Cierra esta actividad y vuelve a la anterior
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        // Si el EditText ya tiene una fecha, intentamos usarla como fecha inicial del diálogo
        val currentEditText = if (isStartDate) editTextStartDate else editTextEndDate
        if (currentEditText.text.toString().isNotEmpty()) {
            try {
                calendar.time = dateFormat.parse(currentEditText.text.toString()) ?: Calendar.getInstance().time
            } catch (e: Exception) {
                // si hay error parseando, usar fecha actual
            }
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