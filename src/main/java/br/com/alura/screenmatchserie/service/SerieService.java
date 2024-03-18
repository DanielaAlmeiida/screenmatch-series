package br.com.alura.screenmatchserie.service;

import br.com.alura.screenmatchserie.dto.EpisodioDTO;
import br.com.alura.screenmatchserie.dto.SerieDTO;
import br.com.alura.screenmatchserie.model.Categoria;
import br.com.alura.screenmatchserie.model.Episodio;
import br.com.alura.screenmatchserie.model.Serie;
import br.com.alura.screenmatchserie.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    public List<SerieDTO> converteDadosSerie(List<Serie> series) {
        return series
                .stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
                .collect(Collectors.toList());
    }

    public List<EpisodioDTO> converteDadosEpisodio(List<Episodio> episodios) {
        return episodios
                .stream()
                .map( e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterTodasAsSeries() {
        return converteDadosSerie(serieRepository.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return converteDadosSerie(serieRepository.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDTO> obterLancamentos() {
        return converteDadosSerie(serieRepository.encontrarEpisodiosMaisRecentes());
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie = serieRepository.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Optional<Serie> serie = serieRepository.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return converteDadosEpisodio(s.getEpisodios());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporadasPorNumero(Long id, Long numero) {
        return converteDadosEpisodio(serieRepository.obterEpisodiosPorTemporada(id, numero));
    }

    public List<SerieDTO> obterSeriesPorCategoria(String genero) {
        Categoria categoria = Categoria.fromPortugues(genero);
        return converteDadosSerie(serieRepository.findByGenero(categoria));
    }

    public List<EpisodioDTO> obterTop5EpisodiosPorSerie(Long id) {
        Serie serie = serieRepository.findById(id).get();
        return converteDadosEpisodio(serieRepository.topEpisodiosPorSerie(serie));
    }
}
