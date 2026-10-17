package com.example.directorioempresas

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Crea la base de datos SQLite y expone las operaciones CRUD.
 * El archivo se crea automáticamente en la primera llamada a
 * readableDatabase / writableDatabase.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "directorio.db"
        const val DB_VERSION = 1

        const val TABLA = "empresas"
        const val COL_ID = "id"
        const val COL_NOMBRE = "nombre"
        const val COL_URL = "url"
        const val COL_TELEFONO = "telefono"
        const val COL_EMAIL = "email"
        const val COL_PRODUCTOS = "productos"
        const val COL_CLASIFICACION = "clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_URL TEXT,
                $COL_TELEFONO TEXT,
                $COL_EMAIL TEXT,
                $COL_PRODUCTOS TEXT,
                $COL_CLASIFICACION TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA")
        onCreate(db)
    }

    private fun toValues(e: Empresa) = ContentValues().apply {
        put(COL_NOMBRE, e.nombre)
        put(COL_URL, e.url)
        put(COL_TELEFONO, e.telefono)
        put(COL_EMAIL, e.email)
        put(COL_PRODUCTOS, e.productos)
        put(COL_CLASIFICACION, e.clasificacion)
    }

    private fun fromCursor(c: Cursor) = Empresa(
        id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
        nombre = c.getString(c.getColumnIndexOrThrow(COL_NOMBRE)),
        url = c.getString(c.getColumnIndexOrThrow(COL_URL)) ?: "",
        telefono = c.getString(c.getColumnIndexOrThrow(COL_TELEFONO)) ?: "",
        email = c.getString(c.getColumnIndexOrThrow(COL_EMAIL)) ?: "",
        productos = c.getString(c.getColumnIndexOrThrow(COL_PRODUCTOS)) ?: "",
        clasificacion = c.getString(c.getColumnIndexOrThrow(COL_CLASIFICACION)) ?: ""
    )

    // ---------- CREATE ----------
    fun insertar(e: Empresa): Long =
        writableDatabase.insert(TABLA, null, toValues(e))

    // ---------- RETRIEVE ----------
    fun obtenerPorId(id: Long): Empresa? {
        readableDatabase.query(
            TABLA, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null
        ).use { c ->
            return if (c.moveToFirst()) fromCursor(c) else null
        }
    }

    /** Lista todas las empresas; filtra por nombre y/o clasificación si se indican. */
    fun listar(nombre: String = "", clasificacion: String = ""): List<Empresa> {
        val condiciones = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (nombre.isNotBlank()) {
            condiciones.add("$COL_NOMBRE LIKE ?")
            args.add("%${nombre.trim()}%")
        }
        if (clasificacion.isNotBlank()) {
            condiciones.add("$COL_CLASIFICACION LIKE ?")
            args.add("%$clasificacion%")
        }
        val where = if (condiciones.isEmpty()) null else condiciones.joinToString(" AND ")

        val lista = mutableListOf<Empresa>()
        readableDatabase.query(
            TABLA, null, where, if (args.isEmpty()) null else args.toTypedArray(),
            null, null, "$COL_NOMBRE COLLATE NOCASE ASC"
        ).use { c ->
            while (c.moveToNext()) lista.add(fromCursor(c))
        }
        return lista
    }

    // ---------- UPDATE ----------
    fun actualizar(e: Empresa): Int =
        writableDatabase.update(TABLA, toValues(e), "$COL_ID = ?", arrayOf(e.id.toString()))

    // ---------- DELETE ----------
    fun eliminar(id: Long): Int =
        writableDatabase.delete(TABLA, "$COL_ID = ?", arrayOf(id.toString()))
}
