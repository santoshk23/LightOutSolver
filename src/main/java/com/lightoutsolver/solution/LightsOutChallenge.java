package com.lightoutsolver.solution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/challenge")
@Slf4j
public class LightsOutChallenge {
    @Autowired
    private WebClient.Builder webClientBuilder;
    private static final String COMPETITIONS_ENDPOINT = "https://planetrandall.com/lightsout/api/competitions/";
    private Integer solvedCount = 0;
    private Integer submittedCount = 0;

    @PostMapping("/solve")
    public LightsOutChallengeResponse fetchAndSolve(@RequestBody LightsOutChallengeRequest request) {
        solvedCount=0;
        submittedCount=0;
        String competitionId = request.getCompetitionId();
        String teamId = request.getTeamId();
        log.info("Fetching game - competitionId: {}", competitionId);

        if (competitionId == null || competitionId.isBlank()) {
            throw new IllegalArgumentException("competitionId is required");
        }
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException("teamId is required");
        }



        WebClient webClient = webClientBuilder.build();

        for (int gameNumber=1; gameNumber<=100;gameNumber++) {

            GameResponse gameResponse = webClient.get()
                    .uri(COMPETITIONS_ENDPOINT + competitionId + "/games/"+ gameNumber)
                    .retrieve()
                    .bodyToMono(GameResponse.class)
                    .doOnError(e -> log.error("Error fetching game: {}", e.getMessage()))
                    .block();

            if (gameResponse == null || !gameResponse.isSuccess()) {
                log.warn("Failed to fetch game number {}. Skipping.", gameNumber);
                continue;
            }

            log.info("Game details: {}", gameResponse);

            SolveResponse solveResponse = LightOut.solve(gameResponse, request.getTeamId());
            log.info("Puzzle solved: {}", solveResponse);

            submitCompetitionSolution(webClient, solveResponse, Integer.toString(gameNumber), competitionId);
        }
        return new LightsOutChallengeResponse(solvedCount, submittedCount);
    }

    private void submitCompetitionSolution(WebClient webClient, SolveResponse solveResponse, String gameNumber, String competitionId) {
        try {
            webClient.post()
                    .uri(COMPETITIONS_ENDPOINT + competitionId + "/games/{gameId}/solution", gameNumber)
                    .bodyValue(solveResponse)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            solvedCount++;
            log.info("Solution accepted for game {}", gameNumber);
        } catch (Exception e) {
            // Do not rethrow - keep the outer loop running
            log.error("Error submitting solution for game {}: {}", gameNumber, e.getMessage(), e);
        } finally {
            submittedCount++;
            log.info("Submission attempt finished for game {}", gameNumber);
        }
    }
}
