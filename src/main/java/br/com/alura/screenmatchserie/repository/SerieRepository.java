package br.com.alura.screenmatchserie.repository;

import br.com.alura.screenmatchserie.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long> {
}