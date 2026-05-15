package com.example.eventlyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class TelaCadastrarEvento extends AppCompatActivity {

    private Button cadastrarEventos;
    private TextView btnBack;

    private ImageView imageAdd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_cadastrar_evento);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnBack=findViewById(R.id.btnBack);
        cadastrarEventos=findViewById(R.id.cadastrarEventos);
        imageAdd=findViewById(R.id.imageAdd);
        TextInputEditText inputNome = findViewById(R.id.info1Input); // ID do EditText, não do Layout
        TextInputEditText inputData = findViewById(R.id.info2Input);
        TextInputEditText inputDesc = findViewById(R.id.info3Input);







        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });



    }






}