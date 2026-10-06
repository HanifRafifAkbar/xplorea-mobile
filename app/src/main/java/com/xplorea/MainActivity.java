package com.xplorea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.xplorea.maps.AutoCompleteAdapter;
import com.xplorea.utils.SessionManager;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.model.RectangularBounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;

    Button searchBar;
    Button btnCekTiket, btnLogout, btnFavorit;
    TextView txtNamaUser;

    GoogleMap mMap;
    SessionManager session;

    PlacesClient placesClient;
    AutocompleteSessionToken sessionToken;

    List<AutocompletePrediction> predictionList = new ArrayList<>();
    AutoCompleteAdapter autocompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        session = new SessionManager(this);

        drawerLayout = findViewById(R.id.drawerLayout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Ambil komponen sidebar
        txtNamaUser = findViewById(R.id.txtNamaUser);
        btnFavorit = findViewById(R.id.btnFavorit);
        btnCekTiket = findViewById(R.id.btnCekTiket);
        btnCekTiket.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CekTiketActivity.class);
            startActivity(intent);
        });
        btnLogout = findViewById(R.id.btnLogout);

        // Tampilkan email user
        String email = session.getEmail();
        if (email != null) {
            txtNamaUser.setText(email);
        } else {
            txtNamaUser.setText("User");
        }

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Search bar
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> openSearchDialog());

        // Init Places
        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);
        sessionToken = AutocompleteSessionToken.newInstance();

        // Maps
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    private void openSearchDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.PopupDialogTheme);

        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_search, null);
        builder.setView(v);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText input = v.findViewById(R.id.inputSearch);
        RecyclerView rv = v.findViewById(R.id.recyclerPrediction);

        rv.setLayoutManager(new LinearLayoutManager(this));

        autocompleteAdapter = new AutoCompleteAdapter(
                predictionList,
                prediction -> fetchPlaceLatLng(prediction.getPlaceId(), dialog)
        );
        rv.setAdapter(autocompleteAdapter);

        Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] debounce = new Runnable[1];

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (debounce[0] != null) handler.removeCallbacks(debounce[0]);

                debounce[0] = () -> {
                    if (s.length() >= 2)
                        fetchPredictions(s.toString().trim());
                };
                handler.postDelayed(debounce[0], 250);
            }
        });

        input.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    private void fetchPredictions(String q) {
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-8.5, 110.0),
                new LatLng(-6.5, 114.0)
        );

        FindAutocompletePredictionsRequest req =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(q)
                        .setSessionToken(sessionToken)
                        .setLocationBias(bounds)
                        .setCountries("ID")
                        .build();

        placesClient.findAutocompletePredictions(req)
                .addOnSuccessListener(res -> {
                    predictionList.clear();
                    predictionList.addAll(res.getAutocompletePredictions());
                    autocompleteAdapter.notifyDataSetChanged();
                });
    }

    private void fetchPlaceLatLng(String placeId, AlertDialog dialog) {
        FetchPlaceRequest req = FetchPlaceRequest.builder(
                placeId,
                Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME)
        ).build();

        placesClient.fetchPlace(req).addOnSuccessListener(result -> {
            LatLng ll = result.getPlace().getLatLng();
            if (ll != null) {
                moveCamera(ll.latitude, ll.longitude);
                searchBar.setText(result.getPlace().getName());
            }
            dialog.dismiss();

            sessionToken = AutocompleteSessionToken.newInstance();
        });
    }

    private void moveCamera(double lat, double lng) {
        if (mMap == null) return;

        LatLng pos = new LatLng(lat, lng);

        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16));
        mMap.addMarker(new MarkerOptions().position(pos).title("Lokasi"));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Keluar dari akun?")
                .setPositiveButton("Ya", (d, w) -> {
                    session.logout();
                    Intent intent =
                            new Intent(MainActivity.this, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap g) {
        mMap = g;
        LatLng jatim = new LatLng(-7.250445, 112.768845);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jatim, 8));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }
}
