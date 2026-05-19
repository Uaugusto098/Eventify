package com.example.eventlyapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.List;

public class TelaEventos extends AppCompatActivity {

    private ListView lsvDados;
    private ImageView imgNenhumEvento;
    private ArrayList<String> nomesEventos;
    private EventoAdapter adapter;
    private EventoDAO dao;

    private List<Evento> listaCompletaEventos = new ArrayList<>();
    private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_eventos);

        // Ajuste de preenchimento para respeitar as barras de sistema e esticar a Navbar azul
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar componentes da UI
        imgNenhumEvento = findViewById(R.id.imgNenhumEvento);
        lsvDados = findViewById(R.id.lsvEventos);

        // 2. Inicializar a lista e o Adapter vinculado
        nomesEventos = new ArrayList<>();
        adapter = new EventoAdapter(this, listaCompletaEventos);
        lsvDados.setAdapter(adapter);

        // 3. Inicializar o Banco de Dados (DAO)
        dao = new EventoDAO();

        // 4. Clique em um evento da lista para abrir os detalhes (TelaEvento)
        lsvDados.setOnItemClickListener((parent, view, position, id) -> {
            Evento clienteClicado = listaCompletaEventos.get(position);
            it = new Intent(getApplicationContext(), TelaEvento.class);

            if ("Evento sem descrição".equals(clienteClicado.getDescricao())) {
                clienteClicado.setDescricao("");
            }
            if ("Sem data marcada".equals(clienteClicado.getData())) {
                clienteClicado.setData("");
            }

            it.putExtra("id", clienteClicado.getId());
            it.putExtra("nome", clienteClicado.getNome());
            it.putExtra("data", clienteClicado.getData());
            it.putExtra("descricao", clienteClicado.getDescricao());
            it.putExtra("imagem", clienteClicado.getImagemUri());

            startActivity(it);
        });

        // 5. Configuração das ações da barra de navegação inferior (Navbar)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_adicionar) {
                // Abre a tela de cadastro de novos eventos
                it = new Intent(TelaEventos.this, TelaCadastrarEvento.class);
                startActivity(it);
                return true;

            } else if (id == R.id.nav_camera) {
                // Configuração do leitor de QR Code (Otimizado)
                GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .enableAutoZoom()
                        .build();

                GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(TelaEventos.this, options);

                scanner.startScan()
                        .addOnSuccessListener(barcode -> {
                            String conteudoQr = barcode.getRawValue();

                            if (conteudoQr != null && !conteudoQr.isEmpty()) {

                                // CASO 1: O QR Code é um link de internet externo
                                if (conteudoQr.startsWith("http://") || conteudoQr.startsWith("https://")) {
                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEventos.this);
                                    builder.setTitle("Deseja ser redirecionado?");
                                    builder.setMessage("Link: " + conteudoQr);
                                    builder.setPositiveButton("Sim", (dialog, which) -> {
                                        Intent intentNavegador = new Intent(Intent.ACTION_VIEW);
                                        intentNavegador.setData(android.net.Uri.parse(conteudoQr));
                                        startActivity(intentNavegador);
                                    });
                                    builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                    builder.show();

                                } else {
                                    // CASO 2: O QR Code é o ID de validação do Evento (Check-in)
                                    String idEventoEscaneado = conteudoQr;

                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEventos.this);
                                    builder.setTitle("Validar Acesso");
                                    builder.setMessage("Deseja confirmar sua presença neste evento?");

                                    builder.setPositiveButton("Confirmar", (dialog, which) -> {
                                        // Dados do participante unificados (Modificar futuramente para puxar dados do login)
                                        String dadosParticipante = "Aluno Fatec (aluno@fatec.edu.br)";

                                        // Salva na mesma estrutura esperada pelo EventoDAO.obterParticipantes()
                                        FirebaseDatabase.getInstance()
                                                .getReference("eventos")
                                                .child(idEventoEscaneado)
                                                .child("participantes")
                                                .push() // Garante uma chave única para a inscrição
                                                .setValue(dadosParticipante)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Acesso Liberado! Presença registrada.", Toast.LENGTH_LONG).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Erro ao registrar presença no banco.", Toast.LENGTH_SHORT).show();
                                                });
                                    });

                                    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
                                    builder.show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erro ao ler QR Code", Toast.LENGTH_SHORT).show();
                        });

                return true;

            } else if (id == R.id.nav_sair) {
                // Caixa de diálogo para logout seguro da conta
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEventos.this);
                builder.setTitle("Sair da Conta");
                builder.setMessage("Deseja realmente sair da sua conta?");

                builder.setPositiveButton("Sim", (dialog, which) -> {
                    Intent intentSair = new Intent(TelaEventos.this, TelaLoginCadastro.class);
                    startActivity(intentSair);
                    finish();
                });

                builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                builder.show();

                return true;
            }

            return false;
        });
    }

    // Método responsável por buscar os dados no Firebase de forma síncrona/cacheada
    private void atualizarListaDoBanco() {
        imgNenhumEvento.setVisibility(GONE);

        dao.obterTodos(new EventoDAO.EventoCallback() {
            @Override
            public void onSucesso(List<Evento> listaRecebida) {
                listaCompletaEventos.clear();
                listaCompletaEventos.addAll(listaRecebida);
                adapter.notifyDataSetChanged();

                if (listaCompletaEventos.isEmpty()) {
                    imgNenhumEvento.setVisibility(VISIBLE);
                    lsvDados.setVisibility(GONE);
                } else {
                    imgNenhumEvento.setVisibility(GONE);
                    lsvDados.setVisibility(VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que a lista atualize sempre que o usuário voltar para essa tela
        atualizarListaDoBanco();
    }
}