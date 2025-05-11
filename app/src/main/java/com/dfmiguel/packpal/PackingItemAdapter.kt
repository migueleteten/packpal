package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.graphics.Paint // Para tachar texto
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PackingItemAdapter(
    private val onItemClicked: (PackingItem) -> Unit, // Para manejar clic en el ítem (para editarlo luego)
    private val onItemCheckedChange: (PackingItem, Boolean) -> Unit // Para manejar cambio en el checkbox
) : ListAdapter<PackingItem, PackingItemAdapter.PackingItemViewHolder>(PackingItemDiffCallback()) {

    class PackingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBoxItemChecked: CheckBox = itemView.findViewById(R.id.checkBoxItemChecked)
        private val textViewItemName: TextView = itemView.findViewById(R.id.textViewItemName)
        private val textViewItemQuantity: TextView = itemView.findViewById(R.id.textViewItemQuantity)

        fun bind(
            item: PackingItem,
            onItemClicked: (PackingItem) -> Unit,
            onItemCheckedChange: (PackingItem, Boolean) -> Unit
        ) {
            textViewItemName.text = item.name
            textViewItemQuantity.text = "x${item.quantity}"
            checkBoxItemChecked.isChecked = item.isChecked

            // Aplicar/quitar tachado según isChecked
            if (item.isChecked) {
                textViewItemName.paintFlags = textViewItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textViewItemName.paintFlags = textViewItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            itemView.setOnClickListener { onItemClicked(item) }
            checkBoxItemChecked.setOnCheckedChangeListener { _, isChecked ->
                // Importante: desvincular el listener temporalmente si es necesario para evitar bucles
                // al rebindear, pero para un CheckBox simple suele ser ok.
                // Si actualizamos el item aquí, el Flow lo propagará y ListAdapter lo manejará.
                onItemCheckedChange(item, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_packing_item, parent, false)
        return PackingItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackingItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, onItemClicked, onItemCheckedChange)
    }

    companion object {
        private class PackingItemDiffCallback : DiffUtil.ItemCallback<PackingItem>() {
            override fun areItemsTheSame(oldItem: PackingItem, newItem: PackingItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PackingItem, newItem: PackingItem): Boolean {
                return oldItem == newItem // Data class se encarga de la comparación de contenido
            }
        }
    }
}