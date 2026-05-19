package com.example.eventlyapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class TelaEvento extends AppCompatActivity {

    private String id, img, nomeOriginal, dataOriginal, descricaoOriginal;
    private TextInputEditText descricaoInput, nomeInput, dataInput;

    // Bitmap global para guardar o QR Code gerado para impressão
    private Bitmap currentQrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_evento);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Inicialização das Views padrão (XML principal)
        TextView txtvoltar = findViewById(R.id.txtBack1);
        TextInputLayout descricaoInfo = findViewById(R.id.descricaoInfo);
        TextInputLayout nomeInfo = findViewById(R.id.nomeInfo);
        TextInputLayout dataInfo = findViewById(R.id.dataInfo);
        descricaoInput = findViewById(R.id.descricaoInput);
        nomeInput = findViewById(R.id.nomeInput);
        dataInput = findViewById(R.id.dataInput);

        // Recebimento de dados da Intent
        Intent intentRecebida = getIntent();
        id = intentRecebida.getStringExtra("id");
        img = intentRecebida.getStringExtra("imagem");
        nomeOriginal = intentRecebida.getStringExtra("nome");
        dataOriginal = intentRecebida.getStringExtra("data");
        descricaoOriginal = intentRecebida.getStringExtra("descricao");

        descricaoInput.setText(descricaoOriginal);
        nomeInput.setText(nomeOriginal);
        dataInput.setText(dataOriginal);

        // Configuração dos botões Alterar e Excluir
        Button btnExcluir = findViewById(R.id.btnExcluir);
        Button btnAlterar = findViewById(R.id.btnAlterar);

        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEvento.this);
                builder.setTitle("Excluir Evento");
                builder.setMessage("Deseja realmente excluir o evento?");
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    EventoDAO dao = new EventoDAO();
                    Evento evento = new Evento();
                    evento.setId(id);
                    dao.deletar(evento);
                    finish();
                });
                builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
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
                if(nomeInput.getText().toString().equals(nomeOriginal) && dataInput.getText().toString().equals(dataOriginal) && descricaoInput.getText().toString().equals(descricaoOriginal)){
                    Snackbar.make(findViewById(android.R.id.content), "É preciso alterar pelo menos um campo para alterar ", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#1D2F46"))
                            .setTextColor(Color.WHITE)
                            .show();
                    return;
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEvento.this);
                builder.setTitle("Alterar Evento");
                builder.setMessage("Deseja realmente alterar o evento?");
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    EventoDAO dao = new EventoDAO();
                    Evento evento = new Evento();
                    evento.setId(id);
                    evento.setNome(nomeInput.getText().toString());

                    if (dataInput.getText().toString().isEmpty()){
                        dataInput.setText("Sem data marcada");
                    }
                    if (descricaoInput.getText().toString().isEmpty()){
                        descricaoInput.setText("Evento sem descrição");
                    }

                    evento.setData(dataInput.getText().toString());
                    evento.setDescricao(descricaoInput.getText().toString());
                    evento.setImagemUri(img);

                    dao.salvar(evento);
                    finish();
                });
                builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });

        // --- CONFIGURAÇÃO DO BOTTOM NAVIGATION VIEW (Ações da Barra Inferior) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int menuItemId = item.getItemId();

            if (menuItemId == R.id.nav_listar) {
                // Abre a tela de listagem
                Intent intentListar = new Intent(TelaEvento.this, TelaListar.class);
                intentListar.putExtra("id", id);
                intentListar.putExtra("nome", nomeInput.getText().toString());
                intentListar.putExtra("descricao", descricaoInput.getText().toString());
                intentListar.putExtra("EMAIL_ORGANIZADOR", "organizador@fatec.com");
                startActivity(intentListar);
                return true;

            } else if (menuItemId == R.id.nav_qrcode) {
                // ESTRATÉGIA ATUALIZADA: O Host agora gera um QR Code que é um Link Web contendo o ID do evento
                if (id != null && !id.isEmpty()) {

                    // Substitua pelo link real da página web do formulário do seu grupo
                    String linkFormularioWeb = "https://seuformulario.com/evento=" + id;

                    // Envia o link completo para ser transformado em QR Code dentro do diálogo flutuante
                    exibirDialogQrCode(linkFormularioWeb);

                } else {
                    Toast.makeText(TelaEvento.this, "ID do evento inválido ou ausente.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    // --- MÉTODOS AUXILIARES: GERAÇÃO E EXIBIÇÃO EM DIÁLOGO SOBREPOSTO ---

    private Bitmap gerarQrCode(String conteudo) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Gera um QR Code de 500x500px para ficar bem visível no diálogo
            return barcodeEncoder.encodeBitmap(conteudo, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void exibirDialogQrCode(String linkCompleto) {
        // Agora ele gera o QR Code com a URL, permitindo que quem tem ou não o app consiga acessar
        currentQrCodeBitmap = gerarQrCode(linkCompleto);

        if (currentQrCodeBitmap != null) {
            // 1. Criar o construtor do AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(TelaEvento.this);

            // 2. Inflar o layout customizado (dialog_qrcode.xml)
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_qrcode, null);
            builder.setView(dialogView);

            // 3. Inicializar as Views do layout inflado
            ImageView imgQrCodeDialog = dialogView.findViewById(R.id.imgQrCodeDialog);
            Button btnImprimirDialog = dialogView.findViewById(R.id.btnImprimirDialog);
            Button btnFecharDialog = dialogView.findViewById(R.id.btnFecharDialog);

            // 4. Injetar o QR Code gerado na ImageView do diálogo
            imgQrCodeDialog.setImageBitmap(currentQrCodeBitmap);

            // 5. Criar o AlertDialog
            AlertDialog dialog = builder.create();

            // 6. Configurar as ações dos botões dentro do diálogo
            btnImprimirDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Feedback de impressão
                    Toast.makeText(TelaEvento.this, "Enviando para a impressora térmica...", Toast.LENGTH_SHORT).show();
                    // Aqui entrará o comando físico da biblioteca (ex: printer.printImage(currentQrCodeBitmap);)
                }
            });

            btnFecharDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss(); // Fecha o diálogo flutuante
                }
            });

            // 7. Exibir o diálogo (ele aparecerá por cima de toda a tela)
            dialog.show();

        } else {
            Toast.makeText(this, "Erro ao gerar a imagem do QR Code.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sair(View view){
        finish();
    }
}