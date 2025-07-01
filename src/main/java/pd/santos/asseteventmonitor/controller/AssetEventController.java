package pd.santos.asseteventmonitor.controller;

import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pd.santos.asseteventmonitor.model.AssetEvent;
import pd.santos.asseteventmonitor.service.CsvParserService;
import pd.santos.asseteventmonitor.service.KafkaProducerService;

import java.io.IOException;
import java.util.List;

/**
 * Controller for handling asset event operations.
 */
@RestController
@RequestMapping("/api/asset-events")
public class AssetEventController {

    private static final Logger logger = LoggerFactory.getLogger(AssetEventController.class);

    private final CsvParserService csvParserService;
    private final KafkaProducerService kafkaProducerService;

    public AssetEventController(CsvParserService csvParserService, KafkaProducerService kafkaProducerService) {
        this.csvParserService = csvParserService;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Import asset events from a CSV file and publish them to Kafka.
     *
     * @param file The CSV file containing asset events
     * @return A response indicating success or failure
     */
    @PostMapping("/import")
    public ResponseEntity<?> importEvents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a non-empty file");
        }

        try {
            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            logger.info("Parsing CSV file: {}", file.getOriginalFilename());
            List<AssetEvent> events = csvParserService.parseAssetEvents(file);
            
            if (events.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid events found in the CSV file");
            }
            
            logger.info("Publishing {} events to Kafka", events.size());
            kafkaProducerService.publishEvents(events);
            
            return ResponseEntity.ok().body("Successfully imported " + events.size() + " events");
        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
            return ResponseEntity.badRequest().body("Error reading CSV file: " + e.getMessage());
        } catch (CsvValidationException e) {
            logger.error("Error parsing CSV file", e);
            return ResponseEntity.badRequest().body("Error parsing CSV file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}