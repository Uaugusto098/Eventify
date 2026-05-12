package com.example.eventlyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class TelaCadastro extends AppCompatActivity {
    private EditText editNomeCadastro, editEmailCadastro, editSenhaCadastro, editSenhaCadastro2;
    private Button btn_cadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciarComponentes();

        btn_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vw) {
                String nome = editNomeCadastro.getText().toString().trim();
                String email = editEmailCadastro.getText().toString().trim();
                String senha = editSenhaCadastro.getText().toString().trim();
                String senhha2 = editSenhaCadastro2.getText().toString().trim();

                if(nome.isEmpty())
                    editNomeCadastro.setError("Nome Obrigatório");
                else if(email.isEmpty())
                    editEmailCadastro.setError("Email Obrigatório");
                else if(senha.isEmpty())
                    editSenhaCadastro.setError("Senha Obrigatória");
                else if(senhha2.isEmpty())
                    editSenhaCadastro2.setError("Senha Obrigatório");
                else if(!senha.equals(senhha2))
                    editSenhaCadastro2.setError("As senhas devem ser iguais");
                else
                    cadastrarUser(vw);
                limparCampos();

            }
        });


    }
    public void iniciarComponentes(){
        editEmailCadastro = findViewById(R.id.edtEmailCadastro);
        editNomeCadastro = findViewById(R.id.edtNomeCadastro);
        editSenhaCadastro = findViewById(R.id.edtSenhaCadastro);
        editSenhaCadastro2 = findViewById(R.id.edtSenha2Cadastro);
        btn_cadastro = findViewById(R.id.btn_cadastro);
    }

    public void limparCampos(){
        editEmailCadastro.setText(null);
        editNomeCadastro.setText(null);
        editSenhaCadastro.setText(null);
        editSenhaCadastro2.setText(null);

    }

    public void cadastrarUser(View vw){
        String email = editEmailCadastro.getText().toString().trim();
        String senhha2 = editSenhaCadastro2.getText().toString().trim();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senhha2).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Snackbar snackbar = Snackbar.make(vw, "Cadastro Realizado", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });
    }
    public void telaLogin(View view) {
        Intent it = new Intent(getApplicationContext(), TelaLoginCadastro.class);
        startActivity(it);
    }

}