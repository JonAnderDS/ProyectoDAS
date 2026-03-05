package com.example.proyectodas.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.proyectodas.R;
import com.example.proyectodas.helpers.LocaleHelper;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.loginUser);
        EditText etPassword = findViewById(R.id.loginPasswd);
        Button btnLogin = findViewById(R.id.loginButton);

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

}
