package com.example.eventlyapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO {
    private DatabaseReference database;

    public EventoDAO() {
        this.database = FirebaseDatabase.getInstance().getReference("eventos");
        // MÁGICA AQUI: Mantém este nó específico sempre sincronizado em segundo plano
        this.database.keepSynced(true);
    }

    // CREATE / UPDATE
    public void salvar(Evento evento) {
        if (evento.getId() == null) {
            String id = database.push().getKey();
            evento.setId(id);
        }
        database.child(evento.getId()).setValue(evento);
    }

    // DELETE
    public void deletar(Evento evento) {
        database.child(evento.getId()).removeValue();
    }

    // READ OTIMIZADO (Sem delay e sem vazamento de memória)
    public void obterTodos(final EventoCallback callback) {
        // Mudamos para addListenerForSingleValueEvent para ler o cache local instantaneamente
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Evento> eventos = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Evento evento = postSnapshot.getValue(Evento.class);
                    if (evento != null) {
                        eventos.add(evento);
                    }
                }
                callback.onSucesso(eventos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Tratar erro aqui
            }
        });
    }

    public void limparTudo() {
        database.removeValue();
    }

    public interface EventoCallback {
        void onSucesso(List<Evento> lista);
    }
}