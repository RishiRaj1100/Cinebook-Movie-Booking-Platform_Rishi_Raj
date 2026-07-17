package com.cinebook.service;

import com.cinebook.domain.entity.*;
import com.cinebook.domain.enums.SeatStatus;
import com.cinebook.domain.repository.*;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.util.PricingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PricingUtil pricingUtil;

    public List<Show> getShowsForMovieAndDate(UUID movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return showRepository.findShowsForMovie(movieId, startOfDay, endOfDay);
    }

    public Show getShowById(UUID id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
    }

    public List<ShowSeat> getSeatMapForShow(UUID showId) {
        return showSeatRepository.findByShowIdWithSeat(showId);
    }

    @Transactional
    public Show createShow(UUID movieId, UUID screenId, LocalDateTime startTime, Integer basePricePaise) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes() + 30); // 30 mins inter-show gap

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(basePricePaise)
                .isActive(true)
                .build();

        Show savedShow = showRepository.save(show);

        // Instantiate ShowSeats for every physical seat in the screen
        List<Seat> physicalSeats = seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId);
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : physicalSeats) {
            int seatPrice = pricingUtil.calculateSeatPrice(basePricePaise, seat.getSeatType());
            showSeats.add(ShowSeat.builder()
                    .show(savedShow)
                    .seat(seat)
                    .price(seatPrice)
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }

        showSeatRepository.saveAll(showSeats);
        return savedShow;
    }
}
