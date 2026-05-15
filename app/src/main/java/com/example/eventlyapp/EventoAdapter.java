package com.example.eventlyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



import java.util.List;

public class EventoAdapter extends ArrayAdapter<Evento> {
    private Context context;
    private List<Evento> eventos;

    public EventoAdapter(Context context, List<Evento> eventos) {
        super(context, 0, eventos);
        this.context = context;
        this.eventos = eventos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Inflar o layout do item (item_lista.xml)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lista, parent, false);
        }

        // 2. Pegar o evento da posição atual
        Evento eventoAtual = eventos.get(position);

        // 3. Referenciar os TextViews do seu XML item_lista
        TextView txtNome = convertView.findViewById(R.id.txtNomeEvento);
        TextView txtDesc = convertView.findViewById(R.id.txtNomeEvento2);
        TextView txtData = convertView.findViewById(R.id.txtDataEvento);

        // 4. Setar os valores do objeto no XML
        if (eventoAtual != null) {
            txtNome.setText(eventoAtual.getNome());
            txtDesc.setText(eventoAtual.getDescricao());
            txtData.setText(eventoAtual.getData());
        }

        return convertView;
    }
}