package br.com.alura.screenmatchserie.dto;

import java.time.LocalDate;

public record EpisodioDTO(
        Integer temporada,
        String titulo,
        Integer numeroEpisodio
) {
}
