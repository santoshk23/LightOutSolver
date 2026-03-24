package com.lightoutsolver.solution;

import lombok.Data;

@Data
public class SubmitSolutionResponse {
    private boolean success;
    private String message;
    private SubmitSolutionData data;
}
