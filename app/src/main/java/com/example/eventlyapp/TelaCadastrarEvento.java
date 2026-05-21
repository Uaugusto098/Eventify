package com.example.eventlyapp;
import com.example.eventlyapp.EventoDAO;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;

public class TelaCadastrarEvento extends AppCompatActivity {

    private Button cadastrarEventos;
    Evento user=new Evento();
    private TextView btnBack;

    private ImageView imageAdd;

    private ActivityResultLauncher<String> imagemUser;


    private Uri imageUriselect=null;

    private TextInputEditText inputNome,inputData,inputDesc;


    EventoDAO dao;


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


        dao=new EventoDAO();
        btnBack=findViewById(R.id.btnBack);
        cadastrarEventos=findViewById(R.id.cadastrarEventos);
        imageAdd=findViewById(R.id.imageAdd);
        inputNome = findViewById(R.id.info1Input); // ID do EditText, não do Layout
        inputData = findViewById(R.id.info2Input);
        inputDesc = findViewById(R.id.info3Input);

        inputData.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            private String anterior = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String texto = s.toString().replaceAll("[^0-9]", ""); // só números

                StringBuilder formatado = new StringBuilder();
                for (int i = 0; i < texto.length(); i++) {
                    if (i == 2 || i == 4) formatado.append("/"); // dd/MM/yyyy
                    formatado.append(texto.charAt(i));
                }

                s.replace(0, s.length(), formatado.toString());
                isFormatting = false;
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });
        cadastrarEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Trava o botão imediatamente no primeiro clique para evitar duplicação
                cadastrarEventos.setEnabled(false);

                // 2. Pega os textos e tira os espaços em branco do começo/fim com trim()
                String nomeStr = inputNome.getText().toString().trim();
                String dataStr = inputData.getText().toString().trim();
                String descStr = inputDesc.getText().toString().trim();

                // 3. Valida se o usuário esqueceu de colocar o nome
                if (nomeStr.isEmpty()) {
                    inputNome.setError("O nome do evento é obrigatório!");
                    inputNome.requestFocus();
                    cadastrarEventos.setEnabled(true); // Destrava o botão para ele tentar de novo
                    return; // Interrompe o processo e não salva no banco
                }

                // 4. Se passou pela validação, manda salvar
                capturarDados(nomeStr, dataStr, descStr);
                finish();
            }
        });
    }
    public void capturarDados(String nome, String data, String desc) {
        user.setNome(nome);

        // Verifica a data usando a string recebida no parâmetro
        if (data.isEmpty()){
            user.setData("Sem data marcada");
        } else {
            user.setData(data);
        }

        // Verifica a descrição usando a string recebida no parâmetro
        if (desc.isEmpty()){
            user.setDescricao("Evento sem descrição");
        } else {
            user.setDescricao(desc);
        }

        if (imageUriselect != null) {
            user.setImagemUri(imageUriselect.toString());
        }

        // Salva uma única vez!
        dao.salvar(user);
    }
}