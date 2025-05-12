package com.dfmiguel.packpal // Asegúrate que coincida con tu paquete

// No olvides importar R si es necesario, aunque dentro del mismo módulo debería encontrarlo.
// import com.dfmiguel.packpal.R // Descomenta si es necesario (Android Studio suele autocompletarlo)

object AppCategories {

    val list: List<CategoryInfo> = listOf(
        CategoryInfo(internalName = "CLOTHING", displayName = "Ropa", iconResId = R.drawable.ic_category_clothing),
        CategoryInfo(internalName = "TOILETRIES", displayName = "Aseo / Higiene", iconResId = R.drawable.ic_category_toiletries),
        CategoryInfo(internalName = "ELECTRONICS", displayName = "Electrónica", iconResId = R.drawable.ic_category_electronics),
        CategoryInfo(internalName = "DOCUMENTS", displayName = "Documentos", iconResId = R.drawable.ic_category_documents),
        CategoryInfo(internalName = "FOOTWEAR", displayName = "Calzado", iconResId = R.drawable.ic_category_footwear),
        CategoryInfo(internalName = "HEALTH", displayName = "Botiquín / Salud", iconResId = R.drawable.ic_category_health),
        CategoryInfo(internalName = "ACCESSORIES", displayName = "Accesorios", iconResId = R.drawable.ic_category_accessories),
        CategoryInfo(internalName = "FOOD", displayName = "Comida / Snacks", iconResId = R.drawable.ic_category_food),
        CategoryInfo(internalName = "KIDS_BABIES", displayName = "Niños / Bebés", iconResId = R.drawable.ic_category_kids),
        CategoryInfo(internalName = "OTHER", displayName = "Otros / General", iconResId = R.drawable.ic_category_other)
        // Puedes añadir más aquí si quieres en el futuro
    )

    // Función de utilidad para obtener una CategoryInfo por su internalName
    fun findByInternalName(internalName: String?): CategoryInfo? {
        return list.find { it.internalName.equals(internalName, ignoreCase = true) }
    }

    // Función de utilidad para obtener el icono por su internalName (o un icono por defecto si no se encuentra)
    fun getIconResIdForCategory(internalName: String?): Int {
        return findByInternalName(internalName)?.iconResId ?: R.drawable.ic_category_other // Devuelve 'Otros' si no se encuentra
    }

    // Función de utilidad para obtener el displayName por su internalName
    fun getDisplayNameForCategory(internalName: String?): String {
        return findByInternalName(internalName)?.displayName ?: internalName ?: "General"
    }
}