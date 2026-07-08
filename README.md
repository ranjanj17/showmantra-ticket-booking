# 🎬 ShowMantra - Intelligent Full-Stack Movie Ticket Booking Platform

> A production-grade, end-to-end movie ticket booking platform featuring a conversational AI Agent, deterministic seat locking, and a seamless React frontend.

---

## 1. Project Overview

### What Problem This Solves
Traditional ticketing systems rely heavily on manual navigation, multi-step forms, and static interfaces. **ShowMantra** revolutionizes the experience by providing two parallel booking paths:
1. **Traditional GUI:** A snappy, modern React interface.
2. **Conversational AI Agent:** A smart LLM-powered assistant that guides users from discovery to payment via natural language.

### Key Features
- **AI-Powered Booking:** Book tickets purely through chat using function-calling capabilities.
- **Deterministic Seat Locking:** Pessimistic database locking to absolutely prevent double-booking.
- **Real-time Seat Matrix:** Dynamic grid representing available, held, and booked seats.
- **Secure Authentication:** Stateless JWT token-based authentication.
- **Role-Based Access:** Segregation between standard users and system admins.

### High-Level Architecture Summary
The system is built on a robust 3-tier architecture:
- **Frontend:** React SPA built with Vite, TailwindCSS, and Zustand.
- **Backend:** Java 17 + Spring Boot 3.4.3 serving REST APIs and orchestrating the AI Agent.
- **Database Layer:** MySQL for transactional integrity, Redis for session and cache state.
- **AI Layer:** Spring AI integrating with LLM providers (e.g., Google Gemini/OpenAI) using memory and RAG capabilities.

---

## 2. System Architecture

The interaction model ensures that the AI Agent can act identically to a human using the frontend, executing the same underlying deterministic backend logic.

### Overall Architecture Diagram
```text
┌──────────────┐         HTTP / REST          ┌───────────────────────────────────┐
│              │ ───────────────────────────▶ │  API Gateway / Controllers        │
│  React SPA   │                              └────────────────┬──────────────────┘
│ (Frontend)   │ ◀───────────────────────────                  │
└──────┬───────┘         JSON Responses       ┌────────────────▼──────────────────┐
       │                                      │  Services (Business Logic)        │
       │                                      │  - DeterministicBookingService    │
       │ (Chat)                               └────────┬─────────────────┬────────┘
       ▼                                               │                 │
┌──────────────┐         Function Calls       ┌────────▼─────────┐ ┌─────▼────────┐
│              │ ───────────────────────────▶ │                  │ │              │
│  AI Agent    │                              │      MySQL       │ │    Redis     │
│ (LlmClient)  │ ◀─────────────────────────── │ (Source of Truth)│ │ (Caching &   │
└──────┬───────┘         Tool Responses       │                  │ │ Agent State) │
       │                                      └──────────────────┘ └──────────────┘
       ▼
┌──────────────┐
│ LLM Provider │
│ (Gemini etc.)│
└──────────────┘
```

**Interaction Flow:** `User → Frontend → Backend Controller → Agent / Services → DB → Response → Frontend`

---

## 3. Backend Architecture

### 3.1 Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.4.3 (Web, Data JPA, Security)
- **AI Integration:** Spring AI 1.0.0-M6
- **Database:** MySQL, H2 (Testing)
- **Caching/State:** Redis
- **Security:** Spring Security, JWT (jjwt)
- **Mapping & Boilerplate:** MapStruct, Lombok
- **Migrations:** Flyway

### 3.2 Folder Structure

```text
backend/src/main/java/com/showmantra/
├── agent/            # AI Agent orchestration, State management, LLM Client
│   └── tools/        # Functions callable by the LLM
├── config/           # Application config (Security, CORS, AI beans)
├── controllers/      # REST API endpoints
├── dtos/             # Data Transfer Objects for Request/Response shapes
├── entities/         # JPA Domain Models mapped to DB tables
├── exceptions/       # Global exception handlers (@ControllerAdvice)
├── mappers/          # MapStruct interfaces converting Entities ↔ DTOs
├── repositories/     # Spring Data JPA interfaces for DB access
└── services/         # Core business logic
```

### 3.3 Core Components

#### Controllers
- **AgentInteractionController:** Handles `/api/agent/chat` routing user messages to the `LlmClient`.
- **BookingController:** Manages locking seats, confirming, and cancelling bookings.
- **MovieController:** APIs to search and list movies.
- **PaymentController:** Dummy payment gateway processing.
- **ShowController:** Fetching showtimes and real-time seat matrices.
- **TheaterController:** Managing theaters.
- **UserController:** Registration and JWT-based Login.

