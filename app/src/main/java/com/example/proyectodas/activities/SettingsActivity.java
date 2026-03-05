package com.example.proyectodas.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.proyectodas.R;
import com.example.proyectodas.helpers.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private DrawerLayout drawerLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // --- 0. CONFIGURACIÓN DEL MENÚ LATERAL (DRAWER) ---
        drawerLayout = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.MainToolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Abrir el menú al pulsar el icono hamburguesa
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Cargar el nombre en la cabecera del menú
        setupNavigationHeader(navigationView);

        // Configurar los clicks del menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Como Settings es un Intent extra, simplemente lo cerramos para volver al Home original
                finish();
            } else if (id == R.id.nav_settings) {
                // Si pulsa Settings estando en Settings, solo cerramos el panel
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        });


        // --- 1. LÓGICA DEL MODO OSCURO ---
        SwitchMaterial switchDark = findViewById(R.id.switchDarkMode);
        switchDark.setChecked(prefs.getBoolean("isDarkMode", false));

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("isDarkMode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // --- 2. LÓGICA DEL NOMBRE DE USUARIO ---
        TextView tvName = findViewById(R.id.tvCurrentName);
        tvName.setText(prefs.getString("username", "Traveler"));

        tvName.setOnClickListener(v -> showNameDialog(tvName));

        // --- 3. LÓGICA DEL IDIOMA ---
        Spinner spinner = findViewById(R.id.spinnerLanguage);
        spinner.setSelection(prefs.getInt("langIndex", 0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String code = "en";
                if (position == 1) code = "es";
                if (position == 2) code = "eu";

                String currentLang = LocaleHelper.getPersistedData(SettingsActivity.this, "en");

                if (!code.equals(currentLang)) {
                    LocaleHelper.setLocale(SettingsActivity.this, code);
                    prefs.edit().putInt("langIndex", position).apply();
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        TextView tvAbout = findViewById(R.id.tvAboutApp);
        tvAbout.setText(cargarInformacionApp());
    }

    /**
     * Lee el fichero de texto desde res/raw y lo devuelve como String.
     */
    private String cargarInformacionApp() {
        StringBuilder resultado = new StringBuilder();
        // Los recursos en res/raw se leen mediante InputStream
        try (InputStream is = getResources().openRawResource(R.raw.acerca_de);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                resultado.append(linea).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Información no disponible";
        }
        return resultado.toString();
    }

    private void showNameDialog(TextView displayView) {
        EditText input = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Enter your name")
                .setView(input)
                .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = input.getText().toString().trim();

            if (name.isEmpty()) {
                input.setError("Name cannot be empty");
            } else {
                prefs.edit().putString("username", name).apply();
                displayView.setText(name); // Actualiza el TextView de la pantalla

                // ACTUALIZA INSTANTÁNEAMENTE EL MENÚ LATERAL
                NavigationView navigationView = findViewById(R.id.navigationView);
                setupNavigationHeader(navigationView);

                dialog.dismiss();
            }
        });
    }

    private void setupNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvUserName);
        String savedName = prefs.getString("username", "Traveler"); // Usamos el texto local o getString
        tvHeaderName.setText(savedName);
    }

    @Override
    public void onBackPressed() {
        // Si el menú está abierto, ciérralo. Si no, vuelve atrás como es habitual.
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}