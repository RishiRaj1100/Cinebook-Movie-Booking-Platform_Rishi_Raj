package com.cinebook.service;

import com.cinebook.domain.entity.Movie;
import com.cinebook.domain.repository.MovieRepository;
import com.cinebook.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Cacheable(value = "movies", key = "#genre + '-' + #language")
    public List<Movie> getMovies(String genre, String language) {
        return movieRepository.findByFilters(genre, language);
    }

    public Movie getMovieById(UUID id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public Movie updateMovie(UUID id, Movie details) {
        Movie movie = getMovieById(id);
        movie.setTitle(details.getTitle());
        movie.setDescription(details.getDescription());
        movie.setDurationMinutes(details.getDurationMinutes());
        movie.setGenre(details.getGenre());
        movie.setLanguage(details.getLanguage());
        movie.setPosterUrl(details.getPosterUrl());
        movie.setBackdropUrl(details.getBackdropUrl());
        movie.setTrailerUrl(details.getTrailerUrl());
        movie.setRating(details.getRating());
        movie.setReleaseDate(details.getReleaseDate());
        movie.setActive(details.isActive());
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(UUID id) {
        Movie movie = getMovieById(id);
        movie.setActive(false);
        movieRepository.save(movie);
    }

    public List<String> getGenres() {
        List<String> rawGenres = movieRepository.findDistinctGenres();
        return rawGenres.stream()
                .flatMap(g -> java.util.Arrays.stream(g.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public List<String> getLanguages() {
        return movieRepository.findDistinctLanguages();
    }
}
