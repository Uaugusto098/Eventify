package com.example.eventlyapp;

import java.util.Date;

public class Evento {
    private String id;
    private String nome;
    private String data;
    private  String Descricao;
    private String imagemUri;
    public Evento(){

    }

    public Evento(String id, String nome, String data, String descricao, String imagemUri) {
        this.id = id;
        this.nome = nome;
        this.data = data;
        Descricao = descricao;
        this.imagemUri = imagemUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return Descricao;
    }

    public void setDescricao(String descricao) {
        Descricao = descricao;
    }

    public String getImagemUri() {
        return imagemUri;
    }

    public void setImagemUri(String imagemUri) {
        this.imagemUri = imagemUri;
    }
}
