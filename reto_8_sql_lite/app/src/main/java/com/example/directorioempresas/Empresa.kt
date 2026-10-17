package com.example.directorioempresas

/** Modelo de datos: una fila de la tabla "empresas". */
data class Empresa(
    val id: Long = 0,
    val nombre: String,
    val url: String,
    val telefono: String,
    val email: String,
    val productos: String,
    val clasificacion: String   // Ej: "Consultoría, Fábrica de software"
) {
    companion object {
        val CLASIFICACIONES = listOf(
            "Consultoría",
            "Desarrollo a la medida",
            "Fábrica de software"
        )
    }
}