#### Services
- **DeterministicBookingService:** Core transaction logic strictly handling DB locks (`PESSIMISTIC_WRITE`) and price calculations.
- **UserService, MovieService, etc.:** Standard CRUD and business logic encapsulations.

#### Repositories
- standard Spring Data interfaces (e.g., `BookingRepository`, `ShowSeatRepository`). Extends `JpaRepository`.

#### Entities
- **BaseEntity:** Abstract class with `createdAt` and `updatedAt`.
- **User:** `id (UUID)`, `email`, `passwordHash`, `role`.
- **Movie:** `id`, `title`, `description`, `duration`.
- **Theater:** `id`, `name`, `city`.
- **Screen:** `id`, `theater`, `name`.
- **Show:** `id`, `movie`, `screen`, `startTime`, `basePrice`.
- **Seat:** `id`, `screen`, `rowLabel`, `seatNumber`.
- **ShowSeat:** `id`, `show`, `seat`, `price`, `status (AVAILABLE/HELD/BOOKED)`. *(Where DB locks occur)*
- **Booking:** `id (UUID)`, `user`, `show`, `totalAmount`, `status`.
- **BookingItem:** Links `Booking` to specific `ShowSeat`s.
- **Payment:** `id`, `booking`, `amount`, `status`.

### 3.4 API Documentation

| Endpoint | Method | Request Body / Params | Response | Purpose |
|----------|--------|-----------------------|----------|---------|
| `/api/users/register` | POST | `UserCreateRequest` (email, pass, etc) | `UserResponse` | Creates a new user in DB. |
| `/api/users/login` | POST | `LoginRequest` (email, pass) | `UserResponse` (with JWT) | Authenticates and returns token. |
| `/api/agent/chat` | POST | `{ sessionId, message }` | `{ sessionId, reply, status }` | Sends message to AI Agent. |
| `/api/bookings/lock` | POST | `{ showId, seatIds }` | `BookingResponse` | Locks seats and generates a pending booking. |
| `/api/bookings/{id}/confirm` | POST | None | `String` (Success msg) | Confirms payment and issues tickets. |
| `/api/bookings/history` | GET | Auth Header | `List<BookingHistoryResponse>` | Retrieves user's past bookings. |
| `/api/movies/search` | GET | `?q=query` | `List<Movie>` | Search movies by title. |
| `/api/shows/movie/{id}` | GET | `?date=YYYY-MM-DD` | `List<ShowtimeResponse>` | Get available shows for a movie. |
| `/api/shows/{id}/seats` | GET | None | `SeatMatrixResponse` | Returns visual grid of seat statuses. |

**Flow inside Backend:**
`Request → Filter (JWT Auth) → Controller → Map Request to DTO → Service (Business Logic + @Transactional) → Repository (DB queries) → Map to DTO → ResponseEntity`

### 3.5 Backend Flow (Seat Booking)
```text
User Request → [BookingController]
                    │
                    ▼
            [BookingService]
                    │
                    ├─ Verify User exists
                    │
                    ▼
      [ShowSeatRepository] @Lock(PESSIMISTIC_WRITE)
      SELECT seats WHERE status = 'AVAILABLE' FOR UPDATE
                    │
                    ├─ If already booked → Throw Exception
                    │
                    ▼
             Update seats to 'HELD'
             Create Booking record (Status: PENDING)
                    │
                    ▼
              Return Booking ID
```

### 3.6 State Management (Backend)
The conversational flow is stateful.
- **Class:** `AgentState.java`
- **Fields:** `sessionId`, `status` (INIT → MOVIE_SELECTED → SHOW_SELECTED → SEATS_LOCKED → CONFIRMED), `movieId`, `showId`, `selectedSeats`, `totalPrice`.
- **Storage:** Persisted via `StateRepository` tied to the `sessionId`.

---

## 4. AI Agent Architecture

### 4.1 Agent Design
- **Core Library:** Spring AI using the `ChatClient` fluent API.
- **Memory:** `InMemoryChatMemory` advised via `MessageChatMemoryAdvisor` to retain 100 messages of conversation context per session.
- **RAG:** `QuestionAnswerAdvisor` backed by a `VectorStore` (documents ingested via Spring AI vector primitives) to answer FAQ / policy questions.
- **System Prompt:** Dynamically injected with current date and the user's `AgentState` before every LLM call.

### 4.2 Tools (Functions)
Defined in `AgentTools.java` as Spring `@Bean` Functions annotated with `@Description`.
1. **`listMovies`**: Returns all playing movies.
2. **`searchShows`**: Input `(movie, city, date)`. Updates State (`MOVIE_SELECTED`). Returns show IDs and times.
3. **`getSeatLayout`**: Input `(showId)`. Updates State (`SHOW_SELECTED`). Returns seat grid.
4. **`lockSeats`**: Input `(showId, seatLabels)`. Triggers backend transactional lock. Updates State (`SEATS_LOCKED`). Returns total price and booking ID.
5. **`processPayment`**: Input `(bookingId)`. Updates State (`CONFIRMED`).
6. **`cancelBooking`**: Input `(bookingId)`. Cancels an active booking.

