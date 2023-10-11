package br.com.alura.screenmatchserie.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
