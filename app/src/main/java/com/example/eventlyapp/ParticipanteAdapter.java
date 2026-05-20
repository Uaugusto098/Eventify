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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participante, parent, false);
        return new ParticipanteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipanteViewHolder holder, int position) {
        String participante = listaParticipantes.get(position);

        if (participante.contains("(") && participante.contains(")")) {
            int inicio = participante.indexOf("(");
            int fim = participante.indexOf(")");

            String nome = participante.substring(0, inicio).trim();
            String email = participante.substring(inicio + 1, fim).trim();

            holder.txtNome.setText(nome);
            holder.txtEmail.setText(email);
        } else {
            holder.txtNome.setText(participante);
            holder.txtEmail.setText(""); // ← evita NullPointerException
        }

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Participante: " + participante, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return listaParticipantes != null ? listaParticipantes.size() : 0;
    }

    public static class ParticipanteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        TextView txtEmail; // ← adicionado

        public ParticipanteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomeParticipante);
            txtEmail = itemView.findViewById(R.id.txtEmailParticipante); // ← adicionado
        }
    }
}