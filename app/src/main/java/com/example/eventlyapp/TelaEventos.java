package com.example.eventlyapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importe seu DAO e sua Model
import com.example.eventlyapp.EventoDAO;
import com.example.eventlyapp.Evento;

import java.util.ArrayList;
import java.util.List;

public class TelaEventos extends AppCompatActivity {

    private ListView listView;
    private ImageView imgNenhumEvento;
    private ArrayList<String> nomesEventos; // Lista de strings para o ArrayAdapter simples
    private ArrayAdapter<String> adapter;
    private EventoDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_eventos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        Evento evento = new Evento("1","teste","02/05/2006","descricao","1");
        dao.salvar(evento);
        atualizarListaDoBanco();
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