### 4.3 Agent Flow
```text
         ┌───────────────┐
         │ User Message  │
         └───────┬───────┘
                 │
                 ▼
        [ System Prompt +  ]
        [ Agent State      ] ───▶ LLM Provider
        [ Chat Memory      ]
                 ▲      │
                 │      │ (Decides to call tool)
                 │      ▼
         ┌───────────────┐
         │  AgentTools   │ (e.g. searchShows)
         └───────────────┘
                 │
                 ▼
        [ Update State Repo ]
                 │
                 ▼
         Return Tool Result
                 │
                 ▼
            LLM Provider ───▶ Formulates human-readable reply
                 │
                 ▼
           [ User Reply ]
```

### 4.4 Conversation Handling
Context retention is handled automatically by Spring AI's Advisors. If an error occurs (e.g., Rate Limiting `429`), the `AgentInteractionController` intercepts it and gracefully falls back with a structured error message to the frontend without crashing the session.

### 4.5 Limitations & Improvements
- **Limitation:** Redis state is currently local memory or basic persistence.
- **Improvement:** Migrate `AgentState` to a highly available Redis cluster with TTL. Implement token limits on Chat Memory to prevent context window overflow.

---

## 5. Frontend Architecture

### 5.1 Tech Stack
- **Framework:** React 18
- **Build Tool:** Vite
- **Styling:** Tailwind CSS + Lucide React (Icons)
- **State Management:** Zustand
- **Routing:** React Router v7
- **Network:** Axios
- **Notifications:** React Hot Toast

### 5.2 Folder Structure
```text
frontend/src/
├── assets/       # Static files, images
├── components/   # Reusable UI (Navbar, AgentChatBox, SeatGrid, Modals)
├── data/         # Dummy/mock data for pure UI testing
├── pages/        # Route components (Home, MovieDetails, BookingPage)
├── services/     # API encapsulations (api.ts, authService.ts)
├── store/        # Zustand state slices (useAppStore, useAuthStore, useBookingStore)
└── utils/        # Helper functions (formatting, validation)
```

### 5.3 UI Components
- **AgentChatBox:** Floating chat widget that interfaces with `/api/agent/chat`.
- **SeatGrid:** Renders the dynamic `SeatMatrixResponse` allowing users to click and select seats.
- **Pages:** `Home` (movie listings), `MovieDetails` (shows and dates), `BookingPage` (seat selection), `PaymentPage` (checkout).

### 5.4 State Management
Zustand is used for lightweight, boilerplate-free state.
- `useAuthStore`: Holds JWT token, user info, and login modal visibility.
- `useBookingStore`: Tracks `selectedSeatIds`, `isLocking`, and `currentBookingId`.
- `useAppStore`: Global UI state (e.g., selected city).

### 5.5 API Integration
`services/api.ts` defines a global Axios instance.
- **Interceptors:** Automatically injects `Authorization: Bearer <token>` on requests.
- **Error Handling:** Globally intercepts `401/403` to clear the token and trigger the Auth Modal.

### 5.6 Frontend Flow
```text
User Action (Click Seat) 
      │
      ▼
useBookingStore (Update UI instantly)
      │
      ▼
User Clicks 'Book' 
      │
      ▼
api.post('/api/bookings/lock', { seatIds })
      │
      ▼
(Backend processing...)
      │
      ▼
Success Response → update store with `bookingId`
      │
      ▼
React Router navigate('/payment')
```

---

## 6. End-to-End Flow

### The Complete Journey (Manual GUI Path)
1. **Discovery:** User lands on the `Home` page, views available movies, and selects a movie.
2. **Show Selection:** User is routed to the `MovieDetails` page, where they select a specific date, theater, and showtime.
3. **Seat Layout:** User is routed to the `BookingPage`. The frontend fetches the real-time seat matrix for the selected show.
4. **Seat Selection:** User clicks on available seats. Zustand (`useBookingStore`) updates the UI state instantly.
5. **Locking:** User clicks "Book Tickets". The frontend calls the `/api/bookings/lock` endpoint with the selected seat IDs. The backend executes a Pessimistic Lock, creates a PENDING Booking, and returns the booking ID.
6. **Payment:** User is redirected to the `PaymentPage`, where they review the total amount and proceed to pay.
7. **Confirmation:** The frontend calls the `/api/bookings/{id}/confirm` endpoint upon successful payment. The backend updates the Booking to CONFIRMED.
8. **Finalization:** User is shown a success message and can view their tickets in the `MyBookings` dashboard.

