package com.lightoutsolver.solution;

import lombok.Data;

import java.time.Instant;

@Data
public class SubmitSolutionData {
    private boolean valid;
    private String message;
    private int moveCount;
    private String gameId;
    private String teamId;
    private Instant solvedAt;
}
