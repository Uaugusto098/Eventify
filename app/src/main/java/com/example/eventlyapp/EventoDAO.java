package com.example.eventlyapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        // 1. Pega o usuário logado atualmente
        FirebaseUser usuarioLogado = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioLogado != null) {
            String uidUsuario = usuarioLogado.getUid();
            // 2. Aponta para: usuarios_eventos -> UID_DO_CARA -> eventos
            // A MÁGICA ESTÁ AQUI NO FINAL DA LINHA 👇
            this.database = FirebaseDatabase.getInstance().getReference("usuarios_eventos").child(uidUsuario).child("eventos");
        } else {
            // Fallback de segurança: se por acaso tentar acessar sem logar (o que não deve acontecer no seu app)
            this.database = FirebaseDatabase.getInstance().getReference("eventos_deslogados");
        }

        // Mantém este nó específico sempre sincronizado em segundo plano
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


    public void obterParticipantes(String uidOrganizador, String eventoId, final ParticipantesCallback callback) {
        // Alinha a rota com a nova árvore de regras: usuarios_eventos -> UID -> eventos -> ID -> participantes
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("usuarios_eventos")
                .child(uidOrganizador)
                .child("eventos")
                .child(eventoId)
                .child("participantes");

        ref.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                List<String> listaParticipantes = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    // Captura a string "Nome (email)" enviada pelo site
                    String participante = child.getValue(String.class);
                    if (participante != null) {
                        listaParticipantes.add(participante);
                    }
                }
                callback.onSucesso(listaParticipantes);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // Tratamento de erro opcional (pode deixar vazio ou colocar um Log)
            }
        });
    }

    // Interface para o retorno dos participantes
    public interface ParticipantesCallback {
        void onSucesso(List<String> lista);
    }
}