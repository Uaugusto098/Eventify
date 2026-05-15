package com.example.eventlyapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    private Button cadastrarEventos,listarEventos;
    private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("teste_conexao");

        myRef.setValue("Opa, Pedro! Conectado via Java.")
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_TESTE", "Conexão realizada com sucesso!");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_TESTE", "Erro ao conectar: " + e.getMessage());
                });

        cadastrarEventos=findViewById(R.id.cadastrarEventos);
        listarEventos=findViewById(R.id.listarEventos);


        cadastrarEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                it=new Intent(MainActivity.this, TelaCadastrarEvento.class);
                startActivity(it);


            }
        });
        listarEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                it=new Intent(MainActivity.this, TelaEventos.class);
                startActivity(it);


            }
        });










    }
}