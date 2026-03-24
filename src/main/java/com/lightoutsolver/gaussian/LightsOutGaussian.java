package com.lightoutsolver.gaussian;

import java.util.ArrayList;
import java.util.List;

public class LightsOutGaussian {

    public static List<int[]> solve(int[][] grid) {
        int n = grid.length;
        int m = grid[0].length;

        int[][] idx = new int[n][m];
        int varCount = 0;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (grid[r][c] != -1) {
                    idx[r][c] = varCount++;
                } else {
                    idx[r][c] = -1;
                }
            }
        }

        int N = varCount;
        int[][] A = new int[N][N];
        int[] b = new int[N];

        int[][] dirs = { {0,0},{1,0},{-1,0},{0,1},{0,-1} };

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {

                if (idx[r][c] == -1) continue;

                int eq = idx[r][c];
                b[eq] = grid[r][c] == 1 ? 1 : 0;

                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];
                    if (nr < 0 || nr >= n || nc < 0 || nc >= m) continue;
                    if (idx[nr][nc] == -1) continue;

                    int var = idx[nr][nc];
                    A[eq][var] = 1;  // toggles
                }
            }
        }

        int[] x = gaussianEliminationMod2(A, b);

        if (x == null) {
            return null; // No solution
        }

        List<int[]> moves = new ArrayList<>();

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (idx[r][c] != -1 && x[idx[r][c]] == 1) {
                    moves.add(new int[]{r, c});
                }
            }
        }

        return moves;
    }

    private static int[] gaussianEliminationMod2(int[][] A, int[] b) {
        int n = A.length;
        int m = A[0].length;

        int row = 0;

        for (int col = 0; col < m && row < n; col++) {

            int pivot = -1;

            for (int r = row; r < n; r++) {
                if (A[r][col] == 1) {
                    pivot = r;
                    break;
                }
            }

            if (pivot == -1) continue;

            swap(A, b, row, pivot);

            for (int r = row + 1; r < n; r++) {
                if (A[r][col] == 1) {
                    for (int c = col; c < m; c++) {
                        A[r][c] ^= A[row][c];
                    }
                    b[r] ^= b[row];
                }
            }

            row++;
        }

        int[] x = new int[m];

        for (int r = n - 1; r >= 0; r--) {
            int lead = -1;
            for (int c = 0; c < m; c++) {
                if (A[r][c] == 1) {
                    lead = c;
                    break;
                }
            }
            if (lead == -1) {
                if (b[r] == 1) return null;
                continue;
            }

            int sum = b[r];
            for (int c = lead + 1; c < m; c++) {
                sum ^= (A[r][c] & x[c]);
            }
            x[lead] = sum;
        }

        return x;
    }

    private static void swap(int[][] A, int[] b, int r1, int r2) {
        int[] tmp = A[r1];
        A[r1] = A[r2];
        A[r2] = tmp;

        int t = b[r1];
        b[r1] = b[r2];
        b[r2] = t;
    }

    public static void main(String[] args) {
        int[][] grid = {
                {1,1,1,0,0},
                {1,0,0,1,0},
                {0,0,1,1,0},
                {0,0,1,1,0},
                {1,0,1,0,0}
        };

        List<int[]> solution = solve(grid);

        if (solution == null) {
            System.out.println("No solution exists!");
        } else {
            System.out.println("Moves = " + solution.size());
            for (int[] m : solution) {
                System.out.println("(" + m[0] + "," + m[1] + ")");
            }
        }
    }
}