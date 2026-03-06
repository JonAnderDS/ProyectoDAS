package com.example.proyectodas.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.adapters.ItemAdapter;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.items.InsideItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ItemsFragment extends Fragment {

    private long luggageId = -1;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<InsideItem> itemList;
    private TextView tvEmptyState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            luggageId = getArguments().getLong("LUGGAGE_ID", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Si estamos en horizontal Y este fragmento no está dentro del contenedor pequeño...
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape && container != null && container.getId() != R.id.detail_container) {
            // Cerramos este fragmento para volver al InventoryFragment (que ahora mostrará las dos pantallas)
            requireActivity().getSupportFragmentManager().popBackStack();
            return null; // Detenemos la carga visual de este fragmento
        }
        return inflater.inflate(R.layout.fragment_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            luggageId = getArguments().getLong("LUGGAGE_ID", -1);
        }

        recyclerView = view.findViewById(R.id.recyclerItems);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAddItem = view.findViewById(R.id.fabAddItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Si luggageId es -1 (no hay maletas seleccionadas), forzamos la lista vacía
        if (luggageId == -1) {
            fabAddItem.setVisibility(View.GONE); // Ocultamos el botón de añadir objetos
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(getString(R.string.empty_luggage)); // Mensaje de "Selecciona una maleta"
            return;
        }

        miDB db = new miDB(requireContext());
        itemList = db.getItemsForLuggage(luggageId);

        adapter = new ItemAdapter(itemList, () -> checkEmptyState());
        recyclerView.setAdapter(adapter);

        checkEmptyState();

        fabAddItem.setOnClickListener(v -> showAddItemDialog());
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

        EditText editItemName = view.findViewById(R.id.editItemName);

        builder.setView(view)
                .setTitle("Añadir Artículo")
                .setPositiveButton(getString(R.string.anadir), null)
                .setNegativeButton(getString(R.string.cancelar), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String itemName = editItemName.getText().toString().trim();

            if (itemName.isEmpty()) {
                editItemName.setError("Por favor, introduce un nombre");
            } else {
                // Por defecto, al añadir un artículo, no está guardado (isPacked = false)
                InsideItem newItem = new InsideItem(itemName, luggageId, false);

                miDB db = new miDB(requireContext());
                long newId = db.addItem(newItem); // Guarda en DB y devuelve el ID generado
                newItem.setId(newId); // Asignamos el ID real al objeto


                itemList.add(newItem);
                adapter.notifyItemInserted(itemList.size() - 1);
                checkEmptyState();

                dialog.dismiss();
            }
        });
    }
    private void checkEmptyState() {
        if (itemList == null || itemList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(getString(R.string.empty_items));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}