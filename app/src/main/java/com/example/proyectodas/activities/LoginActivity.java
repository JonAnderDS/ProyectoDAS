package com.example.proyectodas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.proyectodas.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.loginUser);
        EditText etPassword = findViewById(R.id.loginPasswd);
        Button btnLogin = findViewById(R.id.loginButton);

        // 2. Set the click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() ){
                    String errorMessage = getString(R.string.error_usuario);
                    etUsername.setError(errorMessage);
                } else if(password.isEmpty()){
                    etPassword.setError("Password cannot be empty");
                }
                else{
                    if (username.equals("admin") && password.equals("1234")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

    }

}
