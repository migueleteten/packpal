package com.dfmiguel.gopack // O tu paquete actual

// Definición de las data classes (puedes ponerlas en este mismo archivo o en uno separado si prefieres)
data class TemplateItem(
    val name: String,
    val categoryInternalName: String, // Usaremos el internalName de AppCategories
    val quantity: Int = 1,
    val isChecked: Boolean = false // Las plantillas empiezan con ítems no chequeados
)

data class PackingListTemplate(
    val templateName: String, // Nombre que verá el usuario, ej: "Fin de Semana Urbano"
    val description: String, // Breve descripción de la plantilla
    val items: List<TemplateItem>
)

object AppTemplates {

    val list: List<PackingListTemplate> = listOf(
        PackingListTemplate(
            templateName = "Escapada Fin de Semana (Urbana)",
            description = "Lo esencial para 2-3 días de ciudad y cultura.",
            items = listOf(
                // Ropa
                TemplateItem("Camisetas/Tops", "CLOTHING", 3),
                TemplateItem("Pantalón cómodo (ej: vaqueros)", "CLOTHING", 1),
                TemplateItem("Pantalón versátil (ej: chinos)", "CLOTHING", 1),
                TemplateItem("Ropa interior", "CLOTHING", 3),
                TemplateItem("Calcetines", "CLOTHING", 3),
                TemplateItem("Jersey o chaqueta ligera", "CLOTHING", 1),
                TemplateItem("Pijama", "CLOTHING", 1),
                TemplateItem("Conjunto algo más arreglado (opc.)", "CLOTHING", 1),
                // Aseo / Higiene
                TemplateItem("Cepillo de dientes", "TOILETRIES", 1),
                TemplateItem("Pasta de dientes (viaje)", "TOILETRIES", 1),
                TemplateItem("Desodorante", "TOILETRIES", 1),
                TemplateItem("Champú (viaje)", "TOILETRIES", 1),
                TemplateItem("Gel de ducha (viaje)", "TOILETRIES", 1),
                TemplateItem("Peine/Cepillo pelo", "TOILETRIES", 1),
                TemplateItem("Maquillaje básico (si aplica)", "TOILETRIES", 1),
                TemplateItem("Colonia/Perfume (pequeño)", "TOILETRIES", 1),
                // Electrónica
                TemplateItem("Móvil", "ELECTRONICS", 1),
                TemplateItem("Cargador de móvil", "ELECTRONICS", 1),
                TemplateItem("Power bank (batería externa)", "ELECTRONICS", 1),
                TemplateItem("Auriculares", "ELECTRONICS", 1),
                TemplateItem("Adaptador de enchufe (si es int.)", "ELECTRONICS", 1),
                // Documentos
                TemplateItem("DNI / Pasaporte", "DOCUMENTS", 1),
                TemplateItem("Tarjeta Sanitaria / Seguro Viaje", "DOCUMENTS", 1),
                TemplateItem("Billetes/Reservas (impreso/digital)", "DOCUMENTS", 1),
                TemplateItem("Tarjeta de crédito/débito", "DOCUMENTS", 1),
                TemplateItem("Efectivo", "DOCUMENTS", 1),
                // Calzado
                TemplateItem("Zapatillas cómodas (puestas)", "FOOTWEAR", 1),
                TemplateItem("Calzado extra (opc.)", "FOOTWEAR", 1),
                // Botiquín / Salud
                TemplateItem("Analgésicos (Paracetamol/Ibuprofeno)", "HEALTH", 1),
                TemplateItem("Tiritas", "HEALTH", 5),
                TemplateItem("Medicación personal", "HEALTH", 1),
                // Accesorios
                TemplateItem("Gafas de sol (si aplica)", "ACCESSORIES", 1),
                TemplateItem("Reloj", "ACCESSORIES", 1),
                // Otros
                TemplateItem("Botella de agua reutilizable", "OTHER", 1),
                TemplateItem("Paraguas plegable (según clima)", "OTHER", 1),
                TemplateItem("Bolsa pequeña / Mochila de día", "OTHER", 1)
            )
        ),
        PackingListTemplate(
            templateName = "Viaje de Playa Familiar (5-7 días)",
            description = "Todo para disfrutar del sol, la arena y el mar en familia.",
            items = listOf(
                // Ropa
                TemplateItem("Bañadores Adulto", "CLOTHING", 3),
                TemplateItem("Bañadores Niño/Bebé", "KIDS_BABIES", 4),
                TemplateItem("Camisetas/Tops Adulto", "CLOTHING", 7),
                TemplateItem("Camisetas Niño/Bebé", "KIDS_BABIES", 8),
                TemplateItem("Pantalones cortos/Faldas Adulto", "CLOTHING", 4),
                TemplateItem("Pantalones cortos Niño/Bebé", "KIDS_BABIES", 5),
                TemplateItem("Ropa interior Adulto", "CLOTHING", 7),
                TemplateItem("Ropa interior Niño", "KIDS_BABIES", 8),
                TemplateItem("Pañales (si aplica)", "KIDS_BABIES", 20), // Ajustar según necesidad
                TemplateItem("Pijama ligero Adulto", "CLOTHING", 2),
                TemplateItem("Pijama Niño/Bebé", "KIDS_BABIES", 2),
                TemplateItem("Vestido/Kaftan playa Adulto", "CLOTHING", 2),
                TemplateItem("Chaqueta fina (noche/brisa)", "CLOTHING", 1),
                // Aseo / Higiene
                TemplateItem("Protección solar ALTA (familiar)", "TOILETRIES", 1),
                TemplateItem("Protección solar INFANTIL", "KIDS_BABIES", 1),
                TemplateItem("Aftersun / Aloe Vera", "TOILETRIES", 1),
                TemplateItem("Repelente de insectos (familiar)", "TOILETRIES", 1),
                TemplateItem("Kit aseo básico (cepillos, pasta...)", "TOILETRIES", 1),
                TemplateItem("Toallitas húmedas bebé", "KIDS_BABIES", 1),
                // Electrónica
                TemplateItem("Móviles y cargadores", "ELECTRONICS", 2), // Para los adultos
                TemplateItem("Cámara (opc. acuática)", "ELECTRONICS", 1),
                // Documentos
                TemplateItem("DNI/Pasaportes (todos)", "DOCUMENTS", 1),
                TemplateItem("Libro de Familia (opc. si viajas solo con niños)", "DOCUMENTS", 1),
                TemplateItem("Tarjetas Sanitarias (todos)", "DOCUMENTS", 1),
                TemplateItem("Reservas", "DOCUMENTS", 1),
                // Calzado
                TemplateItem("Chanclas/Sandalias Adulto", "FOOTWEAR", 2),
                TemplateItem("Chanclas/Sandalias Niño/Bebé", "KIDS_BABIES", 2),
                TemplateItem("Zapatillas cómodas Adulto", "FOOTWEAR", 1),
                TemplateItem("Zapatillas Niño", "KIDS_BABIES", 1),
                // Botiquín / Salud
                TemplateItem("Botiquín familiar completo", "HEALTH", 1),
                TemplateItem("Crema para picaduras", "HEALTH", 1),
                TemplateItem("Medicación personal (todos)", "HEALTH", 1),
                TemplateItem("Termómetro", "HEALTH", 1),
                // Accesorios
                TemplateItem("Gafas de sol (todos)", "ACCESSORIES", 1),
                TemplateItem("Gorras/Sombreros (todos)", "ACCESSORIES", 1),
                TemplateItem("Toallas de playa grandes", "ACCESSORIES", 3), // Ajustar
                TemplateItem("Bolsa de playa grande", "ACCESSORIES", 1),
                // Niños / Bebés
                TemplateItem("Juguetes de playa (cubo, pala, etc.)", "KIDS_BABIES", 1),
                TemplateItem("Manguitos/Flotador (si aplica)", "KIDS_BABIES", 1),
                TemplateItem("Carrito de bebé plegable (si aplica)", "KIDS_BABIES", 1),
                TemplateItem("Biberones/Chupetes (si aplica)", "KIDS_BABIES", 1),
                TemplateItem("Comida/Leche bebé (primeros días)", "FOOD", 1),
                // Otros
                TemplateItem("Libros/Revistas/Juegos de viaje", "OTHER", 1)
            )
        ),
        PackingListTemplate(
            templateName = "Viaje de Negocios (1-3 días)",
            description = "Eficiencia y profesionalidad para tus reuniones.",
            items = listOf(
                // Ropa
                TemplateItem("Traje completo o conjunto formal", "CLOTHING", 1),
                TemplateItem("Camisa/Blusa de vestir", "CLOTHING", 3),
                TemplateItem("Corbata/Pañuelo de seda", "ACCESSORIES", 2),
                TemplateItem("Ropa interior", "CLOTHING", 3),
                TemplateItem("Calcetines de vestir", "CLOTHING", 3),
                TemplateItem("Pijama", "CLOTHING", 1),
                TemplateItem("Ropa cómoda (para hotel/viaje)", "CLOTHING", 1),
                // Calzado
                TemplateItem("Zapatos de vestir", "FOOTWEAR", 1),
                TemplateItem("Zapatos cómodos (opc. para viajar)", "FOOTWEAR", 1),
                // Aseo / Higiene
                TemplateItem("Neceser de aseo (miniaturas)", "TOILETRIES", 1),
                TemplateItem("Kit de afeitado / Maquillaje profesional", "TOILETRIES", 1),
                TemplateItem("Peine/Cepillo", "TOILETRIES", 1),
                TemplateItem("Perfume/Colonia (discreto)", "TOILETRIES", 1),
                // Electrónica
                TemplateItem("Portátil y cargador", "ELECTRONICS", 1),
                TemplateItem("Móvil y cargador", "ELECTRONICS", 1),
                TemplateItem("Power bank", "ELECTRONICS", 1),
                TemplateItem("Adaptador universal (si es int.)", "ELECTRONICS", 1),
                TemplateItem("Auriculares (con micrófono)", "ELECTRONICS", 1),
                TemplateItem("Pen drive/Puntero láser (opc.)", "ELECTRONICS", 1),
                // Documentos
                TemplateItem("DNI/Pasaporte", "DOCUMENTS", 1),
                TemplateItem("Billetes de transporte", "DOCUMENTS", 1),
                TemplateItem("Reserva de hotel", "DOCUMENTS", 1),
                TemplateItem("Tarjetas de visita", "DOCUMENTS", 1),
                TemplateItem("Documentación de trabajo/Presentación", "DOCUMENTS", 1),
                TemplateItem("Tarjeta de crédito de empresa", "DOCUMENTS", 1),
                // Botiquín / Salud
                TemplateItem("Analgésicos/Antiacidos", "HEALTH", 1),
                TemplateItem("Medicación personal", "HEALTH", 1),
                // Otros
                TemplateItem("Bolígrafo de calidad y libreta", "OTHER", 1),
                TemplateItem("Botella de agua reutilizable", "OTHER", 1)
            )
        ),
        PackingListTemplate(
            templateName = "Mochilero / Aventura (7-15 días)",
            description = "Ligero, versátil y listo para la aventura.",
            items = listOf(
                // Ropa
                TemplateItem("Camisetas técnicas/secado rápido", "CLOTHING", 4),
                TemplateItem("Pantalón trekking convertible", "CLOTHING", 1),
                TemplateItem("Pantalón ligero/mallas", "CLOTHING", 1),
                TemplateItem("Forro polar o capa intermedia", "CLOTHING", 1),
                TemplateItem("Chubasquero/Cortavientos (ligero, plegable)", "CLOTHING", 1),
                TemplateItem("Ropa interior técnica", "CLOTHING", 5),
                TemplateItem("Calcetines trekking/térmicos", "CLOTHING", 5),
                TemplateItem("Bañador (secado rápido)", "CLOTHING", 1),
                TemplateItem("Pañuelo multiusos/buff", "ACCESSORIES", 1),
                // Calzado
                TemplateItem("Botas trekking o zapatillas trail (puestas)", "FOOTWEAR", 1),
                TemplateItem("Sandalias ligeras/chanclas", "FOOTWEAR", 1),
                // Aseo / Higiene
                TemplateItem("Jabón/Champú sólido", "TOILETRIES", 1),
                TemplateItem("Toalla microfibra secado rápido", "TOILETRIES", 1),
                TemplateItem("Cepillo dientes (viaje) y pasta", "TOILETRIES", 1),
                TemplateItem("Desodorante (pequeño/sólido)", "TOILETRIES", 1),
                TemplateItem("Protección solar", "TOILETRIES", 1),
                TemplateItem("Repelente insectos (según destino)", "TOILETRIES", 1),
                // Electrónica
                TemplateItem("Móvil y cargador", "ELECTRONICS", 1),
                TemplateItem("Power bank (alta capacidad)", "ELECTRONICS", 1),
                TemplateItem("Adaptador universal", "ELECTRONICS", 1),
                TemplateItem("Auriculares", "ELECTRONICS", 1),
                TemplateItem("Frontal o linterna pequeña", "ELECTRONICS", 1),
                // Documentos
                TemplateItem("Pasaporte/DNI (+ copias digitales)", "DOCUMENTS", 1),
                TemplateItem("Seguro de viaje aventura", "DOCUMENTS", 1),
                TemplateItem("Visados (si necesarios)", "DOCUMENTS", 1),
                TemplateItem("Billetes y reservas", "DOCUMENTS", 1),
                TemplateItem("Tarjetas crédito/débito (varias)", "DOCUMENTS", 1),
                TemplateItem("Efectivo local", "DOCUMENTS", 1),
                // Botiquín / Salud
                TemplateItem("Kit primeros auxilios completo", "HEALTH", 1),
                TemplateItem("Medicación personal específica", "HEALTH", 1),
                TemplateItem("Sales rehidratación", "HEALTH", 1),
                TemplateItem("Potabilizador agua (opc.)", "HEALTH", 1),
                // Accesorios
                TemplateItem("Mochila principal (40-60L)", "ACCESSORIES", 1),
                TemplateItem("Mochila día plegable (15-25L)", "ACCESSORIES", 1),
                TemplateItem("Candado(s) pequeño(s)", "ACCESSORIES", 2),
                TemplateItem("Gafas de sol", "ACCESSORIES", 1),
                TemplateItem("Gorra/Sombrero", "ACCESSORIES", 1),
                TemplateItem("Saco de dormir sábana (opc.)", "ACCESSORIES", 1),
                // Otros
                TemplateItem("Botella agua reutilizable (con filtro opc.)", "OTHER", 1),
                TemplateItem("Cuerda fina y pinzas", "OTHER", 1),
                TemplateItem("Navaja multiusos (revisar normativa)", "OTHER", 1),
                TemplateItem("Bolsas estancas/zip (para organizar/proteger)", "OTHER", 3)
            )
        ),
        PackingListTemplate(
            templateName = "Estancia Larga / Erasmus (Meses)",
            description = "Lo clave para empezar tu aventura de estudio en el extranjero.",
            items = listOf(
                // Ropa (suficiente para 1-2 semanas, se lavará)
                TemplateItem("Ropa interior", "CLOTHING", 14),
                TemplateItem("Calcetines", "CLOTHING", 14),
                TemplateItem("Camisetas/Tops variados", "CLOTHING", 10),
                TemplateItem("Pantalones/Vaqueros/Faldas", "CLOTHING", 5),
                TemplateItem("Jerséis/Sudaderas", "CLOTHING", 4),
                TemplateItem("Abrigo principal (adecuado al clima)", "CLOTHING", 1),
                TemplateItem("Chaqueta ligera/impermeable", "CLOTHING", 1),
                TemplateItem("Ropa de deporte (si aplica)", "CLOTHING", 2),
                TemplateItem("Pijama", "CLOTHING", 2),
                TemplateItem("Conjunto formal (presentaciones/eventos)", "CLOTHING", 1),
                TemplateItem("Ropa de estar por casa cómoda", "CLOTHING", 2),
                // Calzado
                TemplateItem("Zapatillas cómodas día a día", "FOOTWEAR", 2),
                TemplateItem("Calzado para lluvia/frío (según destino)", "FOOTWEAR", 1),
                TemplateItem("Zapatos formales (opc.)", "FOOTWEAR", 1),
                TemplateItem("Chanclas (ducha/casa)", "FOOTWEAR", 1),
                // Aseo / Higiene
                TemplateItem("Neceser con esenciales (primeros días)", "TOILETRIES", 1),
                TemplateItem("Toalla de ducha (si no se provee)", "ACCESSORIES", 1),
                // Electrónica
                TemplateItem("Portátil y cargador", "ELECTRONICS", 1),
                TemplateItem("Móvil y cargador", "ELECTRONICS", 1),
                TemplateItem("Adaptador(es) de enchufe país destino", "ELECTRONICS", 2),
                TemplateItem("Power bank", "ELECTRONICS", 1),
                TemplateItem("Disco duro externo/USB grande", "ELECTRONICS", 1),
                TemplateItem("Auriculares", "ELECTRONICS", 1),
                // Documentos (¡MUY IMPORTANTE!)
                TemplateItem("Pasaporte/DNI (vigencia larga)", "DOCUMENTS", 1),
                TemplateItem("Visado de estudiante (si aplica)", "DOCUMENTS", 1),
                TemplateItem("Carta de aceptación universidad", "DOCUMENTS", 1),
                TemplateItem("Seguro médico internacional / TSE", "DOCUMENTS", 1),
                TemplateItem("Billetes de avión/tren (ida)", "DOCUMENTS", 1),
                TemplateItem("Confirmación alojamiento (primeras semanas)", "DOCUMENTS", 1),
                TemplateItem("Fotos de carnet (varias)", "DOCUMENTS", 1),
                TemplateItem("Expediente académico/Títulos (copias)", "DOCUMENTS", 1),
                TemplateItem("Tarjetas bancarias (avisar al banco del viaje)", "DOCUMENTS", 1),
                TemplateItem("Lista de contactos de emergencia", "DOCUMENTS", 1),
                // Botiquín / Salud
                TemplateItem("Medicación personal (suministro inicial amplio con recetas)", "HEALTH", 1),
                TemplateItem("Termómetro", "HEALTH", 1),
                TemplateItem("Analgésicos, tiritas, antiséptico", "HEALTH", 1),
                // Otros / Estudio / Personal
                TemplateItem("Mochila para universidad/día", "ACCESSORIES", 1),
                TemplateItem("Material de escritura básico", "OTHER", 1),
                TemplateItem("Candado para taquilla/maleta", "ACCESSORIES", 1),
                TemplateItem("Pequeños objetos personales/fotos", "OTHER", 1),
                TemplateItem("Diccionario de bolsillo (opc.)", "OTHER", 1),
                TemplateItem("Regalo pequeño para anfitriones (opc.)", "OTHER", 1)
            )
        )
    )
}