### The Complete Journey (Agent Path)
1. **Initiation:** User opens the Agent Chat and types "I want to watch Inception in Mumbai".
2. **Discovery:** LLM hits `searchShows` tool. Backend queries DB. Returns shows.
3. **Selection:** Agent replies: "I found a show at 8 PM. Shall I show you the seats?" User agrees.
4. **Layout:** LLM hits `getSeatLayout`. Returns matrix. Agent describes available seats.
5. **Locking:** User says "Book A1 and A2". LLM hits `lockSeats`. Backend executes Pessimistic Lock, creates PENDING Booking.
6. **Payment:** Agent says "Seats locked. Total is $20. Confirm payment?" User says "Yes".
7. **Confirmation:** LLM hits `processPayment`. Backend updates Booking to CONFIRMED.
8. **Finalization:** Agent congratulates the user and ends flow.

---

## 7. Database Design

### Relationships Overview
- **User (1) → (N) Booking:** A user has many bookings.
- **Movie (1) → (N) Show:** A movie has many screenings.
- **Theater (1) → (N) Screen:** A theater has multiple physical screens.
- **Screen (1) → (N) Seat:** A screen has a static layout of physical seats.
- **Show (1) → (N) ShowSeat:** *Crucial table*. Maps physical seats to a specific show, holding the dynamic `status` (AVAILABLE/BOOKED).
- **Booking (1) → (N) BookingItem:** A booking contains multiple specific ShowSeats.

### Schema Explanation
We avoid the "double booking problem" by separating the static `Seat` from the temporal `ShowSeat`. All transactional locks happen on the `ShowSeat` row, guaranteeing absolute ACID compliance during high-concurrency ticket drops.

---

## 8. Setup & Installation

### Prerequisites
- JDK 17+
- Node.js 18+
- MySQL Server 8+
- Optional: Redis Server (if configured for caching)

### Backend Setup
1. Create a MySQL database: `CREATE DATABASE showmantra;`
2. Update `application.properties` with your DB credentials and LLM API Key (e.g. OpenAI/Gemini).
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/showmantra
spring.datasource.username=root
spring.datasource.password=root
spring.ai.openai.api-key=YOUR_API_KEY
```
3. Run the application:
```bash
cd backend
./mvnw spring-boot:run
```
*(Flyway will automatically run migrations and seed data on startup).*

### Frontend Setup
1. Open a new terminal.
2. Install dependencies and start Vite dev server:
```bash
cd frontend
npm install
npm run dev
```
3. Visit `http://localhost:5173`.

---

## 9. Key Design Decisions

1. **Pessimistic Locking vs Optimistic Locking:** We chose Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) because ticket booking is high-contention. Optimistic locking would result in massive `ObjectOptimisticLockingFailureException` rollbacks during a blockbuster release, ruining UX.
2. **UUIDs for Primary Keys:** `Booking` and `User` use UUIDs instead of sequential IDs. This prevents competitors from guessing ticket sales volumes (ID Enumeration).
3. **Stateless JWT:** To allow horizontal scaling of backend nodes without needing sticky sessions.
4. **Agent State in DB:** Instead of relying entirely on LLM conversation history, we maintain an explicit State Machine (`AgentState`) in the backend to ensure data integrity across the transaction flow.

---

## 10. Edge Cases & Error Handling

- **Race Conditions:** Solved via DB-level locks. If User A and B click the same seat at the exact millisecond, one query waits. The second query will see `status='HELD'` and fail gracefully.
- **Failed / Abandoned Payments:** Bookings left in `PENDING` state will be swept by a scheduled cron job (or TTL expiration) and associated `ShowSeat` statuses are reverted to `AVAILABLE`.
- **LLM Hallucinations:** The Agent cannot bypass backend logic. If it hallucinates a seat that doesn't exist, the `DeterministicBookingService` will throw an error, which the tool returns to the LLM, prompting it to apologize and correct itself.

---

## 11. Future Improvements

1. **WebSockets:** Push real-time seat availability updates to all connected clients viewing a specific `SeatGrid`.
2. **Queueing System:** Implement a waiting room (Virtual Queue) using RabbitMQ for extremely high-demand shows (e.g., Avengers release).
3. **Enhanced RAG:** Load dynamic theater policies (refunds, food) into the Vector Store.
4. **Microservices:** Extract the AI Agent into an isolated Python/FastAPI service communicating via gRPC.

---
<p align="center">Made with ❤️ using Spring Boot, React & Spring AI</p>
