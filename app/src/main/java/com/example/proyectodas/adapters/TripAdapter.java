package com.example.proyectodas.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.activities.MainActivity;
import com.example.proyectodas.fragments.InventoryFragment; // Añadido el import
import com.example.proyectodas.items.TripItem;

import java.util.Date;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TravelViewHolder> {

    private List<TripItem> travelList;

    public TripAdapter(List<TripItem> travelList) {
        this.travelList = travelList;
    }

    @Override
    public TravelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new TravelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TravelViewHolder holder, int position) {
        TripItem currentItem = travelList.get(position);

        holder.tvTitle.setText(currentItem.getTitle());
        holder.tvPlace.setText(currentItem.getPlace());

        // Formateador automático según el idioma del móvil
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(holder.itemView.getContext());

        // Convertimos los timestamps a Strings con formato (ej: 14/05/2026 - 20/05/2026)
        String idaStr = dateFormat.format(new java.util.Date(currentItem.getStartDateTimestamp()));
        String vueltaStr = dateFormat.format(new java.util.Date(currentItem.getEndDateTimestamp()));

        holder.tvDate.setText(idaStr + " - " + vueltaStr);

        // CORRECCIÓN: Usar holder.itemView en lugar de itemView
        holder.itemView.setOnClickListener(v -> {
            InventoryFragment inventoryFragment = new InventoryFragment();

            // Configurar los argumentos para pasar al nuevo Fragment
            Bundle bundle = new Bundle();

            /* * NOTA IMPORTANTE: Estoy asumiendo que tu clase TripItem tiene un método getId()
             * proveniente de la base de datos. Si no lo tiene, deberás crearlo, o puedes
             * utilizar temporalmente otro identificador único, como currentItem.dateTimestamp
             */
            bundle.putLong("TRIP_ID", currentItem.getId()); // Usamos currentItem en vez de trip
            inventoryFragment.setArguments(bundle);

            // Navegar usando el método creado en MainActivity
            if (holder.itemView.getContext() instanceof MainActivity) {
                ((MainActivity) holder.itemView.getContext()).replaceFragment(inventoryFragment, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return travelList.size();
    }

    public static class TravelViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPlace, tvDate, tvTripType;

        public TravelViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlace = itemView.findViewById(R.id.tvPlace); // ¡Actualizado!
            tvDate = itemView.findViewById(R.id.tvDate);   // ¡Actualizado!
            tvTripType = itemView.findViewById(R.id.tvTripType);
        }
    }
}