package com.showmantra.agent;

import com.showmantra.agent.tools.AgentTools;
import com.showmantra.services.DeterministicBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

@SpringBootTest
public class DebugBookingTest {

    @Autowired
    private AgentTools agentTools;
    
    @Autowired
    private DeterministicBookingService backendLogic;

    @Test
    public void testBooking() {
        try {
            Long movieId = backendLogic.searchMovie("Joker");
            System.out.println("Movie ID: " + movieId);
            
            var show = backendLogic.findBestShow(movieId, LocalDate.now(), "evening");
            System.out.println("Show ID: " + show.showId());
            
            var seats = backendLogic.selectBestSeats(show.showId(), 2);
            System.out.println("Seats: " + seats.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
