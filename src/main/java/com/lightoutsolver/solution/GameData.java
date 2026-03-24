package com.lightoutsolver.solution;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameData {
    private String gameId;
    private String teamId;
    private String gameType;
    private List<List<Integer>> grid;
    private int size;
    private Instant createdAt;
    private boolean solved;
    private int moveCount;
}