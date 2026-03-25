package com.lightoutsolver.solution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
//@RestController
@RequestMapping("/lightsout")
public class LightsOutController {
    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String API_BASE_URL = "https://planetrandall.com/lightsout/api/games";
    private static final String SOLUTION_ENDPOINT = "https://planetrandall.com/lightsout/api/games/solution";

    @PostMapping("/solve")
    public SubmitSolutionResponse solvePuzzle(@RequestParam String gameId) {
        log.info("Solving puzzle with gameId: {}", gameId);

        // Step 1: Fetch the puzzle from the external API
        WebClient webClient = webClientBuilder.build();

        GameResponse gameResponse = webClient.get()
                .uri(API_BASE_URL + "/{id}", gameId)
                .retrieve()
                .bodyToMono(GameResponse.class)
                .doOnError(e -> log.error("Error fetching game: {}", e.getMessage()))
                .block();

        if (gameResponse == null || !gameResponse.isSuccess()) {
            throw new RuntimeException("Failed to fetch game from API");
        }
        log.info("Game fetched successfully. Size: {}x{}",
                gameResponse.getData().getSize(),
                gameResponse.getData().getSize());

        // Step 2: Solve the puzzle
        SolveResponse solveResponse = LightOut.solve(gameResponse);

        log.info("Puzzle solved! Found {} moves", solveResponse.getMoves().size());

        // Step 3: Send the solution to the external API
        SubmitSolutionResponse submitResponse = webClient.post()
                .uri(SOLUTION_ENDPOINT)
                .bodyValue(solveResponse)
                .retrieve()
                .bodyToMono(SubmitSolutionResponse.class)
                .doOnSuccess(res -> log.info("Solution submitted successfully: {}",
                        res.getData()))
                .doOnError(e -> log.error("Error submitting solution: {}", e.getMessage()))
                .block();

        if (submitResponse == null) {
            throw new RuntimeException("No response from solution endpoint");
        }

        log.info("Final result: {}", submitResponse.getData());
        log.info("Solved at: {}", submitResponse.getData().getSolvedAt());

        return submitResponse;
    }
}
