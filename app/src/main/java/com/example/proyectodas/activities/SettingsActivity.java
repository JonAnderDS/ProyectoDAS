package com.example.proyectodas.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private DrawerLayout drawerLayout;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        guardarImagenEnMemoriaInterna(selectedImageUri);
                        cargarFotoDePerfil();
                    }
                }
            }
    );

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

        Button btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic);
        android.content.SharedPreferences prefs = getSharedPreferences("MisAjustes", Context.MODE_PRIVATE);
        boolean tieneFoto = prefs.getBoolean("tieneFotoPerfil", false); // false es el valor por defecto

        if (tieneFoto) {
            cargarFotoDePerfil();
        }

        // 2. Lanzamos el Intent Implícito al hacer clic
        btnChangeProfilePic.setOnClickListener(v -> {
            // Este es el intent implícito para pedir una imagen a la galería
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(i);
        });
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

    private void guardarImagenEnMemoriaInterna(Uri uri) {
        try {
            // Abrimos el flujo de lectura de la imagen seleccionada
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Abrimos el flujo de escritura hacia nuestra carpeta privada
            FileOutputStream outputStream = openFileOutput("profile_pic.jpg", Context.MODE_PRIVATE);

            // Copiamos los datos (bytes)
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Cerramos los flujos
            outputStream.close();
            inputStream.close();

            android.content.SharedPreferences prefs = getSharedPreferences("MisAjustes", Context.MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("tieneFotoPerfil", true);
            editor.apply();

            Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
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

    private void cargarFotoDePerfil() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        if (navigationView != null) {
            // Accedemos al header del NavigationView
            View headerView = navigationView.getHeaderView(0);
            ImageView ivProfilePic = headerView.findViewById(R.id.imageView);

            try {
                // Buscamos nuestro archivo guardado en la memoria privada
                FileInputStream fis = openFileInput("profile_pic.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();

                // Si existe, lo ponemos en la vista
                if (bitmap != null) {
                    ivProfilePic.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                // Si el archivo no existe (ej. es la primera vez que abre la app),
                // saltará aquí y simplemente dejará la imagen por defecto.
                e.printStackTrace();
            }
        }
    }
}