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
        // "eventos" é o nome do nó principal no seu banco de dados
        this.database = FirebaseDatabase.getInstance().getReference("eventos");
    }

    // CREATE / UPDATE
    public void salvar(Evento evento) {
        if (evento.getId() == null) {
            // Gera um ID único se for um novo evento
            String id = database.push().getKey();
            evento.setId(id);
        }
        // No Firebase, o setValue serve tanto para inserir quanto para atualizar
        database.child(evento.getId()).setValue(evento);
    }

    // DELETE
    public void deletar(Evento evento) {
        database.child(evento.getId()).removeValue();
    }

    // READ (O CRUD assíncrono)
    public void obterTodos(final EventoCallback callback) {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Evento> eventos = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Evento evento = postSnapshot.getValue(Evento.class);
                    eventos.add(evento);
                }
                // Avisa quem chamou que os dados chegaram
                callback.onSucesso(eventos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Tratar erro aqui
            }
        });
    }

    // Interface para lidar com a natureza assíncrona do Firebase
    public interface EventoCallback {
        void onSucesso(List<Evento> lista);
    }

}
