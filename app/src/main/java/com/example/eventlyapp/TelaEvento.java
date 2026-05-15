package com.example.eventlyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TelaEvento extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_evento);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 1. Mantém o padding no topo, esquerda e direita. Mas o BOTTOM (fundo) fica ZERO!
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // 2. Pega a sua BottomNavigationView
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

            // 3. Aplica o padding do sistema apenas DENTRO dela, para ela esticar a cor azul até o fim
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);

            return insets;
        });
        TextView txtvoltar = findViewById(R.id.txtBack1);
        //layout
        TextInputLayout descricaoInfo = findViewById(R.id.descricaoInfo);
        TextInputLayout nomeInfo = findViewById(R.id.nomeInfo);
        TextInputLayout dataInfo = findViewById(R.id.dataInfo);

        //edtext

        TextInputEditText descricaoInput= findViewById(R.id.descricaoInput);
        TextInputEditText nomeInput= findViewById(R.id.nomeInput);
        TextInputEditText dataInput= findViewById(R.id.dataInput);

        Intent intentRecebida = getIntent();


        descricaoInput.setText(intentRecebida.getStringExtra("descricao"));
        nomeInput.setText(intentRecebida.getStringExtra("nome"));
        dataInput.setText(intentRecebida.getStringExtra("data"));




    }
    public void sair(View view){
        finish();
    }
}
