package com.example.proyectodas.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectodas.R;
import com.example.proyectodas.adapters.TripAdapter;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.items.TripItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private List<TripItem> tripList;
    private long selectedStartTimestamp = 0;
    private long selectedEndTimestamp = 0;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout de este fragmento
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2. Cargar datos de la Base de Datos
        miDB db = new miDB(requireContext());
        tripList = db.getAllTrips();

        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        checkEmptyState();

        // 3. Configurar el Adaptador
        adapter = new TripAdapter(tripList);
        recyclerView.setAdapter(adapter);

        // 4. Configurar el Floating Action Button (Si lo moviste al XML del fragmento)
        FloatingActionButton fab = view.findViewById(R.id.fabAddTrip);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                showAddTripDialog();
                // Nota: Asegúrate de adaptar showAddTripDialog para que funcione dentro del Fragment
            });
        }
    }
    private void showAddTripDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_trip, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        EditText editPlace = view.findViewById(R.id.editPlace);
        EditText editStartDate = view.findViewById(R.id.editStartDate);
        EditText editEndDate = view.findViewById(R.id.editEndDate);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroupType);

        // Formateador automático según el idioma del móvil
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(requireContext());

        // Lógica para Fecha de Ida
        editStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                Calendar choice = Calendar.getInstance();
                choice.set(year, month, day);
                selectedStartTimestamp = choice.getTimeInMillis();
                editStartDate.setText(dateFormat.format(choice.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Lógica para Fecha de Vuelta
        editEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                Calendar choice = Calendar.getInstance();
                choice.set(year, month, day);
                selectedEndTimestamp = choice.getTimeInMillis();
                editEndDate.setText(dateFormat.format(choice.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view)
                .setTitle(getString(R.string.titulo_anadir_viaje))
                .setPositiveButton(getString(R.string.anadir), null)
                .setNegativeButton(getString(R.string.cancelar), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String place = editPlace.getText().toString();
            String type = (radioGroup.getCheckedRadioButtonId() == R.id.radioSolo) ? "Solo" : "Group";

            boolean isValid = true;

            if (title.isEmpty()) { editTitle.setError(getString(R.string.falta_info_anadir_viaje)); isValid = false; }
            if (place.isEmpty()) { editPlace.setError(getString(R.string.falta_info_anadir_viaje)); isValid = false; }
            if (selectedStartTimestamp == 0) { editStartDate.setError(getString(R.string.falta_info_anadir_viaje)); isValid = false; }
            if (selectedEndTimestamp == 0) { editEndDate.setError(getString(R.string.falta_info_anadir_viaje)); isValid = false; }

            // Opcional: Validar que la vuelta sea después de la ida
            if (isValid && selectedEndTimestamp < selectedStartTimestamp) {
                editEndDate.setError("La vuelta debe ser después de la ida");
                isValid = false;
            }

            if (isValid) {
                TripItem newTrip = new TripItem(title, place, selectedStartTimestamp, selectedEndTimestamp, type);
                miDB db = new miDB(requireContext());
                db.addTrip(newTrip);

                tripList.add(newTrip);
                adapter.notifyItemInserted(tripList.size() - 1);
                checkEmptyState(); // Tu método para el texto vacío
                dialog.dismiss();
            }
        });
    }

    private void checkEmptyState() {
        if (tripList.isEmpty()) { // Cambia tripList por luggageList o itemList según el fragmento
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}
