package br.com.alura.screenmatchserie.repository;

import br.com.alura.screenmatchserie.model.Categoria;
import br.com.alura.screenmatchserie.model.Episodio;
import br.com.alura.screenmatchserie.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, Double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    //List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int qntdTemporadas, Double avaliacao);
    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriesPorTemporadaEAValiacao(int totalTemporadas, double avaliacao);

    @Query("""
            SELECT e from Serie s
            JOIN s.episodios e 
            WHERE e.titulo ILIKE %:trechoEpisodio%
            """)
    List<Episodio> episodiosPorTrecho(String trechoEpisodio);


    @Query("""
            SELECT e from Serie s
            Join s.episodios e
            WHERE s = :serie
            ORDER BY e.avaliacao DESC 
            LIMIT 5
            """)
    List<Episodio> topEpisodiosPorSerie(Serie serie);
}