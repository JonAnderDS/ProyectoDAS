package com.example.proyectodas.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.activities.MainActivity;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.fragments.InventoryFragment;
import com.example.proyectodas.fragments.ItemsFragment; // El fragmento del Nivel 2
import com.example.proyectodas.items.LuggageItem;

import java.util.List;

public class LuggageAdapter extends RecyclerView.Adapter<LuggageAdapter.LuggageViewHolder> {

    private List<LuggageItem> luggageList;
    private InventoryFragment parentFragment;
    private Runnable onListChangedListener;

    public LuggageAdapter(List<LuggageItem> luggageList, InventoryFragment parentFragment, Runnable onListChangedListener) {
        this.luggageList = luggageList;
        this.parentFragment = parentFragment;
        this.onListChangedListener = onListChangedListener;
    }

    @NonNull
    @Override
    public LuggageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_luggage, parent, false);
        return new LuggageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LuggageViewHolder holder, int position) {
        LuggageItem currentItem = luggageList.get(position);

        // LÓGICA DEL MENÚ DE 3 PUNTOS
        holder.btnMoreOptions.setOnClickListener(v -> {
            // Creamos el menú emergente anclado al botón
            android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), holder.btnMoreOptions);
            popup.inflate(R.menu.menu_delete);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_delete) {
                    // CREAMOS EL DIÁLOGO DE CONFIRMACIÓN ANTES DE BORRAR
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.titulo_eliminar_equipaje)
                            .setMessage(R.string.mensaje_eliminar_equipaje)
                            .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                                // --- ESTO SOLO SE EJECUTA SI EL USUARIO DICE "SÍ" ---
                                // 1. Borrar de la Base de Datos
                                miDB db = new miDB(v.getContext());
                                db.deleteLuggage(currentItem.getId());

                                // 2. Borrar de la lista visual
                                luggageList.remove(position);

                                // 3. Avisar al adaptador para la animación
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, luggageList.size());

                                if (onListChangedListener != null) {
                                    onListChangedListener.run();
                                }
                            })
                            .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();

                    return true;
                }
                return false;
            });
            popup.show();
        });

        holder.tvLuggageName.setText(currentItem.getName());

        holder.itemView.setOnClickListener(v -> {
            // Comprobamos si el panel derecho existe en la pantalla actual
            View detailContainer = parentFragment.getView().findViewById(R.id.detail_container);

            if (detailContainer != null) {
                // HORIZONTAL: Le decimos al fragmento padre que cargue los items a la derecha
                parentFragment.cargarItemsEnDetalle(currentItem.getId());
            } else {
                // VERTICAL: Navegación clásica a pantalla completa
                ItemsFragment itemsFragment = new ItemsFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("LUGGAGE_ID", currentItem.getId());
                itemsFragment.setArguments(bundle);

                if (v.getContext() instanceof MainActivity) {
                    ((MainActivity) v.getContext()).replaceFragment(itemsFragment, true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return luggageList.size();
    }

    public static class LuggageViewHolder extends RecyclerView.ViewHolder {
        TextView tvLuggageName;
        ImageButton btnMoreOptions;

        public LuggageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLuggageName = itemView.findViewById(R.id.tvLuggageName);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}