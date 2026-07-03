package com.showmantra.components;

import com.showmantra.entities.*;
import com.showmantra.entities.enums.Role;
import com.showmantra.entities.enums.SeatClass;
import com.showmantra.repositories.*;
import com.showmantra.dtos.ShowRequest;
import com.showmantra.services.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final TheaterRepository theaterRepository;
        private final ScreenRepository screenRepository;
        private final SeatRepository seatRepository;
        private final MovieRepository movieRepository;
        private final ShowService showService;
        private final ShowRepository showRepository;

        @Override
        public void run(String... args) throws Exception {
                if (userRepository.count() > 0) {
                        log.info("Database already seeded. Skipping seeder.");
                        return;
                }

                log.info("Starting Extensive Data Seeder...");
                Random random = new Random();

                // 1. Create a dummy user
                User user = User.builder()
                                .email("testuser@showmantra.com")
                                .passwordHash("hashed_password_123")
                                .phone("1234567890")
                                .role(Role.USER)
                                .build();
                user = userRepository.save(user);
                log.info("Created Dummy User with ID: {}", user.getId());

                // 2. Create Movies (20 Real Movies)
                List<Movie> movies = getMovies();
                List<Movie> savedMovies = movieRepository.saveAll(movies);
                log.info("Created {} Movies.", savedMovies.size());

                // 3. Create Theaters for 5 Cities
                List<Theater> theaters = getTheaters();
                List<Theater> savedTheaters = theaterRepository.saveAll(theaters);
                log.info("Created {} Theaters across 5 Cities.", savedTheaters.size());

                // 4. Create Screens and Seats for each Theater
                List<Screen> allScreens = new ArrayList<>();
                int totalSeatsCreated = 0;

                for (Theater t : savedTheaters) {
                        int numScreens = 2 + random.nextInt(2); // 2 or 3 screens per theater
                        for (int i = 1; i <= numScreens; i++) {
                                Screen screen = Screen.builder()
                                                .name("Audi " + i)
                                                .theater(t)
                                                .totalCapacity(50)
                                                .build();
                                screen = screenRepository.save(screen);
                                allScreens.add(screen);

                                // Create Seats for the Screen
                                List<Seat> seats = new ArrayList<>();
                                char rowChar = 'A';
                                for (int row = 0; row < 5; row++) { // 5 rows
                                        for (int seatNum = 1; seatNum <= 10; seatNum++) { // 10 seats per row
                                                SeatClass seatClass = (row < 2) ? SeatClass.SILVER
                                                                : (row < 4 ? SeatClass.GOLD : SeatClass.PLATINUM);
                                                Seat seat = Seat.builder()
                                                                .screen(screen)
                                                                .rowNumber(String.valueOf((char) (rowChar + row)))
                                                                .seatNumber(seatNum)
                                                                .seatClass(seatClass)
                                                                .build();
                                                seats.add(seat);
                                        }
                                }
                                seatRepository.saveAll(seats);
                                totalSeatsCreated += seats.size();
                        }
                }
                log.info("Created {} Screens and {} Physical Seats in total.", allScreens.size(), totalSeatsCreated);

                // 5. Create Shows and ShowSeats
                log.info("Generating Shows and ShowSeats... This may take a few moments.");
                int showCount = 0;
                int[] showHours = { 10, 14, 18, 21 }; // 10 AM, 2 PM, 6 PM, 9 PM

                for (Screen s : allScreens) {
                        // For each screen, schedule 4 shows per day for the next 5 days
                        for (int day = 0; day < 5; day++) {
                                for (int hour : showHours) {
                                        Movie m = savedMovies.get(random.nextInt(savedMovies.size()));
                                        LocalDateTime startTime = LocalDateTime.now().plusDays(day).withHour(hour)
                                                        .withMinute(0).withSecond(0).withNano(0);
                                        LocalDateTime endTime = startTime.plusMinutes(m.getDurationMinutes());
                                        BigDecimal basePrice = new BigDecimal("150.00")
                                                        .add(new BigDecimal(random.nextInt(5) * 50)); // 150, 200, 250,
                                                                                                      // 300, 350

                                        ShowRequest showRequest = new ShowRequest(
                                                        m.getId(),
                                                        s.getId(),
                                                        startTime,
                                                        endTime,
                                                        basePrice);

                                        try {
                                                showService.createShow(showRequest);
                                                showCount++;
                                        } catch (Exception e) {
                                                log.error("Failed to create show: {}", e.getMessage());
                                        }
                                }
                        }
                }

                log.info("-----------------------------------------------------");
                log.info("Data Seeding Complete!");
                log.info("Successfully generated {} Shows across {} Screens.", showCount, allScreens.size());
                log.info("Use the following for API testing:");
                log.info("User ID: {}", user.getId());
                log.info("-----------------------------------------------------");
        }

        private List<Movie> getMovies() {
                return Arrays.asList(
                                Movie.builder().title("Inception").description(
                                                "A thief who steals corporate secrets through the use of dream-sharing technology.")
                                                .language("English").releaseDate(LocalDate.of(2010, 7, 16))
                                                .durationMinutes(148).genre("Sci-Fi").build(),
                                Movie.builder().title("The Dark Knight").description(
                                                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham.")
                                                .language("English").releaseDate(LocalDate.of(2008, 7, 18))
                                                .durationMinutes(152).genre("Action").build(),
                                Movie.builder().title("Interstellar").description(
                                                "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.")
                                                .language("English").releaseDate(LocalDate.of(2014, 11, 7))
                                                .durationMinutes(169).genre("Sci-Fi").build(),
                                Movie.builder().title("Avatar").description(
                                                "A paraplegic Marine dispatched to the moon Pandora on a unique mission becomes torn between following his orders and protecting the world he feels is his home.")
                                                .language("English").releaseDate(LocalDate.of(2009, 12, 18))
                                                .durationMinutes(162).genre("Sci-Fi").build(),
                                Movie.builder().title("Titanic").description(
                                                "A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the luxurious, ill-fated R.M.S. Titanic.")
                                                .language("English").releaseDate(LocalDate.of(1997, 12, 19))
                                                .durationMinutes(194).genre("Romance").build(),
                                Movie.builder().title("The Matrix").description(
                                                "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.")
                                                .language("English").releaseDate(LocalDate.of(1999, 3, 31))
                                                .durationMinutes(136).genre("Sci-Fi").build(),
                                Movie.builder().title("Avengers: Endgame").description(
                                                "After the devastating events of Infinity War, the Avengers assemble once more in order to reverse Thanos' actions.")
                                                .language("English").releaseDate(LocalDate.of(2019, 4, 26))
                                                .durationMinutes(181).genre("Action").build(),
                                Movie.builder().title("Spider-Man: No Way Home").description(
                                                "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help.")
                                                .language("English").releaseDate(LocalDate.of(2021, 12, 17))
                                                .durationMinutes(148).genre("Action").build(),
                                Movie.builder().title("Joker").description(
                                                "In Gotham City, mentally troubled comedian Arthur Fleck is disregarded and mistreated by society.")
                                                .language("English").releaseDate(LocalDate.of(2019, 10, 4))
                                                .durationMinutes(122).genre("Drama").build(),
                                Movie.builder().title("The Godfather").description(
                                                "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.")
                                                .language("English").releaseDate(LocalDate.of(1972, 3, 24))
                                                .durationMinutes(175).genre("Crime").build(),
                                Movie.builder().title("Pulp Fiction").description(
                                                "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.")
                                                .language("English").releaseDate(LocalDate.of(1994, 10, 14))
                                                .durationMinutes(154).genre("Crime").build(),
                                Movie.builder().title("Forrest Gump").description(
                                                "The presidencies of Kennedy and Johnson, the events of Vietnam, Watergate and other historical events unfold through the perspective of an Alabama man.")
                                                .language("English").releaseDate(LocalDate.of(1994, 7, 6))
                                                .durationMinutes(142).genre("Drama").build(),
                                Movie.builder().title("Fight Club").description(
                                                "An insomniac office worker and a devil-may-care soapmaker form an underground fight club.")
                                                .language("English").releaseDate(LocalDate.of(1999, 10, 15))
                                                .durationMinutes(139).genre("Drama").build(),
                                Movie.builder().title("The Lord of the Rings").description(
                                                "A meek Hobbit from the Shire and eight companions set out on a journey to destroy the powerful One Ring.")
                                                .language("English").releaseDate(LocalDate.of(2001, 12, 19))
                                                .durationMinutes(178).genre("Fantasy").build(),
                                Movie.builder().title("Star Wars: A New Hope").description(
                                                "Luke Skywalker joins forces with a Jedi Knight, a cocky pilot, a Wookiee and two droids to save the galaxy.")
                                                .language("English").releaseDate(LocalDate.of(1977, 5, 25))
                                                .durationMinutes(121).genre("Sci-Fi").build(),
                                Movie.builder().title("Jurassic Park").description(
                                                "A pragmatic paleontologist visiting an almost complete theme park is tasked with protecting a couple of kids after a power failure causes the park's cloned dinosaurs to run loose.")
                                                .language("English").releaseDate(LocalDate.of(1993, 6, 11))
                                                .durationMinutes(127).genre("Adventure").build(),
                                Movie.builder().title("Harry Potter").description(
                                                "An orphaned boy enrolls in a school of wizardry, where he learns the truth about himself, his family and the terrible evil that haunts the magical world.")
                                                .language("English").releaseDate(LocalDate.of(2001, 11, 16))
                                                .durationMinutes(152).genre("Fantasy").build(),
                                Movie.builder().title("The Lion King").description(
                                                "Lion prince Simba and his father are targeted by his bitter uncle, who wants to ascend the throne himself.")
                                                .language("English").releaseDate(LocalDate.of(1994, 6, 24))
                                                .durationMinutes(89).genre("Animation").build(),
                                Movie.builder().title("Gladiator").description(
                                                "A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery.")
                                                .language("English").releaseDate(LocalDate.of(2000, 5, 5))
                                                .durationMinutes(155).genre("Action").build(),
                                Movie.builder().title("The Terminator").description(
                                                "A human soldier is sent from 2029 to 1984 to stop an almost indestructible cyborg killing machine, sent from the same year, which has been programmed to execute a young woman whose unborn son is the key to humanity's future salvation.")
                                                .language("English").releaseDate(LocalDate.of(1984, 10, 26))
                                                .durationMinutes(107).genre("Action").build());
        }

        private List<Theater> getTheaters() {
                List<Theater> theaters = new ArrayList<>();
                // Mumbai (City 1) - Existing 3, adding 7 more
                theaters.add(Theater.builder().name("PVR Cinemas - Juhu").cityId(1L).address("Juhu Tara Rd, Mumbai")
                                .build());
                theaters.add(Theater.builder().name("INOX - Nariman Point").cityId(1L)
                                .address("CR2 Mall, Nariman Point, Mumbai").build());
                theaters.add(Theater.builder().name("Cinepolis - Andheri").cityId(1L).address("Andheri West, Mumbai")
                                .build());
                theaters.add(Theater.builder().name("Manya Cineplex").cityId(1L).address("Bandra West, Mumbai")
                                .build());
                theaters.add(Theater.builder().name("Mannu IMAX").cityId(1L).address("Lower Parel, Mumbai").build());
                theaters.add(Theater.builder().name("Mukta A2 Cinemas").cityId(1L).address("Goregaon, Mumbai").build());
                theaters.add(Theater.builder().name("Carnival Cinemas").cityId(1L).address("Borivali, Mumbai").build());
                theaters.add(Theater.builder().name("Sterling Cineplex").cityId(1L).address("Fort, Mumbai").build());
                theaters.add(Theater.builder().name("Regal Cinema").cityId(1L).address("Colaba, Mumbai").build());
                theaters.add(Theater.builder().name("Metro INOX").cityId(1L).address("Marine Lines, Mumbai").build());

                // Delhi (City 2) - Existing 2, adding 8 more
                theaters.add(Theater.builder().name("PVR Select CityWalk").cityId(2L).address("Saket, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("INOX - Nehru Place").cityId(2L)
                                .address("Epicuria, Nehru Place, New Delhi").build());
                theaters.add(Theater.builder().name("Manya Talkies").cityId(2L).address("Connaught Place, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("Mannu Multiplex").cityId(2L).address("Vasant Kunj, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("Cinepolis - DLF").cityId(2L).address("DLF Place, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("Delite Cinema").cityId(2L).address("Daryaganj, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("Satyam Cineplexes").cityId(2L).address("Janakpuri, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("Wave Cinemas").cityId(2L).address("Rajouri Garden, New Delhi")
                                .build());
                theaters.add(Theater.builder().name("M2K Cinemas").cityId(2L).address("Rohini, New Delhi").build());
                theaters.add(Theater.builder().name("Liberty Cinema").cityId(2L).address("Karol Bagh, New Delhi")
                                .build());

                // Bengaluru (City 3) - Existing 3, adding 7 more
                theaters.add(Theater.builder().name("PVR Forum Mall").cityId(3L).address("Koramangala, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Cinepolis - Orion Mall").cityId(3L)
                                .address("Malleshwaram, Bengaluru").build());
                theaters.add(Theater.builder().name("INOX - Lido").cityId(3L).address("Ulsoor, Bengaluru").build());
                theaters.add(Theater.builder().name("Manya Digital 4K").cityId(3L).address("Indiranagar, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Mannu Cinemas").cityId(3L).address("Whitefield, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Urvashi Digital 4K").cityId(3L).address("Lalbagh Road, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Gopalan Cinemas").cityId(3L).address("Old Madras Road, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Vision Cinemas").cityId(3L).address("MG Road, Bengaluru").build());
                theaters.add(Theater.builder().name("Rex Theatre").cityId(3L).address("Brigade Road, Bengaluru")
                                .build());
                theaters.add(Theater.builder().name("Cauvery Theatre").cityId(3L).address("Sadashivanagar, Bengaluru")
                                .build());

                // Hyderabad (City 4) - Existing 2, adding 8 more
                theaters.add(Theater.builder().name("Prasads IMAX").cityId(4L).address("NTR Marg, Hyderabad").build());
                theaters.add(Theater.builder().name("PVR Inorbit").cityId(4L).address("Madhapur, Hyderabad").build());
                theaters.add(Theater.builder().name("Manya Screenz").cityId(4L).address("Banjara Hills, Hyderabad")
                                .build());
                theaters.add(Theater.builder().name("Mannu 70MM").cityId(4L).address("Jubilee Hills, Hyderabad")
                                .build());
                theaters.add(Theater.builder().name("INOX GVK One").cityId(4L).address("Punjagutta, Hyderabad")
                                .build());
                theaters.add(Theater.builder().name("AMB Cinemas").cityId(4L).address("Gachibowli, Hyderabad").build());
                theaters.add(Theater.builder().name("Asian Cinemas").cityId(4L).address("Kukatpally, Hyderabad")
                                .build());
                theaters.add(Theater.builder().name("Cineplanet").cityId(4L).address("Kompally, Hyderabad").build());
                theaters.add(Theater.builder().name("Gokul Theatre").cityId(4L).address("Erragadda, Hyderabad")
                                .build());
                theaters.add(Theater.builder().name("Sudarshan 35MM").cityId(4L).address("RTC X Roads, Hyderabad")
                                .build());

                // Chennai (City 5) - Existing 2, adding 8 more
                theaters.add(Theater.builder().name("SPI Cinemas").cityId(5L).address("Royapettah, Chennai").build());
                theaters.add(Theater.builder().name("PVR VR Mall").cityId(5L).address("Anna Nagar, Chennai").build());
                theaters.add(Theater.builder().name("Manya Talkies").cityId(5L).address("T Nagar, Chennai").build());
                theaters.add(Theater.builder().name("Mannu Multiplex").cityId(5L).address("Adyar, Chennai").build());
                theaters.add(Theater.builder().name("INOX Citi Centre").cityId(5L).address("Mylapore, Chennai")
                                .build());
                theaters.add(Theater.builder().name("AGS Cinemas").cityId(5L).address("Villivakkam, Chennai").build());
                theaters.add(Theater.builder().name("Mayajaal Multiplex").cityId(5L).address("ECR, Chennai").build());
                theaters.add(Theater.builder().name("Palazzo Cinemas").cityId(5L).address("Vadapalani, Chennai")
                                .build());
                theaters.add(Theater.builder().name("Udhayam Theatres").cityId(5L).address("Ashok Nagar, Chennai")
                                .build());
                theaters.add(Theater.builder().name("Kamala Cinemas").cityId(5L).address("Vadapalani, Chennai")
                                .build());

                return theaters;
        }
}
