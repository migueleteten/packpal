package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

import androidx.annotation.DrawableRes // Para el ID del recurso drawable

data class CategoryInfo(
    val internalName: String, // El que guardaremos en la BD (ej: "CLOTHING")
    val displayName: String,  // El que verá el usuario (ej: "Ropa")
    @DrawableRes val iconResId: Int // El ID del recurso del icono (ej: R.drawable.ic_category_clothing)
)