package com.lightoutsolver.solution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestController
@RequestMapping("/lightsout")
public class LightsOutControllerNew {
    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String GAMES_ENDPOINT = "https://planetrandall.com/lightsout/api/games";
    private static final String SOLUTION_ENDPOINT = "https://planetrandall.com/lightsout/api/games/solution";

    @PostMapping("/create")
    public SubmitSolutionResponse createAndSolve(@RequestBody CreateGameRequest request) {
        log.info("Creating game - teamId: {}, gameType: {}", request.getTeamId(), request.getGameType());

        WebClient webClient = webClientBuilder.build();

        // Step 1: Call API 1 — Create the game
        GameResponse gameResponse = webClient.post()
                .uri(GAMES_ENDPOINT)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GameResponse.class)
                .doOnError(e -> log.error("Error creating game: {}", e.getMessage()))
                .block();

        if (gameResponse == null || !gameResponse.isSuccess()) {
            throw new RuntimeException("Failed to create game");
        }

        log.info("Game created. gameId: {}, size: {}x{}",
                gameResponse.getData().getGameId(),
                gameResponse.getData().getSize(),
                gameResponse.getData().getSize());

        SolveResponse solveResponse = LightOut.solve(gameResponse);
        log.info("Puzzle solved. Moves: {}", solveResponse.getMoves().size());

        return submitSolution(webClient, solveResponse);
    }

    @PostMapping("/solve")
    public SubmitSolutionResponse fetchAndSolve(@RequestBody SolveGameRequest request) {
        String gameId = request.getGameId();
        String teamId = request.getTeamId();
        log.info("Fetching game - gameId: {}", gameId);

        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId is required");
        }
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException("teamId is required");
        }

        WebClient webClient = webClientBuilder.build();

        GameResponse gameResponse = webClient.get()
                .uri(GAMES_ENDPOINT + "/{gameId}", gameId)
                .retrieve()
                .bodyToMono(GameResponse.class)
                .doOnError(e -> log.error("Error fetching game: {}", e.getMessage()))
                .block();

        if (gameResponse == null || !gameResponse.isSuccess()) {
            throw new RuntimeException("Failed to fetch game");
        }

        log.info("Game fetched. size: {}x{}",
                gameResponse.getData().getSize(),
                gameResponse.getData().getSize());
        log.info("Game details: {}", gameResponse);

        SolveResponse solveResponse = LightOut.solve(gameResponse, request.getTeamId());
        log.info("Puzzle solved: {}", solveResponse);

        return submitSolution(webClient, solveResponse);
    }

    private SubmitSolutionResponse submitSolution(WebClient webClient, SolveResponse solveResponse) {
        SubmitSolutionResponse submitResponse = null;
        try {
            submitResponse = webClient.post()
                    .uri(SOLUTION_ENDPOINT)
                    .bodyValue(solveResponse)
                    .retrieve()
                    .bodyToMono(SubmitSolutionResponse.class)
                    .doOnSuccess(res -> log.info("Solution accepted: {}", res.getData()))
                    //.doOnError(e -> log.error("Error submitting solution: {}", e.getMessage()))
                    .block();
        }
        catch (WebClientResponseException e){
            log.warn("Response Body "+e.getResponseBodyAsString());
        }

        if (submitResponse == null) {
            throw new RuntimeException("No response from solution endpoint");
        }

        log.info("Done. solvedAt: {}", submitResponse.getData().getSolvedAt());
        return submitResponse;
    }
}
