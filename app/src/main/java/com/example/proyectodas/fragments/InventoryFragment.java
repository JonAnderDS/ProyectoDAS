package com.example.proyectodas.fragments;

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
import com.example.proyectodas.adapters.LuggageAdapter;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.items.LuggageItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class InventoryFragment extends Fragment {

    private long tripId = -1;
    private RecyclerView recyclerView;
    private LuggageAdapter adapter;
    private List<LuggageItem> luggageList;
    private TextView tvEmptyState;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recuperamos el ID del viaje que nos pasó el TripAdapter
        if (getArguments() != null) {
            tripId = getArguments().getLong("TRIP_ID", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflamos el layout correspondiente a este fragmento
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getLong("TRIP_ID", -1);
        }

        recyclerView = view.findViewById(R.id.recyclerLuggage);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        miDB db = new miDB(requireContext());
        luggageList = db.getLuggageForTrip(tripId);

        // Al adaptador le pasamos "this" para que sepa si estamos en horizontal
        adapter = new LuggageAdapter(luggageList, this);
        recyclerView.setAdapter(adapter);

        checkEmptyState();

        // ----------------------------------------------------
        // LÓGICA MODO HORIZONTAL (MASTER-DETAIL)
        // ----------------------------------------------------
        if (view.findViewById(R.id.detail_container) != null) {
            // Si el contenedor derecho existe, cargamos el fragmento de items dentro
            long initialLuggageId = luggageList.isEmpty() ? -1 : luggageList.get(0).getId();
            cargarItemsEnDetalle(initialLuggageId);
        }

        FloatingActionButton fabAddLuggage = view.findViewById(R.id.fabAddLuggage);
        fabAddLuggage.setOnClickListener(v -> showAddLuggageDialog());

    }

    /**
     * Muestra un diálogo para añadir un nuevo equipaje asegurando que
     * no se cierre si el usuario deja el campo en blanco.
     */
    private void showAddLuggageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_luggage, null);

        EditText editLuggageName = view.findViewById(R.id.editLuggageName);

        String tituloAnadir = getString(R.string.anadir); // Asegúrate de tener esto en strings.xml o pon "Añadir"
        String cancelar = getString(R.string.cancelar); // Asegúrate de tener esto en strings.xml o pon "Cancelar"

        // Configuramos los botones temporalmente con 'null' para sobreescribir su comportamiento después
        builder.setView(view)
                .setTitle("Añadir Equipaje")
                .setPositiveButton(tituloAnadir, null)
                .setNegativeButton(cancelar, null);

        // Creamos y mostramos el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobreescribimos el comportamiento del botón de Aceptar para validar los datos
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String luggageName = editLuggageName.getText().toString().trim();

            if (luggageName.isEmpty()) {
                // Si está vacío, mostramos el error y el diálogo NO se cierra
                editLuggageName.setError("El nombre no puede estar vacío");
            } else {
                // 1. Crear el objeto (Aún sin ID definitivo de BD)
                LuggageItem newLuggage = new LuggageItem(luggageName, tripId);

                // 2. Guardarlo en la Base de Datos
                miDB db = new miDB(requireContext());
                long newId = db.addLuggage(newLuggage); // Debes crear este método en miDB

                // Le asignamos el ID real que nos devuelve SQLite
                newLuggage.setId(newId);

                // 3. Actualizar la interfaz (RecyclerView)
                luggageList.add(newLuggage);
                adapter.notifyItemInserted(luggageList.size() - 1);
                checkEmptyState();

                // 4. Cerrar el diálogo manualmente ya que todo fue correcto
                dialog.dismiss();
            }
        });
    }

    private void checkEmptyState() {
        if (luggageList.isEmpty()) { // Cambia tripList por luggageList o itemList según el fragmento
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    public void cargarItemsEnDetalle(long luggageId) {
        if (getView() != null && getView().findViewById(R.id.detail_container) != null) {
            ItemsFragment itemsFrag = new ItemsFragment();
            Bundle args = new Bundle();
            args.putLong("LUGGAGE_ID", luggageId);
            itemsFrag.setArguments(args);

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, itemsFrag)
                    .commit();
        }
    }
}