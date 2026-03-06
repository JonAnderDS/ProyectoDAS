package com.example.proyectodas.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proyectodas.R;
import com.example.proyectodas.helpers.LocaleHelper;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // El usuario le ha dado a "Permitir"
                    Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show();
                } else {
                    // El usuario le ha dado a "No permitir"
                    Toast.makeText(this, "Sin permisos, no recibirás recordatorios de tus viajes", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.loginUser);
        EditText etPassword = findViewById(R.id.loginPasswd);
        Button btnLogin = findViewById(R.id.loginButton);

        pedirPermisosNotificaciones();
        // Cuando se hace click en continuar
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() ){
                    String errorMessage = getString(R.string.error_usuario);
                    etUsername.setError(errorMessage);
                } else if(password.isEmpty()){
                    String errorPasswd = getString(R.string.error_passwd);
                    etPassword.setError(errorPasswd);
                }
                else{
                    if (username.equals("admin") && password.equals("1234")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        // This tells the Activity to use our custom LocaleHelper settings before it even starts
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void pedirPermisosNotificaciones() {
        // Solo necesitamos pedir permiso explícito en Android 13 (API 33 / TIRAMISU) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Comprobamos si ya tenemos el permiso
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Si no lo tenemos, lanzamos la ventanita del sistema
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
