package pd.santos.asseteventmonitor.service;

import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pd.santos.asseteventmonitor.exception.CsvRowParseException;
import pd.santos.asseteventmonitor.model.AssetEvent;
import pd.santos.asseteventmonitor.model.EventType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private final CsvParserService csvParserService = new CsvParserService();

    @Test
    void parseAssetEvents_shouldParseCorrectly_whenStandardColumnOrder() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = "ticker,eventType,amount,date\nEQIX,PRICE_UPDATE,165.75,2025-06-01";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act
        List<AssetEvent> events = csvParserService.parseAssetEvents(file);

        // Assert
        assertEquals(1, events.size());
        AssetEvent event = events.get(0);
        assertEquals("EQIX", event.getTicker());
        assertEquals(EventType.PRICE_UPDATE, event.getEventType());
        assertEquals(new BigDecimal("165.75"), event.getAmount());
        assertEquals(LocalDate.of(2025, 6, 1), event.getDate());
    }

    @Test
    void parseAssetEvents_shouldParseCorrectly_whenDifferentColumnOrder() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = "date,amount,ticker,eventType\n2025-06-01,165.75,EQIX,PRICE_UPDATE";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act
        List<AssetEvent> events = csvParserService.parseAssetEvents(file);

        // Assert
        assertEquals(1, events.size());
        AssetEvent event = events.get(0);
        assertEquals("EQIX", event.getTicker());
        assertEquals(EventType.PRICE_UPDATE, event.getEventType());
        assertEquals(new BigDecimal("165.75"), event.getAmount());
        assertEquals(LocalDate.of(2025, 6, 1), event.getDate());
    }

    @Test
    void parseAssetEvents_shouldParseCorrectly_whenDifferentColumnNames() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = "Symbol,Event Type,Value,Transaction Date\nEQIX,PRICE_UPDATE,165.75,2025-06-01";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act & Assert
        assertThrows(IOException.class, () -> csvParserService.parseAssetEvents(file),
                "Should throw IOException when column names don't match expected values");
    }

    @Test
    void parseAssetEvents_shouldThrowException_whenMissingRequiredColumns() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = "ticker,eventType,amount\nEQIX,PRICE_UPDATE,165.75";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act & Assert
        assertThrows(IOException.class, () -> csvParserService.parseAssetEvents(file),
                "Should throw IOException when required columns are missing");
    }

    @Test
    void parseAssetEvents_shouldThrowException_whenInvalidRow() {
        // Arrange
        String csvContent = "ticker,eventType,amount,date\n" +
                "EQIX,PRICE_UPDATE,165.75,2025-06-01\n" +
                "INVALID,NOT_A_TYPE,abc,not-a-date\n" +
                "GOOG,SPLIT,2.5,2025-07-15";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act & Assert
        CsvRowParseException exception = assertThrows(CsvRowParseException.class, 
                () -> csvParserService.parseAssetEvents(file),
                "Should throw CsvRowParseException when a row cannot be parsed");

        // Verify the exception contains the correct information
        assertTrue(exception.getMessage().contains("Error parsing row: INVALID,NOT_A_TYPE,abc,not-a-date"));
        assertEquals("INVALID", exception.getRow()[0]);
        assertEquals("NOT_A_TYPE", exception.getRow()[1]);
        assertEquals("abc", exception.getRow()[2]);
        assertEquals("not-a-date", exception.getRow()[3]);
    }

    @Test
    void parseAssetEvents_shouldThrowException_whenEmptyFile() {
        // Arrange
        String csvContent = "";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act & Assert
        assertThrows(IOException.class, () -> csvParserService.parseAssetEvents(file),
                "Should throw IOException when file is empty");
    }

    @Test
    void parseAssetEvents_shouldThrowException_whenInsufficientColumns() {
        // Arrange
        String csvContent = "ticker,eventType,amount,date\n" +
                "EQIX,PRICE_UPDATE,165.75,2025-06-01\n" +
                "GOOG,SPLIT\n" +  // Row with insufficient columns
                "AMZN,AGGREGATE,3.5,2025-08-20";
        MultipartFile file = new MockMultipartFile("file", "events.csv", "text/csv", csvContent.getBytes());

        // Act & Assert
        CsvRowParseException exception = assertThrows(CsvRowParseException.class, 
                () -> csvParserService.parseAssetEvents(file),
                "Should throw CsvRowParseException when a row has insufficient columns");

        // Verify the exception contains the correct information
        assertTrue(exception.getMessage().contains("Error parsing row: insufficient columns: GOOG,SPLIT"));
        assertEquals("GOOG", exception.getRow()[0]);
        assertEquals("SPLIT", exception.getRow()[1]);
    }
}
