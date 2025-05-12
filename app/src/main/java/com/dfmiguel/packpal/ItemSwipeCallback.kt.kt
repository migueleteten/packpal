package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt

abstract class ItemSwipeCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) { // HABILITAMOS AMBAS DIRECCIONES

    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.baseline_delete_outline_24) // O tu ic_delete_swipe
    private val checkIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.baseline_check_24) // NUEVO ICONO (o el que elijas)

    private val intrinsicDeleteWidth = deleteIcon?.intrinsicWidth ?: 0
    private val intrinsicDeleteHeight = deleteIcon?.intrinsicHeight ?: 0
    private val intrinsicCheckWidth = checkIcon?.intrinsicWidth ?: 0
    private val intrinsicCheckHeight = checkIcon?.intrinsicHeight ?: 0

    private val background = ColorDrawable()
    private val redBackgroundColor = "#f44336".toColorInt() // Rojo para borrar
    private val greenBackgroundColor = "#4CAF50".toColorInt() // Verde para marcar

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // No manejamos drag & drop
    }

    // onSwiped sigue siendo abstracta o la implementas en TripDetailActivity como hiciste
    // abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, // < 0 para izquierda, > 0 para derecha
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        if (dX == 0.0f) { // No hay swipe, no dibujar nada extra
            background.setBounds(0,0,0,0) // Limpiar cualquier fondo anterior
            background.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (dX < 0) { // Swipe a la Izquierda (Borrar)
            background.color = redBackgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            if (deleteIcon != null) {
                val deleteIconTop = itemView.top + (itemHeight - intrinsicDeleteHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicDeleteHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicDeleteWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicDeleteHeight
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon.draw(c)
            }
        } else { // Swipe a la Derecha (Marcar/Desmarcar)
            background.color = greenBackgroundColor
            background.setBounds(
                itemView.left, // Desde el inicio del ítem
                itemView.top,
                itemView.left + dX.toInt(), // Hasta donde llega el swipe
                itemView.bottom
            )
            background.draw(c)

            if (checkIcon != null) {
                val checkIconTop = itemView.top + (itemHeight - intrinsicCheckHeight) / 2
                val checkIconMargin = (itemHeight - intrinsicCheckHeight) / 2
                val checkIconLeft = itemView.left + checkIconMargin
                val checkIconRight = itemView.left + checkIconMargin + intrinsicCheckWidth
                val checkIconBottom = checkIconTop + intrinsicCheckHeight
                checkIcon.setBounds(checkIconLeft, checkIconTop, checkIconRight, checkIconBottom)
                checkIcon.draw(c)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}