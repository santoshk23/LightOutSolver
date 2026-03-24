package com.lightoutsolver.solution;

import lombok.Data;

@Data
public class CreateGameRequest {
    private String teamId;
    private String gameType;
}