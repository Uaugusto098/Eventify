package com.example.eventlyapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormularioPresenca extends AppCompatActivity {

    private TextView txtSubtitulo, txtBtnConfirmar, txtErro;
    private EditText edtNome, edtEmail, edtCpf, edtTelefone;
    private LinearLayout btnConfirmar;

    private String eventoId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_presenca);

        inicializarViews();

        // CORREÇÃO 1: Alinhado com a chave "id" enviada pela TelaEventos pós-scanner
        eventoId           = getIntent().getStringExtra("id");
        String nomeEvento  = getIntent().getStringExtra("nomeEvento");
        String dataEvento  = getIntent().getStringExtra("dataEvento");

        if (eventoId   == null) eventoId   = "";
        if (nomeEvento == null) nomeEvento = "Evento";
        if (dataEvento == null) dataEvento = "";

        // Subtítulo dinâmico com nome e data do evento
        if (!nomeEvento.isEmpty())
            txtSubtitulo.setText("Confirme sua presença em: " + nomeEvento +
                    (dataEvento.isEmpty() ? "" : " · " + dataEvento));

        aplicarMascaraCpf();
        aplicarMascaraTelefone();

        btnConfirmar.setOnClickListener(v -> confirmarPresenca());
    }

    private void inicializarViews() {
        txtSubtitulo   = findViewById(R.id.txtSubtitulo);
        txtBtnConfirmar = findViewById(R.id.txtBtnConfirmar);
        txtErro        = findViewById(R.id.txtErro);
        edtNome        = findViewById(R.id.edtNome);
        edtEmail       = findViewById(R.id.edtEmail);
        edtCpf         = findViewById(R.id.edtCpf);
        edtTelefone    = findViewById(R.id.edtTelefone);
        btnConfirmar   = findViewById(R.id.btnConfirmar);
    }

    // ─── Validação e envio ───────────────────────────────────────────────────

    private void confirmarPresenca() {
        String nome     = edtNome.getText().toString().trim();
        String email    = edtEmail.getText().toString().trim();
        String cpf      = edtCpf.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

        if (!validarCampos(nome, email, cpf, telefone)) return;

        if (eventoId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do evento inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmar.setClickable(false);
        txtBtnConfirmar.setText("ENVIANDO...");
        txtErro.setVisibility(View.GONE);

        // CORREÇÃO 2: Aponta exatamente para o nó estruturado no seu EventoDAO
        // Caminho: eventos -> [id_do_evento] -> participantes -> [push_gerado]
        DatabaseReference participantesRef = FirebaseDatabase.getInstance()
                .getReference("eventos")
                .child(eventoId)
                .child("participantes")
                .push();

        // Formata a String contendo Nome e Email combinados para ser lida como texto pelo relatório
        String stringParticipante = nome + " (" + email + ")";

        // Salva a string formatada direto no banco
        participantesRef.setValue(stringParticipante)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "✓ Presença confirmada!", Toast.LENGTH_LONG).show();
                    limparCampos();
                    finish(); // Fecha o formulário e volta para a tela anterior
                })
                .addOnFailureListener(e -> {
                    txtErro.setText("✗ Erro ao salvar. Tente novamente.");
                    txtErro.setVisibility(View.VISIBLE);
                    btnConfirmar.setClickable(true);
                    txtBtnConfirmar.setText("CONFIRMAR CADASTRO");
                });
    }

    private boolean validarCampos(String nome, String email, String cpf, String telefone) {
        txtErro.setVisibility(View.GONE);

        if (nome.isEmpty() || email.isEmpty() || cpf.isEmpty() || telefone.isEmpty()) {
            txtErro.setText("⚠ Preencha todos os campos");
            txtErro.setVisibility(View.VISIBLE);
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtErro.setText("⚠ E-mail inválido");
            txtErro.setVisibility(View.VISIBLE);
            return false;
        }
        if (cpf.replaceAll("[^0-9]", "").length() != 11) {
            txtErro.setText("⚠ CPF inválido");
            txtErro.setVisibility(View.VISIBLE);
            return false;
        }
        if (telefone.replaceAll("[^0-9]", "").length() < 10) {
            txtErro.setText("⚠ Telefone inválido");
            txtErro.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void limparCampos() {
        edtNome.getText().clear();
        edtEmail.getText().clear();
        edtCpf.getText().clear();
        edtTelefone.getText().clear();
        btnConfirmar.setClickable(true);
        txtBtnConfirmar.setText("CONFIRMAR CADASTRO");
    }

    // ─── Máscara CPF ─────────────────────────────────────────────────────────

    private void aplicarMascaraCpf() {
        edtCpf.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 11) digits = digits.substring(0, 11);

                String masked;
                if (digits.length() > 9)
                    masked = digits.substring(0,3)+"."+digits.substring(3,6)+"."+digits.substring(6,9)+"-"+digits.substring(9);
                else if (digits.length() > 6)
                    masked = digits.substring(0,3)+"."+digits.substring(3,6)+"."+digits.substring(6);
                else if (digits.length() > 3)
                    masked = digits.substring(0,3)+"."+digits.substring(3);
                else
                    masked = digits;

                edtCpf.setText(masked);
                edtCpf.setSelection(masked.length());
                isUpdating = false;
            }
        });
    }

    // ─── Máscara Telefone ─────────────────────────────────────────────────────

    private void aplicarMascaraTelefone() {
        edtTelefone.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 11) digits = digits.substring(0, 11);

                String masked;
                if (digits.length() > 10)
                    masked = "("+digits.substring(0,2)+") "+digits.substring(2,7)+"-"+digits.substring(7);
                else if (digits.length() > 6)
                    masked = "("+digits.substring(0,2)+") "+digits.substring(2,6)+"-"+digits.substring(6);
                else if (digits.length() > 2)
                    masked = "("+digits.substring(0,2)+") "+digits.substring(2);
                else
                    masked = digits;

                edtTelefone.setText(masked);
                edtTelefone.setSelection(masked.length());
                isUpdating = false;
            }
        });
    }

    public void sair(View view) {
        finish();
    }
}