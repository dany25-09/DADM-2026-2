package com.example.directorioempresas

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.directorioempresas.databinding.ActivityEmpresaFormBinding

/** Formulario único para CREAR (sin extra) y EDITAR (con EXTRA_ID). */
class EmpresaFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "empresa_id"
    }

    private lateinit var binding: ActivityEmpresaFormBinding
    private lateinit var db: DatabaseHelper
    private var empresaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        empresaId = intent.getLongExtra(EXTRA_ID, -1)

        if (empresaId != -1L) {
            binding.tvTitulo.text = "Editar empresa"
            db.obtenerPorId(empresaId)?.let { llenarFormulario(it) }
        } else {
            binding.tvTitulo.text = "Nueva empresa"
        }

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun llenarFormulario(e: Empresa) {
        binding.etNombre.setText(e.nombre)
        binding.etUrl.setText(e.url)
        binding.etTelefono.setText(e.telefono)
        binding.etEmail.setText(e.email)
        binding.etProductos.setText(e.productos)
        binding.cbConsultoria.isChecked = e.clasificacion.contains(Empresa.CLASIFICACIONES[0])
        binding.cbMedida.isChecked = e.clasificacion.contains(Empresa.CLASIFICACIONES[1])
        binding.cbFabrica.isChecked = e.clasificacion.contains(Empresa.CLASIFICACIONES[2])
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val url = binding.etUrl.text.toString().trim()
        val tel = binding.etTelefono.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val productos = binding.etProductos.text.toString().trim()

        val clasif = listOfNotNull(
            if (binding.cbConsultoria.isChecked) Empresa.CLASIFICACIONES[0] else null,
            if (binding.cbMedida.isChecked) Empresa.CLASIFICACIONES[1] else null,
            if (binding.cbFabrica.isChecked) Empresa.CLASIFICACIONES[2] else null
        ).joinToString(", ")

        // Validaciones
        if (nombre.isEmpty()) { binding.etNombre.error = "Ingrese el nombre"; return }
        if (url.isNotEmpty() && !Patterns.WEB_URL.matcher(url).matches()) {
            binding.etUrl.error = "URL no válida"; return
        }
        if (tel.isNotEmpty() && !Patterns.PHONE.matcher(tel).matches()) {
            binding.etTelefono.error = "Teléfono no válido"; return
        }
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email no válido"; return
        }
        if (clasif.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos una clasificación", Toast.LENGTH_SHORT).show()
            return
        }

        val empresa = Empresa(
            id = if (empresaId == -1L) 0 else empresaId,
            nombre = nombre, url = url, telefono = tel,
            email = email, productos = productos, clasificacion = clasif
        )

        if (empresaId == -1L) {
            db.insertar(empresa)
            Toast.makeText(this, "Empresa creada", Toast.LENGTH_SHORT).show()
        } else {
            db.actualizar(empresa)
            Toast.makeText(this, "Empresa actualizada", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
