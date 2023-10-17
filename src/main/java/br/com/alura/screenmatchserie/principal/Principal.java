package br.com.alura.screenmatchserie.principal;

import br.com.alura.screenmatchserie.model.DadosEpisodio;
import br.com.alura.screenmatchserie.model.DadosSerie;
import br.com.alura.screenmatchserie.model.DadosTemporada;
import br.com.alura.screenmatchserie.model.Episodio;
import br.com.alura.screenmatchserie.service.ConsumoApi;
import br.com.alura.screenmatchserie.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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



        //Immprime dados de cada Temporada
        List<DadosTemporada> temporadas = new ArrayList<>();
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



        //Imprime os 5 melhores Episódios
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .map(e -> e.titulo().toUpperCase())
                //.peek(e -> System.out.println("Mapeamento: " + e))
                .forEach(System.out::println);



        //Imprime todos os episódios com sua temporada
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);



        //Busca o primeiro título do episodio encontrado //HJJJJJJJJ
        System.out.println("Digite o trecho do titulo do episódio:");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()) {
            System.out.println("Episodio encontrado!! \n " +
                    "Temporada: " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episódio não encontrado!");
        }



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



        //Imprime a avaliação de cada temporada
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);



        //Imprime estatísticas
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade de episódios avaliados: " + est.getCount());

    }

}
