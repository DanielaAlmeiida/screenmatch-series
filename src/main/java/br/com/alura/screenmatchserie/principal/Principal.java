package br.com.alura.screenmatchserie.principal;

import br.com.alura.screenmatchserie.model.DadosEpisodio;
import br.com.alura.screenmatchserie.model.DadosSerie;
import br.com.alura.screenmatchserie.model.DadosTemporada;
import br.com.alura.screenmatchserie.model.Episodio;
import br.com.alura.screenmatchserie.service.ConsumoApi;
import br.com.alura.screenmatchserie.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    //"https://www.omdbapi.com/?t=gilmore+girls&apikey=6585022c");

    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);



        List<DadosTemporada> temporadas = new ArrayList<>();
        //Immprime dados de cada Temporada
        for (int i=1; i<= dadosSerie.totalTemporadas(); i++) {
            var jsonTemporada = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);


        //Imprime títulos dos Episódios
        temporadas.forEach(t ->
                t.episodios().forEach(e ->
                        System.out.println(e.titulo())
                )
        );


        //Para imprimir os 5 melhores Episódios
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);


        //Imprime todos os episódios com sua temporada
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);


        //Imprime todos os episódios a partir de um determinado Ano
        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter dataFormatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e ->
                        e.getDataLancamento() != null &&
                                e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " | Episódio: " + e.getTitulo() +
                                " | Data lançamento: " + e.getDataLancamento().format(dataFormatador)
                ));

    }

}
