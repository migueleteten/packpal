package com.dfmiguel.gopack // O tu paquete actual

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class AddPackingItemDialogFragment : DialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var tripIdOwner: Long = -1L
    private var editingItemId: Long? = null
    private var existingPackingItem: PackingItem? = null

    // Vistas del layout del diálogo
    private lateinit var editTextItemName: TextInputEditText
    private lateinit var spinnerItemCategory: Spinner
    private lateinit var editTextItemQuantity: TextInputEditText

    // Declaramos el DAO, pero lo inicializaremos más tarde
    private lateinit var packingItemDao: PackingItemDao

    companion object {
        private const val ARG_TRIP_ID = "trip_id_owner"
        private const val ARG_EDIT_ITEM_ID = "edit_item_id"

        fun newInstance(tripId: Long): AddPackingItemDialogFragment {
            val fragment = AddPackingItemDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForEdit(tripId: Long, itemId: Long): AddPackingItemDialogFragment {
            val fragment = AddPackingItemDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId) // tripIdOwner
            args.putLong(ARG_EDIT_ITEM_ID, itemId)
            fragment.arguments = args
            return fragment
        }
    }

    // onAttach se llama ANTES que onCreate. Es un buen lugar para obtener el DAO
    // ya que el Fragment está adjunto a la Activity y tenemos un Context.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        firebaseAnalytics = Firebase.analytics
        // Inicializamos el DAO aquí, usando el 'context' de la Activity
        // que ahora sabemos que existe.
        packingItemDao = (context.applicationContext as GoPackApplication).packingItemDao
        // O si tu PackPalApplication es la Application de la activity:
        // packingItemDao = (requireActivity().application as PackPalApplication).packingItemDao
        // Usar context.applicationContext es generalmente más seguro para singletons globales.
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        arguments?.let {
            tripIdOwner = it.getLong(ARG_TRIP_ID, -1L)
            if (it.containsKey(ARG_EDIT_ITEM_ID)) {
                editingItemId = it.getLong(ARG_EDIT_ITEM_ID)
            }
        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Ahora 'packingItemDao' ya debería estar inicializado gracias a onAttach()

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.dialog_add_packing_item, null)

        editTextItemName = view.findViewById(R.id.editTextItemNameDialog)
        spinnerItemCategory = view.findViewById(R.id.spinnerItemCategoryDialog)
        editTextItemQuantity = view.findViewById(R.id.editTextItemQuantityDialog)

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

        dialog.setOnShowListener {
            if (editingItemId != null) {
                loadItemData(editingItemId!!)
            }
            // ... (la lógica del positiveButton.setOnClickListener sigue igual)
            val positiveButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val itemName = editTextItemName.text.toString().trim()
                val selectedCategoryInfo = AppCategories.list[spinnerItemCategory.selectedItemPosition]
                val itemCategoryInternalName = selectedCategoryInfo.internalName
                val itemQuantityStr = editTextItemQuantity.text.toString().trim()

                if (itemName.isEmpty()) {
                    editTextItemName.error = "El nombre del ítem es obligatorio"
                    Toast.makeText(requireContext(), "El nombre del ítem es obligatorio", Toast.LENGTH_SHORT).show()
                    vibrate()
                    return@setOnClickListener
                } else {
                    editTextItemName.error = null
                }

                val itemQuantity = itemQuantityStr.toIntOrNull() ?: 1

                if (tripIdOwner == -1L && editingItemId == null) {
                    Toast.makeText(requireContext(), "Error: ID de viaje no válido", Toast.LENGTH_SHORT).show()
                    vibrate()
                    return@setOnClickListener
                }

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

                        val itemParams = Bundle().apply {
                            putString("item_category", itemCategoryInternalName)
                            putString("item_name", itemName)
                            putInt("item_name_length", itemName.length)
                            putInt("item_quantity", itemQuantity)
                        }
                        firebaseAnalytics.logEvent("item_added", itemParams)

                        Toast.makeText(requireContext(), "Ítem '${newItem.name}' añadido", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun loadItemData(itemId: Long) {
        // ... (igual que antes)
        lifecycleScope.launch {
            packingItemDao.getPackingItemById(itemId).collectLatest { item ->
                if (item != null) {
                    existingPackingItem = item
                    if (::editTextItemName.isInitialized) {
                        editTextItemName.setText(item.name)
                        val categoryPosition = AppCategories.list.indexOfFirst { it.internalName.equals(item.category, ignoreCase = true) }
                        if (categoryPosition != -1) {
                            spinnerItemCategory.setSelection(categoryPosition)
                        } else {
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

    @SuppressLint("ServiceCast")
    @Suppress("DEPRECATION")
    private fun vibrate() {
        // ... (igual que antes)
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(100)
        }
    }
}