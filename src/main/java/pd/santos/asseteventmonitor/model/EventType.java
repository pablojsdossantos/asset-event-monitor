package pd.santos.asseteventmonitor.model;

/**
 * Enum representing the different types of asset events.
 * Based on the CSV example, we have PRICE_UPDATE, SPLIT, and AGGREGATE event types.
 */
public enum EventType {
    PRICE_UPDATE,
    SPLIT,
    AGGREGATE
}