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

import java.util.ArrayList;
import java.util.List;

public class TelaEventos extends AppCompatActivity {

    private ListView listView;
    private ImageView imgNenhumEvento;
    private ArrayList<String> nomesEventos; // Lista de strings para o ArrayAdapter simples
    private ArrayAdapter<String> adapter;
    private EventoDAO dao;
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
        listView = findViewById(R.id.lsvEventos);

        // 2. Inicializar a lista e o Adapter (começa vazia)
        nomesEventos = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_lista,
                R.id.txtNomeEvento,
                nomesEventos
        );
        listView.setAdapter(adapter);

        // 3. Chamar o banco de dados
        dao = new EventoDAO();
        String[] tiposDeEventos = {"Show de Rock", "Palestra Tech", "Workshop Culinária", "Stand-up Comedy", "Hackathon", "Feira de Livros"};

        for (int i = 0; i < 8; i++) {
            Evento ev = new Evento();
            // Escolhe um nome da lista acima + o número do loop para ser diferente
            String nomeSorteado = tiposDeEventos[i % tiposDeEventos.length];
            ev.setNome(nomeSorteado + " #" + (i + 1));

            // Salva no Firebase
            dao.salvar(ev);
        }
        dao.limparTudo();
        atualizarListaDoBanco();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_adicionar) {
                // Ação quando clicar em "Meus Eventos"
                // Exemplo: recarregar sua lista do banco
                it = new Intent(getApplicationContext(), TelaCadastrarEvento.class);
                startActivity(it);
                return true;

            } else if (id == R.id.nav_camera) {
                // Ação quando clicar em "Disponíveis"
                // Exemplo: mostrar um Toast por enquanto
                Toast.makeText(this, "Carregando eventos disponíveis...", Toast.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.nav_sair) {
                // Ação quando clicar em "Perfil"
                finish();
                return true;
            }

            return false;
        });
    }

    private void atualizarListaDoBanco() {
        // Chamada assíncrona do seu DAO
        dao.obterTodos(new EventoDAO.EventoCallback() {
            @Override
            public void onSucesso(List<Evento> listaRecebida) {
                // Limpamos a lista de nomes atual
                nomesEventos.clear();

                // Como seu ArrayAdapter é de String, vamos extrair apenas os nomes dos objetos Evento
                for (Evento e : listaRecebida) {
                    nomesEventos.add(e.getNome());
                }

                // Notificamos o adapter que os dados mudaram para ele atualizar a tela
                adapter.notifyDataSetChanged();

                // Lógica de visibilidade da imagem de "vazio"
                if (nomesEventos.isEmpty()) {
                    imgNenhumEvento.setVisibility(VISIBLE);
                    listView.setVisibility(GONE);
                } else {
                    imgNenhumEvento.setVisibility(GONE);
                    listView.setVisibility(VISIBLE);
                }
            }
        });
    }
}