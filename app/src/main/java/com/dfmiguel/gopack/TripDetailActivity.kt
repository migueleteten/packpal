package com.dfmiguel.gopack // Asegúrate que coincida con tu paquete

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest // Usaremos collectLatest para el detalle de un solo viaje
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog // Para el diálogo
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.getValue
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class TripDetailActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var toolbarTripDetail: MaterialToolbar
    private lateinit var textViewDetailTripName: TextView
    private lateinit var textViewDetailTripDestination: TextView
    private lateinit var textViewDetailStartDate: TextView
    private lateinit var textViewDetailEndDate: TextView

    // Vistas para la lista de ítems y estado vacío
    private lateinit var recyclerViewPackingItems: RecyclerView
    private lateinit var packingItemAdapter: PackingItemAdapter
    private lateinit var fabAddPackingItem: FloatingActionButton
    private lateinit var layoutEmptyPackingList: LinearLayout // NUEVA VARIABLE
    private lateinit var buttonUseTemplate: Button          // NUEVA VARIABLE

    // DAOs
    private val tripDao by lazy { (application as GoPackApplication).tripDao } // Asumiendo que renombraste GoPackApplication
    private val packingItemDao by lazy { (application as GoPackApplication).packingItemDao }

    private var currentTripId: Long = -1L
    // private var existingTrip: Trip? = null // Comentamos o eliminamos si no se usa activamente

    companion object {
        const val EXTRA_TRIP_ID = "extra_trip_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        setContentView(R.layout.activity_trip_detail)

        toolbarTripDetail = findViewById(R.id.toolbarTripDetail)
        setSupportActionBar(toolbarTripDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // El título se pone en observeTripDetails

        // Views para detalles del viaje
        textViewDetailTripName = findViewById(R.id.textViewDetailTripName)
        textViewDetailTripDestination = findViewById(R.id.textViewDetailTripDestination)
        textViewDetailStartDate = findViewById(R.id.textViewDetailStartDate)
        textViewDetailEndDate = findViewById(R.id.textViewDetailEndDate)

        // Views para lista de equipaje y estado vacío
        recyclerViewPackingItems = findViewById(R.id.recyclerViewPackingItems)
        fabAddPackingItem = findViewById(R.id.fabAddPackingItem)
        layoutEmptyPackingList = findViewById(R.id.layoutEmptyPackingList) // NUEVA
        buttonUseTemplate = findViewById(R.id.buttonUseTemplate)       // NUEVA

        currentTripId = intent.getLongExtra(EXTRA_TRIP_ID, -1L)

        if (currentTripId == -1L) {
            Toast.makeText(this, "Error: ID de viaje no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupPackingItemsRecyclerView()
        observeTripDetails()
        observePackingItems() // Esta función ahora manejará la visibilidad

        fabAddPackingItem.setOnClickListener {
            if (currentTripId != -1L) {
                val dialog = AddPackingItemDialogFragment.newInstance(currentTripId)
                dialog.show(supportFragmentManager, "AddPackingItemDialog")
            } else {
                Toast.makeText(this, "Error: No se puede añadir ítem sin ID de viaje válido", Toast.LENGTH_LONG).show()
            }
        }

        buttonUseTemplate.setOnClickListener { // NUEVO LISTENER
            showTemplateSelectionDialog()
        }
    }

    private fun observeTripDetails() {
        lifecycleScope.launch {
            tripDao.getTripById(currentTripId).collectLatest { trip ->
                if (trip != null) {
                    supportActionBar?.title = trip.name
                    textViewDetailTripName.text = trip.name // Puedes decidir si quieres mostrarlo aquí además de en la Toolbar
                    textViewDetailTripDestination.text = trip.destination
                    textViewDetailStartDate.text = trip.startDate?.let { "Inicio: $it" } ?: getString(R.string.start_date_not_defined) // Nuevo String
                    textViewDetailEndDate.text = trip.endDate?.let { "Fin: $it" } ?: getString(R.string.end_date_not_defined) // Nuevo String
                } else {
                    supportActionBar?.title = getString(R.string.title_activity_trip_detail)
                    Log.e("TripDetailActivity", "Viaje con ID $currentTripId no encontrado.")
                    // Considera cerrar o mostrar un error más persistente si el viaje no se encuentra
                }
            }
        }
    }

    private fun setupPackingItemsRecyclerView() {
        packingItemAdapter = PackingItemAdapter(
            onItemCheckedChange = { item, isChecked ->
                val updatedItem = item.copy(isChecked = isChecked)
                lifecycleScope.launch {
                    packingItemDao.updatePackingItem(updatedItem)
                    val checkedParams = Bundle().apply {
                        putString("item_category", item.category) // Categoría del ítem
                        putString("checked_state", if (isChecked) "true" else "false") // Estado final
                        putString("item_name", item.name)
                    }
                    firebaseAnalytics.logEvent("item_checked", checkedParams)
                }
            },
            onEditItemClicked = { item ->
                val dialog = AddPackingItemDialogFragment.newInstanceForEdit(currentTripId, item.id)
                dialog.show(supportFragmentManager, "EditPackingItemDialog")
            }
        )
        recyclerViewPackingItems.adapter = packingItemAdapter
        recyclerViewPackingItems.layoutManager = LinearLayoutManager(this)

        // --- INICIO: LÓGICA DEL SWIPE ---
        val swipeCallback = object : ItemSwipeCallback(this) { // O tu nombre de clase de callback
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
                // val item = packingItemAdapter.currentList[position] // Esta línea podría dar error si la lista cambia rápidamente
                // Es más seguro obtener el ítem directamente del ViewHolder si tu ViewHolder lo expone,
                // o si no, asumir que la posición es válida para la lista actual.
                // Para ListAdapter, currentList es la forma de acceder, pero cuidado con modificaciones concurrentes.
                // Vamos a intentar obtenerlo de forma segura:
                val item = try {
                    packingItemAdapter.currentList[position]
                } catch (e: IndexOutOfBoundsException) {
                    null // Si la posición ya no es válida, no hacemos nada
                }

                item?.let { // Solo si el ítem no es null
                    if (direction == ItemTouchHelper.LEFT) { // Swipe a la Izquierda (Borrar)
                        showDeletePackingItemConfirmationDialog(it)
                    } else if (direction == ItemTouchHelper.RIGHT) { // Swipe a la Derecha (Marcar/Desmarcar)
                        toggleItemCheckedState(it)
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback) // Usamos el nombre de la variable
        itemTouchHelper.attachToRecyclerView(recyclerViewPackingItems)
        // --- FIN: LÓGICA DEL SWIPE ---
    }

    private fun toggleItemCheckedState(item: PackingItem) {
        val newItemState = !item.isChecked // Invertimos el estado actual
        val updatedItem = item.copy(isChecked = newItemState)
        lifecycleScope.launch {
            packingItemDao.updatePackingItem(updatedItem)

            val checkedParams = Bundle().apply {
                putString("item_category", item.category) // Categoría del ítem
                putString("checked_state", if (newItemState) "true" else "false") // Estado final
                putString("item_name", item.name)
            }
            firebaseAnalytics.logEvent("item_checked", checkedParams)
            // El Toast es opcional, el feedback visual del swipe y el cambio de estado en la lista
            // (tachado/destachado y el checkbox) deberían ser suficientes.
            // Toast.makeText(this, "${updatedItem.name} ${if (newItemState) "marcado" else "desmarcado"}", Toast.LENGTH_SHORT).show()
        }
        // La lista se actualiza automáticamente gracias al Flow y ListAdapter
        // El adapter debería redibujar el ítem, y su lógica de 'bind' aplicará el tachado/destachado
        // y el estado del checkbox.
    }

    private fun observePackingItems() {
        lifecycleScope.launch {
            packingItemDao.getItemsForTrip(currentTripId).collectLatest { items ->
                Log.d("TripDetailActivity", "Ítems para Viaje ID $currentTripId: ${items.size}")
                if (items.isEmpty()) {
                    recyclerViewPackingItems.visibility = View.GONE // Oculta RecyclerView
                    layoutEmptyPackingList.visibility = View.VISIBLE // Muestra layout de estado vacío
                } else {
                    recyclerViewPackingItems.visibility = View.VISIBLE // Muestra RecyclerView
                    layoutEmptyPackingList.visibility = View.GONE    // Oculta layout de estado vacío
                }
                packingItemAdapter.submitList(items)
            }
        }
    }

    // --- NUEVAS FUNCIONES PARA PLANTILLAS ---
    private fun showTemplateSelectionDialog() {
        val templateNames = AppTemplates.list.map { it.templateName }.toTypedArray()

        AlertDialog.Builder(this) // Usamos el tema del diálogo
            .setTitle(R.string.dialog_title_select_template)
            .setItems(templateNames) { dialog, which ->
                val selectedTemplate = AppTemplates.list[which]
                loadTemplateItems(selectedTemplate)
                dialog.dismiss() // Asegúrate de cerrar el diálogo
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadTemplateItems(template: PackingListTemplate) {
        if (currentTripId == -1L) {
            Toast.makeText(this, "Error: ID de viaje no válido para cargar plantilla.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val itemsToInsert = template.items.map { templateItem ->
                PackingItem(
                    // id se autogenera
                    tripIdOwner = currentTripId,
                    name = templateItem.name,
                    category = templateItem.categoryInternalName,
                    quantity = templateItem.quantity,
                    isChecked = false // Las plantillas siempre empiezan con ítems no marcados
                )
            }
            itemsToInsert.forEach { packingItemDao.insertPackingItem(it) }

            Toast.makeText(this@TripDetailActivity, "Plantilla '${template.templateName}' cargada.", Toast.LENGTH_LONG).show()
            // El Flow en observePackingItems() se encargará de actualizar la UI.
            val templateParams = Bundle().apply {
                putString("template_name", template.templateName)
                putInt("item_count_in_template", template.items.size)
            }
            firebaseAnalytics.logEvent("template_used", templateParams)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_trip_detail, menu)
        return true // Devuelve true para que el menú se muestre
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_trip -> {
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
                tripDao.getTripById(currentTripId).collectLatest { tripNullable -> // Usamos collectLatest para obtener el valor más reciente
                    tripNullable?.let { tripToDelete ->
                        lifecycleScope.launch { // Nueva coroutine para la operación DAO
                            tripDao.deleteTrip(tripToDelete)
                            Toast.makeText(this@TripDetailActivity, "Viaje '${tripToDelete.name}' borrado", Toast.LENGTH_SHORT).show()
                            finish() // Volver a MainActivity
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error: No se pudo borrar el viaje", Toast.LENGTH_SHORT).show()
        }
    }

    // Para manejar el botón de "Atrás" de la ActionBar
    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_OK) // Indicar que algo pudo cambiar
        finish() // O onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Y para el botón "atrás" del sistema, modificamos finish()
    override fun finish() {
        setResult(RESULT_OK) // Asegurar que se devuelve resultado
        super.finish()
    }

    // NUEVA FUNCIÓN para el diálogo de confirmación de borrado de ítem
    private fun showDeletePackingItemConfirmationDialog(item: PackingItem) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trip_confirmation_title) // Podemos reutilizar o crear uno nuevo "Borrar Ítem"
            .setMessage(getString(R.string.delete_trip_confirmation_message, item.name)) // Reutilizamos o creamos uno nuevo
            .setIcon(R.drawable.baseline_delete_outline_24)
            .setPositiveButton(R.string.dialog_delete) { dialog, _ ->
                deletePackingItem(item)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                // IMPORTANTE: Si cancelamos, el RecyclerView puede quedar en un estado "semi-deslizado"
                // Necesitamos notificar al adaptador para que redibuje ese ítem a su posición original.
                val position = packingItemAdapter.currentList.indexOf(item)
                if (position != -1) {
                    packingItemAdapter.notifyItemChanged(position)
                }
                dialog.dismiss()
            }
            .setOnCancelListener { // También cuando se cancela pulsando fuera o con el botón atrás
                val position = packingItemAdapter.currentList.indexOf(item)
                if (position != -1) {
                    packingItemAdapter.notifyItemChanged(position)
                }
            }
            .show()
    }

    // NUEVA FUNCIÓN para borrar el ítem
    private fun deletePackingItem(item: PackingItem) {
        lifecycleScope.launch {
            val deleteParams = Bundle().apply {
                putString("item_name", item.name)
                putString("item_category", item.category)
                putInt("item_quantity", item.quantity)
                putBoolean("was_checked", item.isChecked)
            }
            firebaseAnalytics.logEvent("item_deleted", deleteParams)
            packingItemDao.deletePackingItem(item)
            Toast.makeText(this@TripDetailActivity, "Ítem '${item.name}' borrado", Toast.LENGTH_SHORT).show()
            // La lista se actualiza automáticamente gracias al Flow y ListAdapter
        }
    }
}