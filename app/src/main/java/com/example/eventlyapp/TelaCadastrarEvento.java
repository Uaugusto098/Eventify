package com.example.eventlyapp;

import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

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
        imagemUser= registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri select) {

                if(select!=null)
                {   imageUriselect=select;

                    imageAdd.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageAdd.setImageURI(select);
                }

            }
        });
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imagemUser.launch("image/*");

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



                capturarDados(
                        inputNome.getText().toString(),
                        inputData.getText().toString(),
                        inputDesc.getText().toString()

                );

                finish();


            }
        });
    }
    public void capturarDados(String nome, String data, String desc){


        user.setNome(nome);
        user.setData(data);
        user.setDescricao(desc);

        if (inputData.getText().toString().isEmpty()){
            inputData.setText("Sem data marcada");
            user.setData(inputData.getText().toString());
        }
        if (inputDesc.getText().toString().isEmpty()){
            inputDesc.setText("Evento sem descrição");
            user.setDescricao(inputDesc.getText().toString());
        }
        if(imageUriselect!=null)
        {
            user.setImagemUri(imageUriselect.toString());
        }

        dao.salvar(user);

    }
}