package com.lightoutsolver.solution;

import com.lightoutsolver.gaussian.LightsOutGaussian;

import java.util.List;
import java.util.stream.Collectors;

public class LightOut {

    public static SolveResponse solve(GameResponse gameResponse) {
        GameData data = gameResponse.getData();

        int size = data.getSize();
        List<List<Integer>> grid = data.getGrid();

        int[][] matrix = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                matrix[r][c] = grid.get(r).get(c);
            }
        }

        // Solve using Gaussian Elimination
        List<int[]> rawMoves = LightsOutGaussian.solve(matrix);

        // Convert int[] moves → Move objects
        List<Move> moves = rawMoves == null
                ? List.of()
                : rawMoves.stream()
                .map(m -> new Move(m[0], m[1]))
                .collect(Collectors.toList());

        return new SolveResponse(data.getGameId(), data.getTeamId(), moves);
    }

    public static SolveResponse solve(GameResponse gameResponse, String teamIdOverride) {
        if (teamIdOverride != null && !teamIdOverride.isBlank()) {
            gameResponse.getData().setTeamId(teamIdOverride);
        }
        return solve(gameResponse);
    }

}
