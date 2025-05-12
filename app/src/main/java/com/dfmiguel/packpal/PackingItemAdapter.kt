package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.annotation.SuppressLint
import android.graphics.Paint // Para tachar texto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView // Importar ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PackingItemAdapter(
    // private val onItemNameClicked: (PackingItem) -> Unit, // Cambiado de onItemClicked a onItemNameClicked para claridad
    private val onItemCheckedChange: (PackingItem, Boolean) -> Unit, // Para manejar cambio en el checkbox
    private val onEditItemClicked: (PackingItem) -> Unit // NUEVA LAMBDA para editar
) : ListAdapter<PackingItem, PackingItemAdapter.PackingItemViewHolder>(PackingItemDiffCallback()) {

    class PackingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewItemCategoryIcon: ImageView = itemView.findViewById(R.id.imageViewItemCategoryIcon)
        private val checkBoxItemChecked: CheckBox = itemView.findViewById(R.id.checkBoxItemChecked)
        private val layoutItemDetailsClickable: LinearLayout = itemView.findViewById(R.id.layoutItemDetailsClickable) // Referencia al LinearLayout
        private val textViewItemName: TextView = itemView.findViewById(R.id.textViewItemName)
        private val textViewItemCategory: TextView = itemView.findViewById(R.id.textViewItemCategory) // Referencia a categoría
        private val textViewItemQuantity: TextView = itemView.findViewById(R.id.textViewItemQuantity)
        private val buttonEditItem: ImageButton = itemView.findViewById(R.id.buttonEditItem) // Referencia al botó

        @SuppressLint("SetTextI18n") // Si AS te lo sugirió aquí, perfecto
        fun bind(
            item: PackingItem,
            // onItemNameClicked: (PackingItem) -> Unit, // Recordatorio: esta la eliminamos
            onItemCheckedChange: (PackingItem, Boolean) -> Unit,
            onEditItemClicked: (PackingItem) -> Unit
        ) {
            textViewItemName.text = item.name
            textViewItemQuantity.text = "x${item.quantity}"

            val categoryDisplayName = AppCategories.getDisplayNameForCategory(item.category)
            val categoryIconResId = AppCategories.getIconResIdForCategory(item.category)

            textViewItemCategory.text = categoryDisplayName
            // Hacemos visible el texto de categoría solo si no es "Otros" o "General" o si tiene un nombre significativo.
            // O podrías decidir mostrar siempre la categoría. ¡A tu gusto!
            textViewItemCategory.isVisible = !(categoryDisplayName.equals("Otros / General", ignoreCase = true) ||
                    categoryDisplayName.equals("General", ignoreCase = true))


            imageViewItemCategoryIcon.setImageResource(categoryIconResId)

            // --- INICIO DE LA CORRECCIÓN PARA EL CHECKBOX ---
            // 1. Quitamos cualquier listener anterior para evitar que se dispare al cambiar .isChecked
            checkBoxItemChecked.setOnCheckedChangeListener(null)

            // 2. Establecemos el estado del CheckBox basado en los datos del ítem actual
            checkBoxItemChecked.isChecked = item.isChecked
            // --- FIN DE LA CORRECCIÓN PARA EL CHECKBOX ---


            // Aplicar/quitar tachado según isChecked (esto se hace después de setear isChecked)
            if (item.isChecked) {
                textViewItemName.paintFlags = textViewItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textViewItemCategory.paintFlags = textViewItemCategory.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textViewItemName.paintFlags = textViewItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                textViewItemCategory.paintFlags = textViewItemCategory.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Hacemos que pulsar el nombre/categoría también dispare la edición
            layoutItemDetailsClickable.setOnClickListener { onEditItemClicked(item) }
            buttonEditItem.setOnClickListener { onEditItemClicked(item) }

            // --- INICIO DE LA CORRECCIÓN PARA EL CHECKBOX (listener) ---
            // 3. Volvemos a poner el listener para que capture las interacciones del usuario
            checkBoxItemChecked.setOnCheckedChangeListener { _, isUserChecked ->
                onItemCheckedChange(item, isUserChecked)
            }
            // --- FIN DE LA CORRECCIÓN PARA EL CHECKBOX (listener) ---
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_packing_item, parent, false)
        return PackingItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackingItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, onItemCheckedChange, onEditItemClicked)
    }

    companion object {
        private class PackingItemDiffCallback : DiffUtil.ItemCallback<PackingItem>() {
            override fun areItemsTheSame(oldItem: PackingItem, newItem: PackingItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: PackingItem, newItem: PackingItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}