package com.dfmiguel.gopack // O tu paquete

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar // Importar ProgressBar
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner // Para obtener un LifecycleScope
import androidx.lifecycle.lifecycleScope // Para el scope de coroutines
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job // Para cancelar coroutines
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TripAdapter(
    private val tripDao: TripDao,
    private val coroutineScope: CoroutineScope, // NUEVO PARÁMETRO
    private val onItemClicked: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    // Formateador de fecha para mostrarla más amigable
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val storedDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripNameTextView: TextView = itemView.findViewById(R.id.textViewTripName)
        private val tripDestinationTextView: TextView = itemView.findViewById(R.id.textViewTripDestination)
        private val tripStartDateTextView: TextView = itemView.findViewById(R.id.textViewTripStartDate) // NUEVO
        private val tripEndDateTextView: TextView = itemView.findViewById(R.id.textViewTripEndDate)    // NUEVO
        private val itemProgressTextView: TextView = itemView.findViewById(R.id.textViewItemProgress) // NUEVO
        private val tripProgressBar: ProgressBar = itemView.findViewById(R.id.progressBarTripItems) // NUEVO

        internal var currentItemCountJob: Job? = null // Para cancelar la coroutine si el ViewHolder se recicla

        @SuppressLint("SetTextI18n")
        fun bind(trip: Trip, onItemClicked: (Trip) -> Unit) {
            tripNameTextView.text = trip.name
            tripDestinationTextView.text = trip.destination
            itemView.setOnClickListener { onItemClicked(trip) }

            // Formatear y mostrar fechas
            trip.startDate?.let {
                try {
                    val date = storedDateFormat.parse(it)
                    tripStartDateTextView.text = date?.let { d -> displayDateFormat.format(d) } ?: ""
                    tripStartDateTextView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    tripStartDateTextView.text = it // Mostrar como texto si no se puede parsear
                    tripStartDateTextView.visibility = View.VISIBLE
                }
            } ?: run {
                tripStartDateTextView.visibility = View.GONE // O "Fecha no definida"
            }

            trip.endDate?.let {
                try {
                    val date = storedDateFormat.parse(it)
                    tripEndDateTextView.text = date?.let { d -> displayDateFormat.format(d) } ?: ""
                    tripEndDateTextView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    tripEndDateTextView.text = it // Mostrar como texto si no se puede parsear
                    tripEndDateTextView.visibility = View.VISIBLE
                }
            } ?: run {
                tripEndDateTextView.visibility = View.GONE // O "Fecha no definida"
            }
            // Si solo hay una fecha, ajusta la visibilidad o el texto para que quede bien


            // Cancelar job anterior si existe
            currentItemCountJob?.cancel()
            // Usamos el lifecycleScope del itemView (si está disponible y es una Activity/Fragment)
            // o creamos un scope propio. Para un adapter, es más complejo.
            // Por simplicidad ahora, y sabiendo que puede mejorarse con ViewModels,
            // vamos a usar el lifecycleScope del itemView si lo tiene (a partir de ciertas versiones de RecyclerView y AppCompat).
            // Una forma más segura es pasar un CoroutineScope al Adapter.
            // Pero para MVP, intentemos con el del itemView.
            currentItemCountJob = coroutineScope.launch {
                // Las llamadas al DAO son suspend, por lo que por defecto se ejecutarán
                // en el hilo que Room designe para ello (generalmente un hilo de fondo).
                val totalItems = tripDao.getTotalItemCountForTrip(trip.id)
                val checkedItems = tripDao.getCheckedItemCountForTrip(trip.id)

                // Asegurarnos de actualizar la UI en el hilo principal si es necesario,
                // aunque launch desde lifecycleScope suele estar ya en el Main dispatcher.
                // Por seguridad, o si el scope no fuera el Main, usarías withContext(Dispatchers.Main)
                // Pero con lifecycleScope pasado desde la Activity, esto ya debería ser seguro.

                if (totalItems > 0) {
                    itemProgressTextView.text = "$checkedItems/$totalItems ítems"
                    tripProgressBar.max = totalItems
                    tripProgressBar.progress = checkedItems
                    itemProgressTextView.visibility = View.VISIBLE
                    tripProgressBar.visibility = View.VISIBLE
                } else {
                    itemProgressTextView.text = "Sin ítems"
                    tripProgressBar.progress = 0
                    tripProgressBar.max = 1
                    itemProgressTextView.visibility = View.VISIBLE
                    tripProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = getItem(position)
        holder.bind(currentTrip, onItemClicked)
    }

    // Cancelar el job cuando el ViewHolder es reciclado o desvinculado
    override fun onViewRecycled(holder: TripViewHolder) {
        super.onViewRecycled(holder)
        holder.currentItemCountJob?.cancel()
    }


    companion object {
        private class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
            override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
                return oldItem == newItem
            }
        }
    }
}