package com.cinebook.service;

import com.cinebook.domain.entity.Movie;
import com.cinebook.domain.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring AI Recommendation Service — AI movie recommendations based on user prompts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ChatClient.Builder chatClientBuilder;
    private final MovieRepository   movieRepository;

    public String recommendMovies(String userPreference) {
        List<Movie> activeMovies = movieRepository.findByIsActiveTrueOrderByReleaseDateDesc();

        String catalogStr = activeMovies.stream()
                .map(m -> String.format("ID: %s, Title: %s, Genre: %s, Rating: %s, Language: %s",
                        m.getId(), m.getTitle(), m.getGenre(), m.getRating(), m.getLanguage()))
                .collect(Collectors.joining("\n"));

        String systemMessage = """
            You are CineBook AI — a personalized movie recommendation assistant.
            Recommend up to 3 movies from the provided catalog based on the user's request.
            For each recommendation, give the Movie Title and a brief explanation why it matches.
            """;

        String userPrompt = "Movie Catalog:\n" + catalogStr + "\n\nUser Preference: " + userPreference;

        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system(systemMessage)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Spring AI call failed (falling back to simple recommendation): {}", e.getMessage());
            return "Based on your preference for '" + userPreference + "', we recommend checking out top rated movies like " +
                    activeMovies.stream().limit(3).map(Movie::getTitle).collect(Collectors.joining(", "));
        }
    }
}
