package com.example.eventlyapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importe seu DAO e sua Model
import com.example.eventlyapp.EventoDAO;
import com.example.eventlyapp.Evento;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.List;

public class TelaEventos extends AppCompatActivity {

    private ListView lsvDados;
    private ImageView imgNenhumEvento;
    private ArrayList<String> nomesEventos; // Lista de strings para o ArrayAdapter simples
    private EventoAdapter adapter;
    private EventoDAO dao;


    private List<Evento> listaCompletaEventos = new ArrayList<>();


    private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_eventos);

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

        // 1. Inicializar componentes da UI

        imgNenhumEvento = findViewById(R.id.imgNenhumEvento);
        lsvDados = findViewById(R.id.lsvEventos);

        // 2. Inicializar a lista e o Adapter (começa vazia)
        nomesEventos = new ArrayList<>();
        adapter = new EventoAdapter(this, listaCompletaEventos);
        lsvDados.setAdapter(adapter);


        // 3. Chamar o banco de dados
        dao = new EventoDAO();

        /*
        dao.limparTudo();
        String[] tiposDeEventos = {"Show de Rock", "Palestra Tech", "Workshop Culinária", "Stand-up Comedy", "Hackathon", "Feira de Livros"};
        String[] datas = {"20/05/2026", "12/06/2026", "05/07/2026", "18/08/2026", "30/09/2026", "10/10/2026"};
        String[] descricoes = {
                "Uma noite inesquecível com as melhores bandas locais.",
                "Tudo sobre as novas tendências de Inteligência Artificial.",
                "Aprenda a fazer massas artesanais com um Chef profissional.",
                "Prepare-se para rir muito com os melhores comediantes.",
                "48 horas de programação intensa e muita pizza.",
                "Milhares de títulos com descontos incríveis para leitores."
        };

// 2. Rodamos o loop (aumentei para 6 para usar todos os dados acima)
        for (int i = 0; i < 6; i++) {
            Evento ev = new Evento();

            // O operador % (módulo) garante que não dê erro se o loop for maior que a lista
            ev.setNome(tiposDeEventos[i % tiposDeEventos.length]);
            ev.setData(datas[i % datas.length]);
            ev.setDescricao(descricoes[i % descricoes.length]);

            // Se você tiver um campo de imagem, pode setar um placeholder ou algo assim:
            // ev.setImagemUri("android.resource://com.example.eventlyapp/" + R.drawable.logoapp);

            dao.salvar(ev);
        }
         */




        lsvDados.setOnItemClickListener((parent, view, position, id) -> {
            // Pega o objeto que foi clicado
            Evento clienteClicado = listaCompletaEventos.get(position);
            // Exemplo: Mostrar o nome do cliente ou abrir uma nova tela
            it =  new Intent(getApplicationContext(), TelaEvento.class);
            if(clienteClicado.getDescricao().equals("Evento sem descrição")){
                clienteClicado.setDescricao("");
            }
            if(clienteClicado.getData().equals("Sem data marcada")){
                clienteClicado.setData("");
            }
            it.putExtra("id", clienteClicado.getId());
            it.putExtra("nome", clienteClicado.getNome());
            it.putExtra("data", clienteClicado.getData());
            it.putExtra("descricao", clienteClicado.getDescricao());
            it.putExtra("imagem", clienteClicado.getImagemUri());

            startActivity(it);
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_adicionar) {
                // Ação quando clicar em "Meus Eventos"
                // Exemplo: recarregar sua lista do banco
                it = new Intent(TelaEventos.this, TelaCadastrarEvento.class);
                startActivity(it);
                return true;

            } else if (id == R.id.nav_camera) {
                // 1. Configurar as opções (apenas QR Code para ser mais rápido)
                GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .enableAutoZoom() // Ajuda se o QR estiver longe
                        .build();

                // 2. Inicializar o Scanner
                GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(TelaEventos.this, options);

                // 3. Abrir a câmera e processar o resultado
                scanner.startScan()
                        .addOnSuccessListener(barcode -> {
                            // 1. Pega o link que está dentro do QR Code
                            String url = barcode.getRawValue();

                            // 2. Verifica se o valor realmente parece um link (começa com http)
                            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {

                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEventos.this);

                                // 2. Configurar o título e a mensagem
                                builder.setTitle("Deseja ser redirecionado?");
                                builder.setMessage("Link:"+ url);

                                // 3. Botão de confirmação (Sim)
                                builder.setPositiveButton("Sim", (dialog, which) -> {
                                    // Se clicar em sim, ele executa a sua lógica de sair
                                    Intent intentNavegador = new Intent(Intent.ACTION_VIEW);
                                    intentNavegador.setData(android.net.Uri.parse(url));

                                    // 4. Inicia a atividade (abre o Chrome/Samsung Internet, etc.)
                                    startActivity(intentNavegador);
                                });

                                // 4. Botão de cancelamento (Não)
                                builder.setNegativeButton("Não", (dialog, which) -> {
                                    // Se clicar em não, apenas fecha o card e não faz nada
                                    dialog.dismiss();
                                });

                                // 5. Exibir o card na tela
                                builder.show();
                                // 3. Cria a Intent de visualização


                            } else {
                                // Se o QR Code for apenas um texto e não um link
                                Toast.makeText(this, "Conteúdo do QR não é um link: " + url, Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erro ao ler QR Code", Toast.LENGTH_SHORT).show();
                        });

                return true;

            } else if (id == R.id.nav_sair) {
                // Ação quando clicar em "sair"
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TelaEventos.this);

                // 2. Configurar o título e a mensagem
                builder.setTitle("Sair da Conta");
                builder.setMessage("Deseja realmente sair da sua conta?");

                // 3. Botão de confirmação (Sim)
                builder.setPositiveButton("Sim", (dialog, which) -> {
                    // Se clicar em sim, ele executa a sua lógica de sair
                    Intent intentSair = new Intent(TelaEventos.this, TelaLoginCadastro.class);
                    startActivity(intentSair);
                    finish(); // Fecha a tela atual para ele não conseguir voltar no botão "back"
                });

                // 4. Botão de cancelamento (Não)
                builder.setNegativeButton("Não", (dialog, which) -> {
                    // Se clicar em não, apenas fecha o card e não faz nada
                    dialog.dismiss();
                });

                // 5. Exibir o card na tela
                builder.show();

                return true;
            }

            return false;
        });
    }

    private void atualizarListaDoBanco() {
        // 1. Mostra o loading e esconde a lista/imagem vazia antes de buscar


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
        super.onResume(); // Pronto, agora está correto!

        // Recarrega a lista toda vez que a tela voltar a ficar ativa
        atualizarListaDoBanco();
    }
}