package com.example.proyectodas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.items.InsideItem;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<InsideItem> itemList;

    public ItemAdapter(List<InsideItem> itemList) {
        this.itemList = itemList;
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

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
        }
    }
}