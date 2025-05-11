package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddPackingItemDialogFragment : DialogFragment() {

    private var tripIdOwner: Long = -1L

    // DAO para interactuar con la base de datos
    private val packingItemDao by lazy {
        (requireActivity().application as PackPalApplication).packingItemDao
    }

    companion object {
        private const val ARG_TRIP_ID = "trip_id_owner"

        fun newInstance(tripId: Long): AddPackingItemDialogFragment {
            val fragment = AddPackingItemDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tripIdOwner = it.getLong(ARG_TRIP_ID, -1L)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_packing_item, null)

        val editTextItemName = view.findViewById<TextInputEditText>(R.id.editTextItemNameDialog)
        val editTextItemCategory = view.findViewById<TextInputEditText>(R.id.editTextItemCategoryDialog)
        val editTextItemQuantity = view.findViewById<TextInputEditText>(R.id.editTextItemQuantityDialog)

        builder.setView(view)
            .setTitle(R.string.dialog_title_add_item)
            .setPositiveButton(R.string.dialog_add) { _, _ ->
                val itemName = editTextItemName.text.toString().trim()
                var itemCategory = editTextItemCategory.text.toString().trim()
                val itemQuantityStr = editTextItemQuantity.text.toString().trim()

                if (itemName.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre del ítem es obligatorio", Toast.LENGTH_SHORT).show()
                    // Podríamos evitar que el diálogo se cierre aquí, pero requiere más lógica
                    return@setPositiveButton
                }

                if (itemCategory.isEmpty()){
                    itemCategory = "General" // Categoría por defecto si está vacía
                }

                val itemQuantity = itemQuantityStr.toIntOrNull() ?: 1 // Default a 1 si no es número o está vacío

                if (tripIdOwner == -1L) {
                    Toast.makeText(requireContext(), "Error: ID de viaje no válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newItem = PackingItem(
                    tripIdOwner = tripIdOwner,
                    name = itemName,
                    category = itemCategory,
                    quantity = itemQuantity
                    // isChecked será false por defecto
                )

                // Guardar en la base de datos usando una coroutine
                lifecycleScope.launch { // Usamos el lifecycleScope del DialogFragment
                    packingItemDao.insertPackingItem(newItem)
                    Toast.makeText(requireContext(), "Ítem '${newItem.name}' añadido", Toast.LENGTH_SHORT).show()
                }
                // El diálogo se cierra automáticamente al pulsar el botón positivo
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.cancel() // Simplemente cierra el diálogo
            }
        return builder.create()
    }
}