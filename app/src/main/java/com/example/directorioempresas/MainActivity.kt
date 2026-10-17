package com.example.directorioempresas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.directorioempresas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: EmpresaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        // RecyclerView
        adapter = EmpresaAdapter(emptyList(), ::abrirEdicion, ::confirmarEliminacion)
        binding.rvEmpresas.layoutManager = LinearLayoutManager(this)
        binding.rvEmpresas.adapter = adapter

        // Spinner de clasificación ("Todas" + 3 opciones)
        val opciones = listOf("Todas") + Empresa.CLASIFICACIONES
        binding.spClasificacion.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        binding.spClasificacion.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) =
                    cargarEmpresas()
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }

        // Filtro por nombre mientras se escribe
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = cargarEmpresas()
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        // Botón flotante: crear empresa
        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, EmpresaFormActivity::class.java))
        }
    }

    /** Se refresca la lista cada vez que se vuelve del formulario. */
    override fun onResume() {
        super.onResume()
        cargarEmpresas()
    }

    private fun cargarEmpresas() {
        val nombre = binding.etBuscar.text.toString()
        val clasif = if (binding.spClasificacion.selectedItemPosition == 0) ""
        else binding.spClasificacion.selectedItem.toString()

        val lista = db.listar(nombre, clasif)
        adapter.actualizar(lista)
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun abrirEdicion(e: Empresa) {
        val intent = Intent(this, EmpresaFormActivity::class.java)
        intent.putExtra(EmpresaFormActivity.EXTRA_ID, e.id)
        startActivity(intent)
    }

    /** Mensaje de confirmación antes de eliminar. */
    private fun confirmarEliminacion(e: Empresa) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar empresa")
            .setMessage("¿Está seguro de que desea eliminar \"${e.nombre}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.eliminar(e.id)
                Toast.makeText(this, "Empresa eliminada", Toast.LENGTH_SHORT).show()
                cargarEmpresas()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
