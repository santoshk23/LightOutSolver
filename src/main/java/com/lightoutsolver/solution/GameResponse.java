package com.lightoutsolver.solution;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameResponse {
    private boolean success;
    private String message;
    private GameData data;
}