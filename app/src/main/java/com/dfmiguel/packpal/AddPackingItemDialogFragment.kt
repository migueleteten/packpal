package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect // Para la vibración
import android.os.Vibrator // Para la vibración
import android.os.VibratorManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.view.View // Para el ArrayAdapter personalizado
import android.view.ViewGroup // Para el ArrayAdapter personalizado
import android.widget.ArrayAdapter // Importar ArrayAdapter
import android.widget.Spinner // Importar Spinner
import android.widget.TextView // Para el ArrayAdapter personalizado
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddPackingItemDialogFragment : DialogFragment() {

    private var tripIdOwner: Long = -1L
    private var editingItemId: Long? = null // Para el ID del ítem si estamos editando
    private var existingPackingItem: PackingItem? = null // Para el objeto ítem existente

    private lateinit var editTextItemName: TextInputEditText
    // private lateinit var editTextItemCategory: TextInputEditText
    private lateinit var spinnerItemCategory: Spinner // NUEVO SPINNER
    private lateinit var editTextItemQuantity: TextInputEditText

    // DAO para interactuar con la base de datos
    private val packingItemDao by lazy {
        (requireActivity().application as PackPalApplication).packingItemDao
    }

    companion object {
        private const val ARG_TRIP_ID = "trip_id_owner"
        private const val ARG_EDIT_ITEM_ID = "edit_item_id" // Nuevo argumento

        fun newInstance(tripId: Long): AddPackingItemDialogFragment {
            val fragment = AddPackingItemDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }

        // newInstance para editar
        fun newInstanceForEdit(tripId: Long, itemId: Long): AddPackingItemDialogFragment {
            val fragment = AddPackingItemDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId) // Aún necesitamos el tripIdOwner
            args.putLong(ARG_EDIT_ITEM_ID, itemId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tripIdOwner = it.getLong(ARG_TRIP_ID, -1L)
            if (it.containsKey(ARG_EDIT_ITEM_ID)) {
                editingItemId = it.getLong(ARG_EDIT_ITEM_ID)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_packing_item, null)

        editTextItemName = view.findViewById(R.id.editTextItemNameDialog)
        spinnerItemCategory = view.findViewById(R.id.spinnerItemCategoryDialog)
        editTextItemQuantity = view.findViewById(R.id.editTextItemQuantityDialog)

        // Configurar el ArrayAdapter para el Spinner
        // Usaremos los displayNames de nuestras AppCategories
        val categoryDisplayNames = AppCategories.list.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryDisplayNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerItemCategory.adapter = adapter

        val dialogTitle: String
        val positiveButtonText: String

        if (editingItemId != null) {
            dialogTitle = "Editar Ítem"
            positiveButtonText = "Actualizar"
            // loadItemData(editingItemId!!)
        } else {
            dialogTitle = getString(R.string.dialog_title_add_item)
            positiveButtonText = getString(R.string.dialog_add)
            val defaultCategoryPosition = AppCategories.list.indexOfFirst { it.internalName == "OTHER" }
            if (defaultCategoryPosition != -1) {
                spinnerItemCategory.setSelection(defaultCategoryPosition)
            }
        }

        builder.setView(view)
            .setTitle(dialogTitle)
            .setPositiveButton(positiveButtonText, null)
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.cancel()
            }
        val dialog = builder.create()

        // Sobrescribimos el listener del botón positivo DESPUÉS de mostrar el diálogo
        // para poder controlar cuándo se cierra.
        dialog.setOnShowListener {
            if (editingItemId != null) {
                loadItemData(editingItemId!!) // Cargar datos si estamos editando
            }
            val positiveButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                // Ahora, la lógica de saveOrUpdateItem se ejecuta aquí
                val itemName = editTextItemName.text.toString().trim()
                val selectedCategoryInfo = AppCategories.list[spinnerItemCategory.selectedItemPosition]
                val itemCategoryInternalName = selectedCategoryInfo.internalName // Guardamos el internalName
                val itemQuantityStr = editTextItemQuantity.text.toString().trim()

                if (itemName.isEmpty()) {
                    editTextItemName.error = "El nombre del ítem es obligatorio" // Mostrar error en el EditText
                    Toast.makeText(requireContext(), "El nombre del ítem es obligatorio", Toast.LENGTH_SHORT).show()
                    vibrate() // ¡Vibrar!
                    return@setOnClickListener // IMPORTANTE: No continuamos, el diálogo no se cierra
                } else {
                    editTextItemName.error = null // Limpiar error si lo había
                }
                val itemQuantity = itemQuantityStr.toIntOrNull() ?: 1

                if (tripIdOwner == -1L && editingItemId == null) {
                    Toast.makeText(requireContext(), "Error: ID de viaje no válido", Toast.LENGTH_SHORT).show()
                    vibrate()
                    return@setOnClickListener
                }

                // Si está bien, procedemos a guardar/actualizar
                lifecycleScope.launch {
                    if (editingItemId != null && existingPackingItem != null) {
                        val updatedItem = existingPackingItem!!.copy(
                            name = itemName,
                            category = itemCategoryInternalName,
                            quantity = itemQuantity
                        )
                        packingItemDao.updatePackingItem(updatedItem)
                        Toast.makeText(requireContext(), "Ítem '${updatedItem.name}' actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        val newItem = PackingItem(
                            tripIdOwner = tripIdOwner,
                            name = itemName,
                            category = itemCategoryInternalName,
                            quantity = itemQuantity
                        )
                        packingItemDao.insertPackingItem(newItem)
                        Toast.makeText(requireContext(), "Ítem '${newItem.name}' añadido", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss() // Cerramos el diálogo MANUALMENTE solo si fue bien
                }
            }
        }
        return dialog
    }

    private fun loadItemData(itemId: Long) {
        lifecycleScope.launch {
            packingItemDao.getPackingItemById(itemId).collectLatest { item ->
                if (item != null) {
                    existingPackingItem = item
                    if (::editTextItemName.isInitialized) {
                        editTextItemName.setText(item.name)
                        // Seleccionar la categoría correcta en el Spinner
                        val categoryPosition = AppCategories.list.indexOfFirst { it.internalName.equals(item.category, ignoreCase = true) }
                        if (categoryPosition != -1) {
                            spinnerItemCategory.setSelection(categoryPosition)
                        } else {
                            // Si la categoría guardada no está en nuestra lista predefinida (ej. "General" o una antigua),
                            // podríamos seleccionar "Otros" o dejar la primera opción.
                            val otherPosition = AppCategories.list.indexOfFirst { it.internalName == "OTHER" }
                            if (otherPosition != -1) spinnerItemCategory.setSelection(otherPosition)
                        }
                        editTextItemQuantity.setText(item.quantity.toString())
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar ítem para editar", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    @Suppress("DEPRECATION") // Suprimimos el warning de la vibración antigua solo donde se usa
    private fun vibrate() {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31 es Android S
            val vibratorManager =
                context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26 es Android Oreo (para VibrationEffect)
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(100) // Para API < 26
        }
    }
}