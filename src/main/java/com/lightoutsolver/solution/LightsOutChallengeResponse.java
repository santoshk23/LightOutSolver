package com.lightoutsolver.solution;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LightsOutChallengeResponse {
    private Integer solvedCount;
    private Integer submittedCount;
}
