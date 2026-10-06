package com.xplorea;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class CekTiketActivity extends AppCompatActivity {

    Spinner spAwal, spTujuan;
    Button btnCek;
    TextView txtHasil;

    String[] kota = {"Pilih lokasi...", "Surabaya", "Malang", "Kediri", "Blitar"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cek_tiket_activity);

        // FIX: ini membuat toolbar aktif & mencegah crash
        MaterialToolbar toolbar = findViewById(R.id.toolbarCekTiket);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        spAwal = findViewById(R.id.spAwal);
        spTujuan = findViewById(R.id.spTujuan);
        btnCek = findViewById(R.id.btnCek);
        txtHasil = findViewById(R.id.txtHasil);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                kota
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spAwal.setAdapter(adapter);
        spTujuan.setAdapter(adapter);

        btnCek.setOnClickListener(v -> cekTiket());
    }

    private void cekTiket() {
        String awal = spAwal.getSelectedItem().toString();
        String tujuan = spTujuan.getSelectedItem().toString();

        if (awal.equals("Pilih lokasi...") || tujuan.equals("Pilih lokasi...")) {
            Toast.makeText(this, "Silakan pilih lokasi awal dan tujuan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (awal.equals(tujuan)) {
            Toast.makeText(this, "Lokasi awal dan tujuan tidak boleh sama", Toast.LENGTH_SHORT).show();
            return;
        }

        String hargaKereta = getKeretaPrice(awal, tujuan);
        String hargaPesawat = getPesawatPrice(awal, tujuan);

        txtHasil.setVisibility(View.VISIBLE);
        txtHasil.setText(
                "Kereta: " + hargaKereta +
                        "\nPesawat: " + hargaPesawat
        );
    }

    private String getKeretaPrice(String a, String b) {
        if ((a.equals("Surabaya") && b.equals("Malang")) || (a.equals("Malang") && b.equals("Surabaya")))
            return "Rp 35.000";
        if ((a.equals("Surabaya") && b.equals("Kediri")) || (a.equals("Kediri") && b.equals("Surabaya")))
            return "Rp 55.000";
        if ((a.equals("Surabaya") && b.equals("Blitar")) || (a.equals("Blitar") && b.equals("Surabaya")))
            return "Rp 65.000";
        if ((a.equals("Malang") && b.equals("Kediri")) || (a.equals("Kediri") && b.equals("Malang")))
            return "Rp 45.000";
        if ((a.equals("Malang") && b.equals("Blitar")) || (a.equals("Blitar") && b.equals("Malang")))
            return "Rp 40.000";
        if ((a.equals("Kediri") && b.equals("Blitar")) || (a.equals("Blitar") && b.equals("Kediri")))
            return "Rp 25.000";
        return "-";
    }

    private String getPesawatPrice(String a, String b) {
        if ((a.equals("Surabaya") && b.equals("Malang")) || (a.equals("Malang") && b.equals("Surabaya")))
            return "Rp 400.000";
        if ((a.equals("Surabaya") && b.equals("Kediri")) || (a.equals("Kediri") && b.equals("Surabaya")))
            return "Rp 650.000";
        if ((a.equals("Surabaya") && b.equals("Blitar")) || (a.equals("Blitar") && b.equals("Surabaya")))
            return "Rp 700.000";
        if ((a.equals("Malang") && b.equals("Kediri")) || (a.equals("Kediri") && b.equals("Malang")))
            return "Rp 550.000";
        if ((a.equals("Malang") && b.equals("Blitar")) || (a.equals("Blitar") && b.equals("Malang")))
            return "Rp 600.000";
        if ((a.equals("Kediri") && b.equals("Blitar")) || (b.equals("Kediri") && a.equals("Blitar")))
            return "Rp 450.000";
        return "-";
    }
}
