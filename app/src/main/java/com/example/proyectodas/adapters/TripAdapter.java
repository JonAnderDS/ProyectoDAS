package com.example.proyectodas.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.example.proyectodas.database.miDB;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectodas.R;
import com.example.proyectodas.activities.MainActivity;
import com.example.proyectodas.fragments.InventoryFragment; // Añadido el import
import com.example.proyectodas.items.TripItem;

import java.util.Date;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TravelViewHolder> {

    private List<TripItem> travelList;
    private Runnable onListChangedListener;

    public TripAdapter(List<TripItem> travelList, Runnable onListChangedListener) {
        this.travelList = travelList;
        this.onListChangedListener = onListChangedListener;
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

        String soloGrupo = currentItem.getTripType();
        if (soloGrupo.equals("Solo")){
            holder.tvTripType.setText(R.string.solo);
        } else {
            holder.tvTripType.setText(R.string.grupo);
        }

        // Formateador automático según el idioma del móvil
        java.text.DateFormat dateFormatPrincipal = android.text.format.DateFormat.getDateFormat(holder.itemView.getContext());

        // Convertir los timestamps a Strings con formato (ej: 14/05/2026 - 20/05/2026)
        String idaStr = dateFormatPrincipal.format(new java.util.Date(currentItem.getStartDateTimestamp()));
        String vueltaStr = dateFormatPrincipal.format(new java.util.Date(currentItem.getEndDateTimestamp()));

        holder.tvDate.setText(idaStr + " - " + vueltaStr);

        // LÓGICA DEL MENÚ DE 3 PUNTOS
        holder.btnMoreOptions.setOnClickListener(v -> {
            // Creamos el menú emergente anclado al botón
            android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), holder.btnMoreOptions);
            popup.inflate(R.menu.menu_item_options);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_edit) {
                    // 1. Preparamos el diálogo usando tu diseño existente
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                    View dialogView = android.view.LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_add_trip, null);

                    EditText editTitle = dialogView.findViewById(R.id.editTitle);
                    EditText editPlace = dialogView.findViewById(R.id.editPlace);
                    EditText editStartDate = dialogView.findViewById(R.id.editStartDate);
                    EditText editEndDate = dialogView.findViewById(R.id.editEndDate);
                    RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupType);

                    // 2. RELLENAMOS LOS DATOS ACTUALES
                    editTitle.setText(currentItem.getTitle());
                    editPlace.setText(currentItem.getPlace());

                    java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(v.getContext());
                    editStartDate.setText(dateFormat.format(new java.util.Date(currentItem.getStartDateTimestamp())));
                    editEndDate.setText(dateFormat.format(new java.util.Date(currentItem.getEndDateTimestamp())));

                    if ("Solo".equals(currentItem.getTripType())) {
                        radioGroup.check(R.id.radioSolo); // Ajusta el ID si se llama distinto en tu XML
                    } else {
                        // Aquí pon el ID de tu RadioButton de Grupo (ej. radioGroup)
                        radioGroup.check(radioGroup.getChildAt(1).getId());
                    }

                    // Arrays de tamaño 1 para poder modificar variables dentro de los listeners (truco de Java)
                    final long[] newStartTimestamp = {currentItem.getStartDateTimestamp()};
                    final long[] newEndTimestamp = {currentItem.getEndDateTimestamp()};

                    // 3. Lógica de los calendarios (DatePickers)
                    editStartDate.setOnClickListener(viewBtn -> {
                        java.util.Calendar calendar = java.util.Calendar.getInstance();
                        calendar.setTimeInMillis(newStartTimestamp[0]); // Abrir en la fecha actual del viaje
                        new android.app.DatePickerDialog(v.getContext(), (view1, year, month, day) -> {
                            java.util.Calendar choice = java.util.Calendar.getInstance();
                            choice.set(year, month, day);
                            newStartTimestamp[0] = choice.getTimeInMillis();
                            editStartDate.setText(dateFormat.format(choice.getTime()));
                        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
                    });

                    editEndDate.setOnClickListener(viewBtn -> {
                        java.util.Calendar calendar = java.util.Calendar.getInstance();
                        calendar.setTimeInMillis(newEndTimestamp[0]);
                        new android.app.DatePickerDialog(v.getContext(), (view1, year, month, day) -> {
                            java.util.Calendar choice = java.util.Calendar.getInstance();
                            choice.set(year, month, day);
                            newEndTimestamp[0] = choice.getTimeInMillis();
                            editEndDate.setText(dateFormat.format(choice.getTime()));
                        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
                    });

                    // 4. Mostramos el diálogo
                    builder.setView(dialogView)
                            .setTitle(R.string.editar_viaje)
                            .setPositiveButton(R.string.guardar, null) // Null para sobreescribir el click luego
                            .setNegativeButton(R.string.cancelar, null);

                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                    dialog.show();

                    // 5. Lógica de guardado y validación
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(btnView -> {
                        String title = editTitle.getText().toString();
                        String place = editPlace.getText().toString();
                        String type = (radioGroup.getCheckedRadioButtonId() == R.id.radioSolo) ? "Solo" : "Group";

                        if (title.isEmpty() || place.isEmpty()) {
                            android.widget.Toast.makeText(v.getContext(), R.string.falta_info_anadir_viaje, android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (newEndTimestamp[0] < newStartTimestamp[0]) {
                            editEndDate.setError("La vuelta debe ser después de la ida");
                            return;
                        }

                        // Actualizamos el objeto actual
                        currentItem.setTitle(title);
                        currentItem.setPlace(place);
                        currentItem.setStartDateTimestamp(newStartTimestamp[0]);
                        currentItem.setEndDateTimestamp(newEndTimestamp[0]);
                        currentItem.setTripType(type);

                        // Actualizamos la Base de Datos
                        miDB db = new miDB(v.getContext());
                        db.updateTrip(currentItem);

                        // Avisamos al adaptador para que repinte ESA tarjeta específica
                        notifyItemChanged(position);

                        dialog.dismiss(); // Cerramos el diálogo si todo ha ido bien
                    });

                    return true;

                } else if (id == R.id.action_delete) {
                    // CREAMOS EL DIÁLOGO DE CONFIRMACIÓN ANTES DE BORRAR
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.titulo_eliminar_viaje)
                            .setMessage(R.string.mensaje_eliminar_viaje)
                            .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                                // --- ESTO SOLO SE EJECUTA SI EL USUARIO DICE "SÍ" ---
                                // 1. Borrar de la Base de Datos
                                miDB db = new miDB(v.getContext());
                                db.deleteTrip(currentItem.getId());

                                // 2. Borrar de la lista visual
                                travelList.remove(position);

                                // 3. Avisar al adaptador para la animación
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, travelList.size());

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

        // CORRECCIÓN: Usar holder.itemView en lugar de itemView
        holder.itemView.setOnClickListener(v -> {
            InventoryFragment inventoryFragment = new InventoryFragment();

            // Configurar los argumentos para pasar al nuevo Fragment
            Bundle bundle = new Bundle();

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
        ImageButton btnMoreOptions;

        public TravelViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlace = itemView.findViewById(R.id.tvPlace);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTripType = itemView.findViewById(R.id.tvTripType);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}