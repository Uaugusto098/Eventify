package com.example.eventlyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TelaListar extends AppCompatActivity {

    private List<String> dados = new ArrayList<>(); // Lista dinâmica
    private String nomeEvento, detalhesEvento, emailDestino, eventoId;
    private ParticipanteAdapter adapter;
    private EventoDAO eventoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_listar);

        // Configuração de Insets (Preenchimento das barras do sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Seta de Voltar (Fecha a tela atual e limpa a pilha de Activities)
        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        // 2. Recuperar as informações alinhadas com as chaves do seu colega
        eventoId = getIntent().getStringExtra("id");
        nomeEvento = getIntent().getStringExtra("nome");
        detalhesEvento = getIntent().getStringExtra("descricao");

        // Mantido caso o organizador envie o email por essa chave externa
        emailDestino = getIntent().getStringExtra("EMAIL_ORGANIZADOR");

        // Tratamento de segurança: Garante que variáveis não fiquem nulas no relatório
        if (nomeEvento == null || nomeEvento.isEmpty()) nomeEvento = "Evento Sem Nome";
        if (detalhesEvento == null || detalhesEvento.isEmpty())
            detalhesEvento = "Sem descrição informada.";
        if (emailDestino == null || emailDestino.isEmpty()) emailDestino = "organizador@email.com";

        // 3. Configurar RecyclerView (Layout Manager e Adapter vinculados)
        RecyclerView rvParticipantes = findViewById(R.id.recyclerViewParticipantes);
        rvParticipantes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipanteAdapter(dados);
        rvParticipantes.setAdapter(adapter);

        // 4. Buscar dados REAIS no Firebase usando o caminho estruturado com o UID
        eventoDAO = new EventoDAO();
        if (eventoId != null && !eventoId.isEmpty()) {

            // CAPTURA O UID DO ORGANIZADOR LOGADO
            String uidOrganizador = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Passamos o uidOrganizador e o eventoId para o método atualizado
            eventoDAO.obterParticipantes(uidOrganizador, eventoId, new EventoDAO.ParticipantesCallback() {
                @Override
                public void onSucesso(List<String> lista) {
                    dados.clear();
                    dados.addAll(lista);
                    adapter.notifyDataSetChanged(); // Notifica o RecyclerView para renderizar
                }
            });
        } else {
            Toast.makeText(this, "Erro: ID do evento inválido.", Toast.LENGTH_SHORT).show();
        }

        // 5. Botão Enviar Relatório por E-mail
        Button btnRelatorio = findViewById(R.id.btnImprimirTodos);
        btnRelatorio.setOnClickListener(v -> dispararEmailAutomatico());
    }


// ... resto dos imports da sua classe


// ... resto dos imports da sua classe

    private void dispararEmailAutomatico() {
        if (dados.isEmpty()) {
            Toast.makeText(this, "Nenhum participante para enviar e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> listaEmails = new ArrayList<>();

        // 1. Extrair os e-mails de dentro da String "Nome (email@teste.com)"
        for (String participante : dados) {
            if (participante.contains("(") && participante.contains(")")) {
                int start = participante.indexOf("(") + 1;
                int end = participante.indexOf(")");
                String emailExtraido = participante.substring(start, end).trim();

                // Validação simples para garantir que pegamos um e-mail válido
                if (!emailExtraido.isEmpty() && emailExtraido.contains("@")) {
                    listaEmails.add(emailExtraido);
                }
            }
        }

        if (listaEmails.isEmpty()) {
            Toast.makeText(this, "Nenhum e-mail válido encontrado na lista.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Converter a lista de e-mails para o Array de Strings exigido pela Intent
        String[] emailsBcc = listaEmails.toArray(new String[0]);

        // 3. Construção do texto da mensagem
        StringBuilder corpo = new StringBuilder();
        corpo.append("Olá!\n\n");
        corpo.append("Obrigado por participar do evento: ").append(nomeEvento).append("\n\n");
        corpo.append("Detalhes: ").append(detalhesEvento).append("\n\n");
        corpo.append("Atenciosamente,\nOrganização do Evento.");

        // 4. Criar a Intent para abrir o aplicativo de E-mail externo
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(android.net.Uri.parse("mailto:")); // Filtra para abrir apenas apps de e-mail

        // Adiciona as informações pré-definidas
        intent.putExtra(Intent.EXTRA_BCC, emailsBcc); // Adiciona todos os participantes em Cópia Oculta
        intent.putExtra(Intent.EXTRA_SUBJECT, "Agradecimento pela presença: " + nomeEvento);
        intent.putExtra(Intent.EXTRA_TEXT, corpo.toString());

        // 5. Redirecionar o usuário para o app de e-mail escolhido
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir o aplicativo de e-mail.", Toast.LENGTH_SHORT).show();
        }
    }
}



