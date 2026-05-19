package com.example.eventlyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParticipanteAdapter extends RecyclerView.Adapter<ParticipanteAdapter.ParticipanteViewHolder> {

    private final List<String> listaParticipantes;

    public ParticipanteAdapter(List<String> lista) {
        this.listaParticipantes = lista;
    }

    @NonNull
    @Override
    public ParticipanteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do card (item_participante.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participante, parent, false);
        return new ParticipanteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipanteViewHolder holder, int position) {
        String nome = listaParticipantes.get(position);
        holder.txtNome.setText(nome);

        // 3. Clique simplificado (Opcional)
        // Como o relatório agora é geral, o clique no nome pode apenas mostrar um aviso
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Participante: " + nome, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaParticipantes != null ? listaParticipantes.size() : 0;
    }

    public static class ParticipanteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        public ParticipanteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referência ao TextView do seu item_participante.xml
            txtNome = itemView.findViewById(R.id.txtNomeParticipante);
        }
    }
}