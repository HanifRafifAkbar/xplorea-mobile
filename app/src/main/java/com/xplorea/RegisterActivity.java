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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private Button btnRegister;

    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        session = new SessionManager(this);
        api = ApiClient.getClient().create(ApiService.class);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Semua harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        api.register(name, email, pass).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> res) {

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(RegisterActivity.this, "Gagal registrasi", Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = res.body();

                if (!"success".equals(u.getStatus())) {
                    Toast.makeText(RegisterActivity.this, u.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (u.getUserId() == null || u.getUserId().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "User ID tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                int uid;
                try {
                    uid = Integer.parseInt(u.getUserId());
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "Format userId salah", Toast.LENGTH_SHORT).show();
                    return;
                }

                session.saveSession(
                        uid,
                        u.getEmail(),
                        u.getToken()
                );

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
