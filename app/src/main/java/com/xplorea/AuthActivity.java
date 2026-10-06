package com.xplorea;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xplorea.model.User;
import com.xplorea.utils.ApiClient;
import com.xplorea.utils.ApiService;
import com.xplorea.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister, btnGuest;

    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);

        session = new SessionManager(this);
        api = ApiClient.getClient().create(ApiService.class);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGuest = findViewById(R.id.btnGuest);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnGuest.setOnClickListener(v -> {
            session.saveSession(-1, "guest", "guest_token");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Harus diisi semua", Toast.LENGTH_SHORT).show();
            return;
        }

        api.login(email, pass).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> res) {

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(AuthActivity.this, "Login gagal", Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = res.body();

                if (!"success".equals(u.getStatus())) {
                    Toast.makeText(AuthActivity.this, u.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (u.getUserId() == null || u.getUserId().isEmpty()) {
                    Toast.makeText(AuthActivity.this, "User ID tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                int uid;
                try {
                    uid = Integer.parseInt(u.getUserId());
                } catch (Exception e) {
                    Toast.makeText(AuthActivity.this, "Format userId salah", Toast.LENGTH_SHORT).show();
                    return;
                }

                session.saveSession(
                        uid,
                        u.getEmail(),
                        u.getToken()
                );

                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AuthActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
