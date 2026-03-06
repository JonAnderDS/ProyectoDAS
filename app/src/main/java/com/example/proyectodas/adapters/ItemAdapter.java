package com.example.proyectodas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.items.InsideItem;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<InsideItem> itemList;
    private Runnable onListChangedListener;

    public ItemAdapter(List<InsideItem> itemList, Runnable onListChangedListener) {
        this.itemList = itemList;
        this.onListChangedListener = onListChangedListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inside, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InsideItem currentItem = itemList.get(position);

        holder.btnMoreOptions.setOnClickListener(v -> {
            // Creamos el menú emergente anclado al botón
            android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), holder.btnMoreOptions);
            popup.inflate(R.menu.menu_delete);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_delete) {
                    // CREAMOS EL DIÁLOGO DE CONFIRMACIÓN ANTES DE BORRAR
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.titulo_eliminar_articulo)
                            .setMessage(R.string.mensaje_eliminar_articulo)
                            .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                                // --- ESTO SOLO SE EJECUTA SI EL USUARIO DICE "SÍ" ---
                                // 1. Borrar de la Base de Datos
                                miDB db = new miDB(v.getContext());
                                db.deleteItem(currentItem.getId());

                                // 2. Borrar de la lista visual
                                itemList.remove(position);

                                // 3. Avisar al adaptador para la animación
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, itemList.size());

                                // 4. ¡LA MAGIA! Avisamos al Fragmento de que la lista ha cambiado
                                if (onListChangedListener != null) {
                                    onListChangedListener.run();
                                }
                            })
                            .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                                // Si pulsa cancelar, simplemente cerramos el diálogo sin hacer nada
                                dialog.dismiss();
                            })
                            .show(); // <-- ¡No olvides el .show() para que aparezca en pantalla!

                    return true;
                }
                return false;
            });
            popup.show();
        });
        // Importante: Eliminar listeners previos para evitar bugs al reciclar vistas
        holder.checkBoxItem.setOnCheckedChangeListener(null);

        // Configurar la vista con los datos del objeto
        holder.checkBoxItem.setText(currentItem.getItemName());
        holder.checkBoxItem.setChecked(currentItem.isPacked());

        // Escuchar cambios en el CheckBox para actualizar la BD
        holder.checkBoxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentItem.setPacked(isChecked); // Actualizar objeto en memoria

            // Actualizar en base de datos
            miDB db = new miDB(holder.itemView.getContext());
            db.updateItemPackedStatus(currentItem.getId(), isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxItem;
        ImageButton btnMoreOptions;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}