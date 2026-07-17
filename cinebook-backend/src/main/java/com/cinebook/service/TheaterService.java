package com.cinebook.service;

import com.cinebook.domain.entity.Screen;
import com.cinebook.domain.entity.Seat;
import com.cinebook.domain.entity.Theater;
import com.cinebook.domain.enums.SeatType;
import com.cinebook.domain.repository.ScreenRepository;
import com.cinebook.domain.repository.SeatRepository;
import com.cinebook.domain.repository.TheaterRepository;
import com.cinebook.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAllByOrderByNameAsc();
    }

    public List<Theater> getTheatersByCity(String city) {
        return theaterRepository.findByCityIgnoreCaseOrderByName(city);
    }

    public List<String> getCities() {
        return theaterRepository.findDistinctCities();
    }

    public Theater getTheaterById(UUID id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));
    }

    @Transactional
    public Theater createTheater(Theater theater) {
        return theaterRepository.save(theater);
    }

    @Transactional
    public Screen createScreen(UUID theaterId, Screen screen) {
        Theater theater = getTheaterById(theaterId);
        screen.setTheater(theater);
        Screen savedScreen = screenRepository.save(screen);

        // Auto-generate physical seats grid
        generateSeatsForScreen(savedScreen);
        return savedScreen;
    }

    private void generateSeatsForScreen(Screen screen) {
        List<Seat> seats = new ArrayList<>();
        int rows = screen.getTotalRows();
        int cols = screen.getTotalColumns();

        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            SeatType seatType = SeatType.REGULAR;
            if (r >= rows - 2) seatType = SeatType.RECLINER;
            else if (r >= rows - 4) seatType = SeatType.PREMIUM;

            for (int c = 1; c <= cols; c++) {
                seats.add(Seat.builder()
                        .screen(screen)
                        .rowLabel(rowLabel)
                        .seatNumber(c)
                        .seatType(seatType)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }
}
