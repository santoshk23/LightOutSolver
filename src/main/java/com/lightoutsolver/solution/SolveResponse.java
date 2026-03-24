package com.lightoutsolver.solution;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SolveResponse {
    private String gameId;
    private String teamId;
    private List<Move> moves;
}