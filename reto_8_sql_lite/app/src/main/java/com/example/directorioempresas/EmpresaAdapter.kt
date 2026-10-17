package com.example.directorioempresas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.directorioempresas.databinding.ItemEmpresaBinding

/** Conecta la lista de empresas con el RecyclerView. */
class EmpresaAdapter(
    private var items: List<Empresa>,
    private val onEditar: (Empresa) -> Unit,
    private val onEliminar: (Empresa) -> Unit
) : RecyclerView.Adapter<EmpresaAdapter.VH>() {

    inner class VH(val b: ItemEmpresaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEmpresaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        with(holder.b) {
            tvNombre.text = e.nombre
            tvClasificacion.text = e.clasificacion
            tvContacto.text = "${e.telefono}  •  ${e.email}"
            tvUrl.text = e.url
            tvProductos.text = e.productos
            root.setOnClickListener { onEditar(e) }
            btnEliminar.setOnClickListener { onEliminar(e) }
        }
    }

    fun actualizar(nuevos: List<Empresa>) {
        items = nuevos
        notifyDataSetChanged()
    }
}
