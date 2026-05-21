package com.example.eventlyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
    private Button btnImprimirdados;

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

        btnImprimirdados = findViewById(R.id.btnImprimirTodos2);
        btnImprimirdados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dados.isEmpty()) {
                    Toast.makeText(TelaListar.this, "A lista está vazia. Não há dados para gerar o PDF.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. Captura o e-mail do usuário logado no Firebase Auth
                com.google.firebase.auth.FirebaseUser usuarioLogado = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (usuarioLogado != null && usuarioLogado.getEmail() != null) {
                    String emailLogado = usuarioLogado.getEmail();

                    // 2. Chama a função para criar o PDF e disparar o e-mail
                    gerarPdfEEnviar(emailLogado);
                } else {
                    Toast.makeText(TelaListar.this, "Erro: Usuário não autenticado ou e-mail não encontrado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


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
    private void gerarPdfEEnviar(String emailDestinatario) {
        // 1. Criar o documento PDF (Exatamente como estava)
        android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
        android.graphics.pdf.PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        android.graphics.Canvas canvas = page.getCanvas();
        android.graphics.Paint paint = new android.graphics.Paint();

        int x = 40; int y = 50;
        paint.setTextSize(18f); paint.setFakeBoldText(true);
        canvas.drawText("Relatório de Participantes", x, y, paint);

        y += 15; paint.setStrokeWidth(1f); canvas.drawLine(x, y, 555, y, paint);

        y += 30; paint.setTextSize(12f); paint.setFakeBoldText(false);
        canvas.drawText("Evento: " + nomeEvento, x, y, paint);

        y += 20; canvas.drawText("Total de Participantes: " + dados.size(), x, y, paint);

        y += 20; canvas.drawLine(x, y, 555, y, paint);

        y += 30; paint.setFakeBoldText(true); canvas.drawText("Lista de Presença:", x, y, paint);
        paint.setFakeBoldText(false);

        y += 20;
        for (String participante : dados) {
            if (y > 800) {
                canvas.drawText("... (Lista continua no sistema)", x, y, paint);
                break;
            }
            canvas.drawText("- " + participante, x, y, paint);
            y += 20;
        }

        pdfDocument.finishPage(page);

        // 2. Salvar o arquivo PDF
        java.io.File arquivoPdf = new java.io.File(getCacheDir(), "Relatorio_Eventify.pdf");
        try {
            pdfDocument.writeTo(new java.io.FileOutputStream(arquivoPdf));
        } catch (java.io.IOException e) {
            Toast.makeText(this, "Erro ao gerar PDF.", Toast.LENGTH_SHORT).show();
            pdfDocument.close();
            return;
        }
        pdfDocument.close();

        // 3. Avisa o usuário que o processo começou e chama a função de envio silencioso
        Toast.makeText(this, "Gerando e enviando relatório...", Toast.LENGTH_SHORT).show();
        enviarEmailSilencioso(emailDestinatario, arquivoPdf);
    }
    private void enviarEmailSilencioso(String destinatario, java.io.File anexoPdf) {
        new Thread(() -> {
            try {
                // 1. O e-mail e senha "remetente" do seu App (Leia a observação abaixo)
                String emailRemetente = "appeventify3@gmail.com";
                String senhaAppRemetente = "zzwy nmll huxx gegr";

                // 2. Configurar o servidor do Gmail
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                // 3. Autenticação
                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailRemetente, senhaAppRemetente);
                    }
                });

                // 4. Montar o e-mail
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailRemetente));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                message.setSubject("Relatório do Evento - " + nomeEvento);

                // Parte do texto
                javax.mail.internet.MimeBodyPart corpoTexto = new javax.mail.internet.MimeBodyPart();
                corpoTexto.setText("Olá!\n\nSegue em anexo o relatório atualizado do evento " + nomeEvento + ".\nQuantidade total: " + dados.size() + " participantes.");

                // Parte do anexo (O PDF)
                javax.mail.internet.MimeBodyPart corpoAnexo = new javax.mail.internet.MimeBodyPart();
                corpoAnexo.attachFile(anexoPdf);

                // Juntar texto + anexo
                javax.mail.Multipart multipart = new javax.mail.internet.MimeMultipart();
                multipart.addBodyPart(corpoTexto);
                multipart.addBodyPart(corpoAnexo);

                message.setContent(multipart);

                // 5. Enviar o E-mail!
                Transport.send(message);

                // 6. Voltar para a tela principal para mostrar a mensagem de sucesso
                runOnUiThread(() -> Toast.makeText(TelaListar.this, "E-mail enviado com sucesso!", Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TelaListar.this, "Erro no envio. Verifique a conexão e as credenciais.", Toast.LENGTH_LONG).show());
            }
        }).start(); // Inicia a Thread em segundo plano
    }
}



