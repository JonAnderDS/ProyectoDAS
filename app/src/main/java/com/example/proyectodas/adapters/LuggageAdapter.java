package com.example.proyectodas.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.activities.MainActivity;
import com.example.proyectodas.fragments.InventoryFragment;
import com.example.proyectodas.fragments.ItemsFragment; // El fragmento del Nivel 2
import com.example.proyectodas.items.LuggageItem;

import java.util.List;

public class LuggageAdapter extends RecyclerView.Adapter<LuggageAdapter.LuggageViewHolder> {

    private List<LuggageItem> luggageList;
    private InventoryFragment parentFragment;

    public LuggageAdapter(List<LuggageItem> luggageList, InventoryFragment parentFragment) {
        this.luggageList = luggageList;
        this.parentFragment = parentFragment;
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

        public LuggageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLuggageName = itemView.findViewById(R.id.tvLuggageName);
        }
    }
}