/***package com.example.proyectodas.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectodas.R;
import com.example.proyectodas.adapters.TripAdapter;
import com.example.proyectodas.database.miDB;
import com.example.proyectodas.helpers.LocaleHelper;
import com.example.proyectodas.items.TripItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Long selectedTimestamp = 0L;
    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private List<TripItem> tripList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("isDarkMode", false);

        // Apply theme before the view is even created
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 3. DYNAMIC USERNAME IN HEADER ---
        setupNavigationHeader();

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.MainToolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Se abre el menú al hacer click en el icono
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        // Manejo de interacciones con los botones del menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                //Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                //Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.close(); // Se cierra al hacer click
            return true;
        });


        recyclerView = findViewById(R.id.recyclerView);
        // Para que la lista de cards esté una debajo de la otra
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        miDB db = new miDB(this);
        // 1. Llamar todos los viajes guardados en la base de datos
        tripList = db.getAllTrips();
        // 2. Llamar a adapter con la lista de viajes
        adapter = new TripAdapter(tripList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddTrip);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTripDialog(); // Abre el menú de agregar viaje
            }
        });
    }

    private void showAddTripDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_trip, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        EditText editPlace = view.findViewById(R.id.editPlace);
        EditText editDate = view.findViewById(R.id.editDate);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroupType);

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view1, year, month, day) -> {
                Calendar choice = Calendar.getInstance();
                choice.set(year, month, day);

                // 1. Save the number for the database
                selectedTimestamp = choice.getTimeInMillis();

                // 2. Show the "Pretty" version to the user in their language
                java.text.DateFormat df = android.text.format.DateFormat.getMediumDateFormat(this);
                editDate.setText(df.format(choice.getTime()));

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
        /*editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        // Months are 0-indexed (Jan is 0), so we add 1
                        String dateString = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editDate.setText(dateString);
                    }, year, month, day);

            datePicker.show();
        });*/
/*
        String tituloAnadirViaje = getString(R.string.titulo_anadir_viaje);
        String anadir = getString(R.string.anadir);
        String cancelar = getString(R.string.cancelar);
        String faltaInfo = getString(R.string.falta_info_anadir_viaje);

        builder.setView(view)
                .setTitle(tituloAnadirViaje)
                .setPositiveButton(anadir, (dialog, which) -> {
                    String title = editTitle.getText().toString();
                    String place = editPlace.getText().toString();
                    String date = editDate.getText().toString();
                    String type = (radioGroup.getCheckedRadioButtonId() == R.id.radioSolo) ? "Solo" : "Group";

                    if (title.isEmpty() ){
                        editTitle.setError(faltaInfo);
                        showAddTripDialog();
                    } else if(place.isEmpty()){
                        editPlace.setError(faltaInfo);
                        showAddTripDialog();
                    } else if (date.isEmpty()) {
                        editDate.setError(faltaInfo);
                        showAddTripDialog();
                    }
                    else{
                        // 1. Crear Objeto
                        TripItem newTrip = new TripItem(title, place, selectedTimestamp, type);

                        // 2. Guardar en la base de datos
                        miDB db = new miDB(MainActivity.this);
                        db.addTrip(newTrip);

                        // 3. Actualizar UI
                        tripList.add(newTrip);
                        adapter.notifyItemInserted(tripList.size() - 1);
                    }
                })
                .setNegativeButton(cancelar, null)
                .show();
    }

    private void setupNavigationHeader() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        // Headers are "inflated" separately, so we get the first one (index 0)
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvUserName);

        String savedName = prefs.getString("username", getString(R.string.default_user));
        tvHeaderName.setText(savedName);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // This tells the Activity to use our custom LocaleHelper settings before it even starts
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the name every time the user comes back to this screen
        String savedName = prefs.getString("username", "Traveler");

        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvUserName);

        tvHeaderName.setText(savedName);

        // Check if we need to recreate for language/theme
        // If the language changed in settings, this will catch it
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // When returning from Settings, check if the language changed
        // and force a refresh of the strings.
        recreate();
    }
}**/

package com.example.proyectodas.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyectodas.R;
import com.example.proyectodas.fragments.HomeFragment;
import com.example.proyectodas.fragments.SettingsFragment;
import com.example.proyectodas.helpers.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Apply theme before view creation
        boolean isDark = prefs.getBoolean("isDarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        // Load HomeFragment by default if starting fresh
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void setupUI() {
        drawerLayout = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.MainToolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());

        setupNavigationHeader(navigationView);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // 1. Clear the entire back stack to return to the root (Home)
                // This prevents the "unlimited" buildup.
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                replaceFragment(new HomeFragment(), false);
            } else if (id == R.id.nav_settings) {
                replaceFragment(new SettingsFragment(), true);
            }

            drawerLayout.close();
            return true;
        });
    }

    // Refined replaceFragment with Stack Control
    private void replaceFragment(Fragment fragment, boolean addToStack) {
        String tag = fragment.getClass().getSimpleName();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // 2. DUPLICATE CHECK: Don't reload if we are already there
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Add a nice transition for the evaluation points
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment, tag);

        // 3. STACK CONTROL: Only add to backstack if it's NOT the home screen
        if (addToStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    public void setupNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvUserName);
        String savedName = prefs.getString("username", getString(R.string.default_user));
        tvHeaderName.setText(savedName);
    }

    /*@Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/
}