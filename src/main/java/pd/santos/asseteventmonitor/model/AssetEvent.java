package pd.santos.asseteventmonitor.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Model class representing an asset event.
 * This class is used to store data parsed from CSV files and to be published to Kafka.
 * Uses Lombok's builder pattern for object creation.
 */
@Getter
@ToString
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AssetEvent {
    @Builder.Default
    private final UUID eventId = UUID.randomUUID();
    private final String ticker;
    private final EventType eventType;
    private final BigDecimal amount;
    private final LocalDate date;
}
