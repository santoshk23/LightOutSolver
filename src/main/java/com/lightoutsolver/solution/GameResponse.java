package com.lightoutsolver.solution;

import lombok.Data;

@Data
public class GameResponse {
    private boolean success;
    private String message;
    private GameData data;
}