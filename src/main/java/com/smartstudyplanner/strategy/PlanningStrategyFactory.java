package com.smartstudyplanner.strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for obtaining PlanningStrategy implementations.
 * Demonstrates Factory Pattern in OOP.
 */
public class PlanningStrategyFactory {

    private static final Map<String, PlanningStrategy> strategies = new HashMap<>();

    static {
        registerStrategy(new PriorityBasedPlanningStrategy());
        registerStrategy(new DeadlineBasedPlanningStrategy());
    }

    public static void registerStrategy(PlanningStrategy strategy) {
        if (strategy != null) {
            strategies.put(strategy.getName().toUpperCase(), strategy);
        }
    }

    public static PlanningStrategy getStrategy(String strategyName) {
        if (strategyName == null) {
            return strategies.get(PriorityBasedPlanningStrategy.NAME);
        }
        PlanningStrategy s = strategies.get(strategyName.trim().toUpperCase());
        return s != null ? s : strategies.get(PriorityBasedPlanningStrategy.NAME);
    }

    public static Map<String, PlanningStrategy> getAllStrategies() {
        return CollectionsUnmodifiableMap();
    }

    private static Map<String, PlanningStrategy> CollectionsUnmodifiableMap() {
        return java.util.Collections.unmodifiableMap(strategies);
    }
}

