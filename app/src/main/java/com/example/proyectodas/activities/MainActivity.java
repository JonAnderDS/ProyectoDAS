package com.example.proyectodas.activities;

import android.content.Context;
import android.content.Intent;
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
// import com.example.proyectodas.fragments.InventoryFragment; // Para tu futuro fragmento
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

        // Aplicar tema oscuro/claro antes de la creación de la vista
        boolean isDark = prefs.getBoolean("isDarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        // Cargar HomeFragment por defecto solo si no hay un estado guardado (evita solapamiento)
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

        // Abrir el Navigation Drawer al pulsar el icono del Toolbar
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        setupNavigationHeader(navigationView);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Limpiar el BackStack para volver a la raíz y evitar apilamiento infinito
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                replaceFragment(new HomeFragment(), false);
            } else if (id == R.id.nav_settings) {
                // Modificación: Abrir SettingsActivity mediante un Intent clásico
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }

            // Cerrar el panel lateral después de la selección
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Reemplaza el fragmento actual.
     * Puede ser llamado desde HomeFragment para abrir InventoryFragment.
     */
    public void replaceFragment(Fragment fragment, boolean addToStack) {
        String tag = fragment.getClass().getSimpleName();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Evitar recargar el fragmento si ya estamos en él
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment, tag);

        // Solo añadir al BackStack si no es el Home
        if (addToStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
    }

    private void setupNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvUserName);
        String savedName = prefs.getString("username", getString(R.string.default_user));
        tvHeaderName.setText(savedName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 1. Actualizar el nombre en el menú lateral al volver
        NavigationView navigationView = findViewById(R.id.navigationView);
        setupNavigationHeader(navigationView);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 2. Forzar la recarga de la actividad para aplicar cambios de idioma
        recreate();
    }

    @Override
    public void onBackPressed() {
        // Mejor práctica: si el menú lateral está abierto, ciérralo en lugar de salir de la app
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
