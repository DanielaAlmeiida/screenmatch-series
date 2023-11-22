package br.com.alura.screenmatchserie.principal;

import br.com.alura.screenmatchserie.model.*;
import br.com.alura.screenmatchserie.repository.SerieRepository;
import br.com.alura.screenmatchserie.service.ConsumoApi;
import br.com.alura.screenmatchserie.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private SerieRepository serieRepository;
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    //"https://www.omdbapi.com/?t=gilmore+girls&apikey=6585022c");


    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0 ) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo
                    5 - Buscar série(s) por ator
                    6 - Top 5 séries
                    7 - Buscar séries por gênero
                    8 - Buscar séries por quantidade de temporadas e avaliação
                    9 - Buscar episódios por trecho
                    10 - Buscar top 5 episódios de uma série
                    11 - Buscar episódios a partir de uma data
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Serie();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorQuantidadeTemporadasEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosAposUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var jsonSerie = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dadosSerie = conversor.obterDados(jsonSerie, DadosSerie.class);
        return dadosSerie;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var jsonTemporada = consumoApi.obterDados(
                        ENDERECO
                                + serieEncontrada.getTitulo().replace(" ", "+")
                                + "&season="
                                + i
                                + API_KEY
                );
                DadosTemporada dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);

            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome do ator para busca de série(s)?");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de qual valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = serieRepository.
                findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));

    }

    private void buscarTop5Serie() {
        List<Serie> seriesTop = serieRepository.findTop5ByOrderByAvaliacaoDesc();

        seriesTop.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de qual gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriesPorQuantidadeTemporadasEAvaliacao() {
        System.out.println("Até quantas temporadas você quer que a série tenha?");
        var qntdTemporadas = leitura.nextInt();

        System.out.println("A partir de qual avaliação?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesPorQuantidadeTemporadasEAvaliacao = serieRepository
                .seriesPorTemporadaEAValiacao(qntdTemporadas, avaliacao);

        System.out.println("Séries com até " + qntdTemporadas + " temporadas e avaliação maior que " + avaliacao + ":");
        seriesPorQuantidadeTemporadasEAvaliacao.forEach(System.out::println);

    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para busca?");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trechoEpisodio);

        System.out.println("Episódios com o trecho: '" + trechoEpisodio + "':");
        episodiosEncontrados.forEach(e ->
                        System.out.println(
                                "Série: " + e.getSerie().getTitulo() +
                                " - Temporada: " + e.getTemporada()+
                                " - Episódio: " + e.getNumeroEpisodio()+ " - " + e.getTitulo())
                );
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> top5Episodios = serieRepository.topEpisodiosPorSerie(serie);

            System.out.println("Top 5 melhores episódios da série: " + serie.getTitulo());
            top5Episodios.forEach(e ->
                    System.out.println(
                            "Avaliação: " + e.getAvaliacao() +
                                    " - Temporada: " + e.getTemporada()+
                                    " - Episódio: " + e.getNumeroEpisodio()+ " - " + e.getTitulo())
            );
        }
    }

    private void buscarEpisodiosAposUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();

            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = serieRepository.episodiosSeriePorAno(serie, anoLancamento);

            System.out.println("Filtro de episódios de " + serie.getTitulo() + " a partir do ano " + anoLancamento + ":");
            episodiosAno.forEach(System.out::println);
        }
    }

}