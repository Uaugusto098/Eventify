package com.example.eventlyapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import com.google.android.material.snackbar.Snackbar;
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

        String id = intentRecebida.getStringExtra("id");
        String img = intentRecebida.getStringExtra("imagem");
        descricaoInput.setText(intentRecebida.getStringExtra("descricao"));
        nomeInput.setText(intentRecebida.getStringExtra("nome"));
        dataInput.setText(intentRecebida.getStringExtra("data"));


        Button btnExcluir = findViewById(R.id.btnExcluir);
        Button btnAlterar = findViewById(R.id.btnAlterar);

        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEvento.this);

                // 2. Configurar o título e a mensagem
                builder.setTitle("Excluir Evento");
                builder.setMessage("Deseja realmente excluir o evento?");

                // 3. Botão de confirmação (Sim)
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    // Se clicar em sim, ele executa a sua lógica de sair
                    EventoDAO dao = new EventoDAO();

                    Evento evento = new Evento();
                    evento.setId(id);

                    dao.deletar(evento);

                    finish(); // Fecha a tela atual para ele não conseguir voltar no botão "back"
                });

                // 4. Botão de cancelamento (Não)
                builder.setNegativeButton("Não", (dialog, which) -> {
                    // Se clicar em não, apenas fecha o card e não faz nada
                    dialog.dismiss();
                });

                // 5. Exibir o card na tela
                builder.show();
            }
        });
        btnAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nomeInput.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(android.R.id.content), "Preencha o campo Nome para alterar!", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#1D2F46"))
                            .setTextColor(Color.WHITE)
                            .show();
                    return;
                }
                if(nomeInput.getText().toString().equals(intentRecebida.getStringExtra("nome")) && dataInput.getText().toString().equals(intentRecebida.getStringExtra("data")) && descricaoInput.getText().toString().equals(intentRecebida.getStringExtra("descricao"))){
                    Snackbar.make(findViewById(android.R.id.content), "É preciso alterar pelo menos um campo para alterar ", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#1D2F46"))
                            .setTextColor(Color.WHITE)
                            .show();
                    return;
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEvento.this);

                // 2. Configurar o título e a mensagem
                builder.setTitle("Alterar Evento");
                builder.setMessage("Deseja realmente alterar o evento?");

                // 3. Botão de confirmação (Sim)
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    // Se clicar em sim, ele executa a sua lógica de sair
                    EventoDAO dao = new EventoDAO();

                    Evento evento = new Evento();
                    evento.setId(id);
                    evento.setNome(nomeInput.getText().toString());
                    if (dataInput.getText().toString().isEmpty()){
                        dataInput.setText("Sem data marcada");
                        evento.setData(dataInput.getText().toString());
                    }
                    if (descricaoInput.getText().toString().isEmpty()){
                        descricaoInput.setText("Evento sem descrição");
                        evento.setDescricao(descricaoInput.getText().toString());
                    }
                    evento.setData(dataInput.getText().toString());
                    evento.setDescricao(descricaoInput.getText().toString());
                    evento.setImagemUri(img);

                    dao.salvar(evento);

                    finish(); // Fecha a tela atual para ele não conseguir voltar no botão "back"
                });

                // 4. Botão de cancelamento (Não)
                builder.setNegativeButton("Não", (dialog, which) -> {
                    // Se clicar em não, apenas fecha o card e não faz nada
                    dialog.dismiss();
                });

                // 5. Exibir o card na tela
                builder.show();
            }
        });
    }
    public void sair(View view){
        finish();
    }
}
