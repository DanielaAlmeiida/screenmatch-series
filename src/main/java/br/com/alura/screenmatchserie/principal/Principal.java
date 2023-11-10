package br.com.alura.screenmatchserie.principal;

import br.com.alura.screenmatchserie.model.DadosSerie;
import br.com.alura.screenmatchserie.model.DadosTemporada;
import br.com.alura.screenmatchserie.model.Episodio;
import br.com.alura.screenmatchserie.model.Serie;
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
                var jsonTemporada = consumoApi.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
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

        Optional<Serie> serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
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


}
