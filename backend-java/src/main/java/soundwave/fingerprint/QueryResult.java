package soundwave.fingerprint;

import java.util.Map;

public record QueryResult(Map<Integer, String> winnerNames, Map<Integer, Double> scores, int[] top5) {}