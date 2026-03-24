# 💡 LightOut Solver

A Spring Boot REST API that automatically **creates**, **fetches**, and **solves** [Lights Out](https://en.wikipedia.org/wiki/Lights_Out_(game)) puzzles by communicating with an external game API at `planetrandall.com`.

---

## 📖 What is Lights Out?

Lights Out is a classic puzzle game played on an **n×n grid** where each cell is either **on (1)** or **off (0)**. Clicking a cell toggles it **and all its orthogonal neighbours**. The goal is to turn **all lights off**.

This solver computes the exact set of moves needed to solve any given board using **Gaussian Elimination over GF(2)** (binary field arithmetic).

---

## 🗂️ Project Structure

```
src/main/java/com/lightoutsolver/
├── LightOutSolverApplication.java      # Spring Boot entry point + WebClient bean
├── gaussian/
│   └── LightsOutGaussian.java          # Core solver — Gaussian Elimination mod 2
└── solution/
    ├── LightsOutControllerNew.java     # Active REST controller (POST /create, POST /solve)
    ├── LightsOutController.java        # Legacy controller (disabled, @RestController removed)
    ├── LightOut.java                   # Orchestrates solving: parses grid → calls solver → builds response
    ├── CreateGameRequest.java          # Request body for creating a new game
    ├── SolveGameRequest.java           # Request body for fetching + solving an existing game
    ├── GameResponse.java               # API response wrapper for a game
    ├── GameData.java                   # Game details: id, teamId, grid, size, etc.
    ├── SolveResponse.java              # Payload sent to the solution endpoint
    ├── Move.java                       # Represents a single move: { row, col }
    ├── SubmitSolutionResponse.java     # Response from submitting the solution
    └── SubmitSolutionData.java         # Solution result data: message, moveCount, solvedAt
```

---

## 🔧 Tech Stack

| Technology | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.4 |
| Spring WebFlux (WebClient) | via `spring-boot-starter-webflux` |
| Lombok | latest (via annotation processor) |
| Gradle | 8.x (wrapper included) |

---

## 🚀 Running the Application

### Prerequisites
- **Java 25** installed
- No database or additional services required

### Build & Run

```bash
# Clone the repo
git clone https://github.com/santoshk23/LightOutSolver.git
cd LightOutSolver

# Run with Gradle wrapper
./gradlew bootRun
```

The server starts on **`http://localhost:8080`** by default.

---

## 📡 API Endpoints

Base path: `/lightsout`  
External game API: `https://planetrandall.com/lightsout/api`

---

### `POST /lightsout/create`

Creates a **new game** on the external API, solves it, and submits the solution — all in one call.

**Request Body:**
```json
{
  "teamId": "your-team-id",
  "gameType": "standard"
}
```

**Flow:**
1. `POST` to external API → creates a new game and returns the board
2. Solves the board using Gaussian Elimination
3. `POST` solution back to external API

**Response:** `SubmitSolutionResponse`
```json
{
  "success": true,
  "message": "...",
  "data": {
    "valid": true,
    "message": "Puzzle solved!",
    "moveCount": 7,
    "gameId": "abc-123",
    "teamId": "your-team-id",
    "solvedAt": "2026-03-25T10:00:00Z"
  }
}
```

---

### `POST /lightsout/solve`

Fetches an **existing game** by ID, solves it, and submits the solution.

**Request Body:**
```json
{
  "gameId": "abc-123",
  "teamId": "your-team-id"
}
```

**Flow:**
1. `GET` from external API → fetches game board by `gameId`
2. Solves the board using Gaussian Elimination
3. `POST` solution back to external API

**Response:** Same `SubmitSolutionResponse` as above.

---

## 🧠 How the Solver Works

The solver is located in `LightsOutGaussian.java` and uses **Gaussian Elimination over GF(2)**.

### Algorithm Steps

1. **Model as a linear system** — Each cell press is a binary variable `x[i]`. The effect of pressing any cell on its neighbours is encoded as a matrix `A`, and the target state (turn all lights off) forms the vector `b`.

2. **Gaussian Elimination mod 2** — Reduce the augmented matrix `[A | b]` using XOR operations (since `1 + 1 = 0` in GF(2)).

3. **Back substitution** — Solve for each variable to determine which cells need to be pressed.

4. **Return moves** — All cells where `x[i] = 1` are returned as the required moves.

> If no solution exists for a given board, the solver returns `null` and an empty move list is used.

### Example

```
Input grid:
1 1 1 0 0
1 0 0 1 0
0 0 1 1 0
0 0 1 1 0
1 0 1 0 0

Output: list of (row, col) coordinates to press
```

---

## 📦 Key Data Models

| Class | Description |
|---|---|
| `CreateGameRequest` | `{ teamId, gameType }` — body for creating a game |
| `SolveGameRequest` | `{ teamId, gameId }` — body for fetching a game |
| `GameResponse` | `{ success, message, data: GameData }` |
| `GameData` | `{ gameId, teamId, gameType, grid, size, createdAt, solved, moveCount }` |
| `SolveResponse` | `{ gameId, teamId, moves: [{ row, col }] }` — sent to solution API |
| `Move` | `{ row, col }` — a single cell press |
| `SubmitSolutionResponse` | `{ success, message, data: SubmitSolutionData }` |
| `SubmitSolutionData` | `{ valid, message, moveCount, gameId, teamId, solvedAt }` |

---

## 🏗️ Architecture Notes

- **`WebClient`** (non-blocking, reactive) is used for all HTTP calls to the external API, with `.block()` to keep the endpoint synchronous.
- **`WebClient.Builder`** is registered as a Spring `@Bean` in `LightOutSolverApplication` and injected via **constructor injection** into the controller.
- **`LightsOutController`** is the original controller (kept for reference) and is **disabled** (`@RestController` annotation removed).
- **`LightsOutControllerNew`** is the active controller handling all live requests.

---

## 📝 License

This project is for educational/competition use.

