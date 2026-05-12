package com.example.eventlyapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class TelaEventos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_eventos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView imgNenhumEvento = findViewById(R.id.imgNenhumEvento);
        ArrayList<String> eventos = new ArrayList<>(Arrays.asList("Workshop de Kotlin", "Palestra: Arquitetura MVVM", "Hackathon 2026", "Meetup de Android", "DevFest Regional"
        ));
        imgNenhumEvento.setVisibility(GONE);

// 2. O Adapter (Contexto, o layout do CARD que você criou, o ID do TextView dentro do card, e a lista)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_lista,
                R.id.txtNomeEvento,
                eventos
        );

// 3. Setar o adapter no seu ListView
        ListView listView = findViewById(R.id.lsvEventos);
        listView.setAdapter(adapter);

        if (eventos.isEmpty()){
            imgNenhumEvento.setVisibility(VISIBLE);
        }

    }
}