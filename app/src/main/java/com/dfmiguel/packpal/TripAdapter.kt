package com.dfmiguel.packpal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TripAdapter(
    private val onItemClicked: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) { // CAMBIO AQUÍ

    // ViewHolder sigue igual
    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripNameTextView: TextView = itemView.findViewById(R.id.textViewTripName)
        private val tripDestinationTextView: TextView = itemView.findViewById(R.id.textViewTripDestination)

        fun bind(trip: Trip, onItemClicked: (Trip) -> Unit) {
            tripNameTextView.text = trip.name
            tripDestinationTextView.text = trip.destination
            itemView.setOnClickListener { onItemClicked(trip) }
        }
    }

    // onCreateViewHolder sigue prácticamente igual
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    // onBindViewHolder ahora usa getItem(position)
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = getItem(position) // CAMBIO AQUÍ
        holder.bind(currentTrip, onItemClicked)
    }

    // getItemCount() YA NO ES NECESARIO, ListAdapter lo maneja

    // Esta es la clase que le dice a ListAdapter cómo calcular las diferencias
    companion object { // O puedes crearla como una clase separada si prefieres
        private class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
            // Comprueba si dos objetos representan el MISMO ítem (ej: por su ID)
            override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
                return oldItem.id == newItem.id
            }

            // Comprueba si el CONTENIDO de dos ítems es el mismo (ej: todos sus campos)
            // Kotlin data classes autogeneran equals(), que compara todos los campos.
            override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
                return oldItem == newItem
            }
        }
    }
}