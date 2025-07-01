package pd.santos.asseteventmonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import pd.santos.asseteventmonitor.model.AssetEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing AssetEvent objects to Kafka.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;
    private final String topicName;

    public KafkaProducerService(
            KafkaTemplate<String, AssetEvent> kafkaTemplate,
            @Value("${asset.events.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Publish a list of AssetEvent objects to Kafka.
     *
     * @param events The list of events to publish
     */
    public void publishEvents(List<AssetEvent> events) {
        events.forEach(this::publishEvent);
    }

    /**
     * Publish a single AssetEvent to Kafka.
     *
     * @param event The event to publish
     */
    public void publishEvent(AssetEvent event) {
        // Use the ticker as the key for the Kafka record
        String key = event.getTicker();
        
        CompletableFuture<SendResult<String, AssetEvent>> future = kafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Published event with ID {} for ticker {} to topic {}",
                        event.getEventId(), event.getTicker(), topicName);
            } else {
                logger.error("Failed to publish event with ID {} for ticker {} to topic {}",
                        event.getEventId(), event.getTicker(), topicName, ex);
            }
        });
    }
}