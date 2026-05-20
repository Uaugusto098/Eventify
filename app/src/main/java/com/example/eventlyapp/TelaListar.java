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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private void dispararEmailAutomatico() {
        if (dados.isEmpty()) {
            Toast.makeText(this, "Nenhum participante para enviar e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Extrair os e-mails
        List<String> listaEmails = new ArrayList<>();
        for (String participante : dados) {
            if (participante.contains("(") && participante.contains(")")) {
                int start = participante.indexOf("(") + 1;
                int end = participante.indexOf(")");
                String emailExtraido = participante.substring(start, end).trim();

                if (!emailExtraido.isEmpty() && emailExtraido.contains("@")) {
                    listaEmails.add(emailExtraido);
                }
            }
        }

        if (listaEmails.isEmpty()) {
            Toast.makeText(this, "Nenhum e-mail válido encontrado na lista.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Troca o texto do botão para mostrar que está processando
        Button btnRelatorio = findViewById(R.id.btnImprimirTodos);
        btnRelatorio.setText("ENVIANDO...");
        btnRelatorio.setEnabled(false);

        // 2. Executar o envio em segundo plano para não travar a tela
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean sucesso = enviarEmailJavaMail(listaEmails);

            // 3. Voltar para a Thread principal para atualizar a interface (Toast e Botão)
            runOnUiThread(() -> {
                btnRelatorio.setText("ENVIAR E-MAIL");
                btnRelatorio.setEnabled(true);

                if (sucesso) {
                    Toast.makeText(this, "E-mails enviados com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Erro ao enviar e-mails. Verifique as credenciais.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean enviarEmailJavaMail(List<String> listaEmails) {
        // --- COLOQUE SUAS CREDENCIAIS AQUI ---
        final String remetenteEmail = "SEU_EMAIL@gmail.com";
        final String remetenteSenha = "SUA_SENHA_DE_APP_DE_16_DIGITOS"; // A senha gerada no Google

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remetenteEmail, remetenteSenha);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remetenteEmail));

            // Define o assunto e corpo
            message.setSubject("Agradecimento pela presença: " + nomeEvento);
            String corpo = "Olá!\n\n" +
                    "Obrigado por participar do evento: " + nomeEvento + "\n\n" +
                    "Detalhes: " + detalhesEvento + "\n\n" +
                    "Atenciosamente,\nOrganização do Evento.";
            message.setText(corpo);

            // Adiciona todos os e-mails como Cópia Oculta (BCC)
            for (String emailDestino : listaEmails) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailDestino));
            }

            // Dispara o e-mail via servidor SMTP
            Transport.send(message);
            return true; // Sucesso

        } catch (MessagingException e) {
            e.printStackTrace();
            return false; // Falha
        }
    }
}
